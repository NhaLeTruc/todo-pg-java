package com.todoapp.integration.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.application.dto.TimeEntryDTO;
import com.todoapp.domain.model.EntryType;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TimeEntry;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TimeEntryRepository;
import com.todoapp.domain.repository.UserRepository;
import com.todoapp.infrastructure.security.JwtTokenProvider;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Time Tracking API Integration Tests")
class TimeTrackingApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private TimeEntryRepository timeEntryRepository;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Autowired private PasswordEncoder passwordEncoder;

  private User testUser;
  private Task testTask;
  private String authToken;

  @BeforeEach
  void setUp() {
    timeEntryRepository.deleteAll();
    taskRepository.deleteAll();
    userRepository.deleteAll();

    testUser =
        User.builder()
            .email("timetrack@test.com")
            .username("timetracker")
            .password(passwordEncoder.encode("password"))
            .build();
    testUser = userRepository.save(testUser);

    testTask =
        Task.builder()
            .description("Test Task for Time Tracking")
            .user(testUser)
            .priority(Priority.MEDIUM)
            .isCompleted(false)
            .build();
    testTask = taskRepository.save(testTask);

    authToken = jwtTokenProvider.generateToken(testUser.getEmail());
  }

  @Test
  @DisplayName("Should start timer successfully")
  void shouldStartTimerSuccessfully() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("notes", "Working on feature");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/tasks/{taskId}/time-entries/start", testTask.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.taskId").value(testTask.getId()))
            .andExpect(jsonPath("$.entryType").value("TIMER"))
            .andExpect(jsonPath("$.startTime").exists())
            .andExpect(jsonPath("$.endTime").doesNotExist())
            .andExpect(jsonPath("$.running").value(true))
            .andExpect(jsonPath("$.notes").value("Working on feature"))
            .andReturn();

    String content = result.getResponse().getContentAsString();
    TimeEntryDTO timeEntry = objectMapper.readValue(content, TimeEntryDTO.class);

    assertNotNull(timeEntry.getId());
    assertTrue(timeEntry.isRunning());
  }

  @Test
  @DisplayName("Should not allow starting timer when one is already running")
  void shouldNotAllowStartingTimerWhenOneIsRunning() throws Exception {
    TimeEntry activeTimer =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusMinutes(10))
            .build();
    timeEntryRepository.save(activeTimer);

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/time-entries/start", testTask.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Should stop timer successfully")
  void shouldStopTimerSuccessfully() throws Exception {
    TimeEntry runningTimer =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusMinutes(30))
            .build();
    runningTimer = timeEntryRepository.save(runningTimer);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/time-entries/{id}/stop", runningTimer.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(runningTimer.getId()))
            .andExpect(jsonPath("$.endTime").exists())
            .andExpect(jsonPath("$.running").value(false))
            .andExpect(jsonPath("$.durationMinutes").exists())
            .andReturn();

    String content = result.getResponse().getContentAsString();
    TimeEntryDTO timeEntry = objectMapper.readValue(content, TimeEntryDTO.class);

    assertFalse(timeEntry.isRunning());
    assertNotNull(timeEntry.getEndTime());
    assertNotNull(timeEntry.getDurationMinutes());
    assertTrue(timeEntry.getDurationMinutes() >= 29 && timeEntry.getDurationMinutes() <= 31);
  }

  @Test
  @DisplayName("Should not allow stopping already stopped timer")
  void shouldNotAllowStoppingAlreadyStoppedTimer() throws Exception {
    TimeEntry stoppedTimer =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusMinutes(60))
            .endTime(LocalDateTime.now().minusMinutes(30))
            .build();
    stoppedTimer = timeEntryRepository.save(stoppedTimer);

    mockMvc
        .perform(
            post("/api/v1/time-entries/{id}/stop", stoppedTimer.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Should log manual time successfully")
  void shouldLogManualTimeSuccessfully() throws Exception {
    Map<String, Object> request = new HashMap<>();
    request.put("durationMinutes", 45);
    request.put("notes", "Completed bug fix");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/tasks/{taskId}/time-entries", testTask.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.taskId").value(testTask.getId()))
            .andExpect(jsonPath("$.entryType").value("MANUAL"))
            .andExpect(jsonPath("$.durationMinutes").value(45))
            .andExpect(jsonPath("$.notes").value("Completed bug fix"))
            .andExpect(jsonPath("$.running").value(false))
            .andReturn();

    String content = result.getResponse().getContentAsString();
    TimeEntryDTO timeEntry = objectMapper.readValue(content, TimeEntryDTO.class);

    assertEquals(45, timeEntry.getDurationMinutes());
    assertEquals(EntryType.MANUAL, timeEntry.getEntryType());
  }

  @Test
  @DisplayName("Should not allow manual time with zero or negative duration")
  void shouldNotAllowManualTimeWithInvalidDuration() throws Exception {
    Map<String, Object> request = new HashMap<>();
    request.put("durationMinutes", 0);

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/time-entries", testTask.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is4xxClientError());

    request.put("durationMinutes", -10);

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/time-entries", testTask.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("Should get time entries for task")
  void shouldGetTimeEntriesForTask() throws Exception {
    TimeEntry entry1 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusHours(2))
            .endTime(LocalDateTime.now().minusHours(1))
            .build();
    timeEntryRepository.save(entry1);

    TimeEntry entry2 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(30)
            .build();
    timeEntryRepository.save(entry2);

    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/time-entries", testTask.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].taskId").value(testTask.getId()))
        .andExpect(jsonPath("$[1].taskId").value(testTask.getId()));
  }

  @Test
  @DisplayName("Should get active timer for task")
  void shouldGetActiveTimerForTask() throws Exception {
    TimeEntry activeTimer =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusMinutes(15))
            .build();
    activeTimer = timeEntryRepository.save(activeTimer);

    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/time-entries/active", testTask.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(activeTimer.getId()))
        .andExpect(jsonPath("$.running").value(true));
  }

  @Test
  @DisplayName("Should return no content when no active timer")
  void shouldReturnNoContentWhenNoActiveTimer() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/time-entries/active", testTask.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("Should get total time for task")
  void shouldGetTotalTimeForTask() throws Exception {
    TimeEntry entry1 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(45)
            .build();
    timeEntryRepository.save(entry1);

    TimeEntry entry2 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(30)
            .build();
    timeEntryRepository.save(entry2);

    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/time-entries/total", testTask.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalMinutes").value(75));
  }

  @Test
  @DisplayName("Should get time report")
  void shouldGetTimeReport() throws Exception {
    TimeEntry entry1 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(60)
            .loggedAt(LocalDateTime.now().minusDays(1))
            .build();
    timeEntryRepository.save(entry1);

    TimeEntry entry2 =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.TIMER)
            .startTime(LocalDateTime.now().minusHours(2))
            .endTime(LocalDateTime.now().minusHours(1))
            .build();
    timeEntryRepository.save(entry2);

    LocalDateTime startDate = LocalDateTime.now().minusDays(7);
    LocalDateTime endDate = LocalDateTime.now().plusDays(1);

    mockMvc
        .perform(
            get("/api/v1/time-entries/report")
                .header("Authorization", "Bearer " + authToken)
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entries").isArray())
        .andExpect(jsonPath("$.entries.length()").value(2))
        .andExpect(jsonPath("$.totalMinutes").exists())
        .andExpect(jsonPath("$.startDate").exists())
        .andExpect(jsonPath("$.endDate").exists());
  }

  @Test
  @DisplayName("Should delete time entry successfully")
  void shouldDeleteTimeEntrySuccessfully() throws Exception {
    TimeEntry entry =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(30)
            .build();
    entry = timeEntryRepository.save(entry);

    mockMvc
        .perform(
            delete("/api/v1/time-entries/{id}", entry.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().isNoContent());

    assertFalse(timeEntryRepository.findById(entry.getId()).isPresent());
  }

  @Test
  @DisplayName("Should update time entry notes")
  void shouldUpdateTimeEntryNotes() throws Exception {
    TimeEntry entry =
        TimeEntry.builder()
            .task(testTask)
            .user(testUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(30)
            .notes("Original notes")
            .build();
    entry = timeEntryRepository.save(entry);

    Map<String, String> request = new HashMap<>();
    request.put("notes", "Updated notes");

    mockMvc
        .perform(
            patch("/api/v1/time-entries/{id}/notes", entry.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notes").value("Updated notes"));

    TimeEntry updated = timeEntryRepository.findById(entry.getId()).orElseThrow();
    assertEquals("Updated notes", updated.getNotes());
  }

  @Test
  @DisplayName("Should enforce user ownership on time entries")
  void shouldEnforceUserOwnershipOnTimeEntries() throws Exception {
    User otherUser =
        User.builder()
            .email("other@test.com")
            .username("otheruser")
            .password(passwordEncoder.encode("password"))
            .build();
    otherUser = userRepository.save(otherUser);

    Task otherTask =
        Task.builder()
            .description("Other user's task")
            .user(otherUser)
            .priority(Priority.LOW)
            .isCompleted(false)
            .build();
    otherTask = taskRepository.save(otherTask);

    TimeEntry otherEntry =
        TimeEntry.builder()
            .task(otherTask)
            .user(otherUser)
            .entryType(EntryType.MANUAL)
            .durationMinutes(30)
            .build();
    otherEntry = timeEntryRepository.save(otherEntry);

    mockMvc
        .perform(
            post("/api/v1/time-entries/{id}/stop", otherEntry.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());

    mockMvc
        .perform(
            delete("/api/v1/time-entries/{id}", otherEntry.getId())
                .header("Authorization", "Bearer " + authToken))
        .andExpect(status().is4xxClientError());
  }
}

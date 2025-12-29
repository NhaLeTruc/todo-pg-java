package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.application.dto.TimeEntryDTO;
import com.todoapp.application.service.TimeTrackingService;
import com.todoapp.domain.model.EntryType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TimeEntry;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TimeEntryRepository;
import com.todoapp.presentation.exception.GlobalExceptionHandler.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeTrackingService Tests")
class TimeTrackingServiceTest {

  @Mock private TimeEntryRepository timeEntryRepository;

  @Mock private TaskRepository taskRepository;

  @InjectMocks private TimeTrackingService timeTrackingService;

  private User testUser;
  private Task testTask;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).email("user@test.com").build();

    testTask = Task.builder().id(1L).description("Test Task").user(testUser).build();
  }

  @Nested
  @DisplayName("Start Timer Tests")
  class StartTimerTests {

    @Test
    @DisplayName("Should start timer for task successfully")
    void shouldStartTimerForTaskSuccessfully() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findActiveTimerForTask(1L, 1L)).thenReturn(Optional.empty());

      ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
      when(timeEntryRepository.save(entryCaptor.capture()))
          .thenAnswer(
              invocation -> {
                TimeEntry saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
              });

      TimeEntryDTO result = timeTrackingService.startTimer(1L, 1L, null);

      assertNotNull(result, "Result should not be null");
      assertEquals(EntryType.TIMER, result.getEntryType(), "Entry type should be TIMER");
      assertNotNull(result.getStartTime(), "Start time should be set");
      assertNull(result.getEndTime(), "End time should be null for running timer");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).findActiveTimerForTask(1L, 1L);
      verify(timeEntryRepository).save(any(TimeEntry.class));

      TimeEntry savedEntry = entryCaptor.getValue();
      assertEquals(testTask, savedEntry.getTask(), "Task should match");
      assertEquals(testUser, savedEntry.getUser(), "User should match");
      assertEquals(EntryType.TIMER, savedEntry.getEntryType(), "Entry type should be TIMER");
      assertNotNull(savedEntry.getStartTime(), "Start time should be set");
      assertNull(savedEntry.getEndTime(), "End time should be null");
    }

    @Test
    @DisplayName("Should start timer with notes")
    void shouldStartTimerWithNotes() {
      String notes = "Working on feature X";
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findActiveTimerForTask(1L, 1L)).thenReturn(Optional.empty());

      ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
      when(timeEntryRepository.save(entryCaptor.capture()))
          .thenAnswer(
              invocation -> {
                TimeEntry saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
              });

      TimeEntryDTO result = timeTrackingService.startTimer(1L, 1L, notes);

      assertNotNull(result, "Result should not be null");
      assertEquals(notes, result.getNotes(), "Notes should match");

      TimeEntry savedEntry = entryCaptor.getValue();
      assertEquals(notes, savedEntry.getNotes(), "Notes should match");
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
      when(taskRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.startTimer(999L, 1L, null),
          "Should throw ResourceNotFoundException");

      verify(taskRepository).findById(999L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user does not own task")
    void shouldThrowExceptionWhenUserDoesNotOwnTask() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.startTimer(1L, 999L, null),
          "Should throw IllegalArgumentException for non-owner");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when active timer already exists")
    void shouldThrowExceptionWhenActiveTimerExists() {
      TimeEntry activeTimer =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(10))
              .build();

      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findActiveTimerForTask(1L, 1L)).thenReturn(Optional.of(activeTimer));

      assertThrows(
          IllegalStateException.class,
          () -> timeTrackingService.startTimer(1L, 1L, null),
          "Should throw IllegalStateException when active timer exists");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).findActiveTimerForTask(1L, 1L);
      verify(timeEntryRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Stop Timer Tests")
  class StopTimerTests {

    @Test
    @DisplayName("Should stop running timer successfully")
    void shouldStopRunningTimerSuccessfully() {
      TimeEntry runningTimer =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(30))
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(runningTimer));

      ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
      when(timeEntryRepository.save(entryCaptor.capture())).thenReturn(runningTimer);

      TimeEntryDTO result = timeTrackingService.stopTimer(1L, 1L);

      assertNotNull(result, "Result should not be null");
      assertNotNull(result.getEndTime(), "End time should be set");
      assertNotNull(result.getDurationMinutes(), "Duration should be calculated");

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository).save(any(TimeEntry.class));

      TimeEntry stoppedEntry = entryCaptor.getValue();
      assertNotNull(stoppedEntry.getEndTime(), "End time should be set");
      assertFalse(stoppedEntry.isRunning(), "Timer should not be running");
    }

    @Test
    @DisplayName("Should throw exception when time entry not found")
    void shouldThrowExceptionWhenTimeEntryNotFound() {
      when(timeEntryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.stopTimer(999L, 1L),
          "Should throw ResourceNotFoundException");

      verify(timeEntryRepository).findById(999L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user does not own time entry")
    void shouldThrowExceptionWhenUserDoesNotOwnTimeEntry() {
      TimeEntry runningTimer =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(30))
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(runningTimer));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.stopTimer(1L, 999L),
          "Should throw IllegalArgumentException for non-owner");

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when timer is already stopped")
    void shouldThrowExceptionWhenTimerAlreadyStopped() {
      TimeEntry stoppedTimer =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(30))
              .endTime(LocalDateTime.now().minusMinutes(10))
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(stoppedTimer));

      assertThrows(
          IllegalStateException.class,
          () -> timeTrackingService.stopTimer(1L, 1L),
          "Should throw IllegalStateException for already stopped timer");

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when stopping manual entry")
    void shouldThrowExceptionWhenStoppingManualEntry() {
      TimeEntry manualEntry =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(manualEntry));

      assertThrows(
          IllegalStateException.class,
          () -> timeTrackingService.stopTimer(1L, 1L),
          "Should throw IllegalStateException when stopping manual entry");

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Manual Time Entry Tests")
  class ManualTimeEntryTests {

    @Test
    @DisplayName("Should create manual time entry successfully")
    void shouldCreateManualTimeEntrySuccessfully() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
      when(timeEntryRepository.save(entryCaptor.capture()))
          .thenAnswer(
              invocation -> {
                TimeEntry saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
              });

      TimeEntryDTO result = timeTrackingService.logManualTime(1L, 1L, 45, "Manual work", null);

      assertNotNull(result, "Result should not be null");
      assertEquals(EntryType.MANUAL, result.getEntryType(), "Entry type should be MANUAL");
      assertEquals(45, result.getDurationMinutes(), "Duration should be 45 minutes");
      assertEquals("Manual work", result.getNotes(), "Notes should match");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).save(any(TimeEntry.class));

      TimeEntry savedEntry = entryCaptor.getValue();
      assertEquals(testTask, savedEntry.getTask(), "Task should match");
      assertEquals(testUser, savedEntry.getUser(), "User should match");
      assertEquals(EntryType.MANUAL, savedEntry.getEntryType(), "Entry type should be MANUAL");
      assertEquals(45, savedEntry.getDurationMinutes(), "Duration should be 45 minutes");
      assertEquals("Manual work", savedEntry.getNotes(), "Notes should match");
      assertNotNull(savedEntry.getLoggedAt(), "Logged at should be set");
    }

    @Test
    @DisplayName("Should create manual entry with specific logged date")
    void shouldCreateManualEntryWithSpecificLoggedDate() {
      LocalDateTime specificDate = LocalDateTime.of(2025, 1, 15, 14, 30);
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
      when(timeEntryRepository.save(entryCaptor.capture()))
          .thenAnswer(
              invocation -> {
                TimeEntry saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
              });

      TimeEntryDTO result =
          timeTrackingService.logManualTime(1L, 1L, 60, "Past work", specificDate);

      assertNotNull(result, "Result should not be null");
      assertEquals(specificDate, result.getLoggedAt(), "Logged at should match");

      TimeEntry savedEntry = entryCaptor.getValue();
      assertEquals(specificDate, savedEntry.getLoggedAt(), "Logged at should match");
    }

    @Test
    @DisplayName("Should throw exception when duration is zero")
    void shouldThrowExceptionWhenDurationIsZero() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.logManualTime(1L, 1L, 0, null, null),
          "Should throw IllegalArgumentException for zero duration");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when duration is negative")
    void shouldThrowExceptionWhenDurationIsNegative() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.logManualTime(1L, 1L, -30, null, null),
          "Should throw IllegalArgumentException for negative duration");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
      when(taskRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.logManualTime(999L, 1L, 30, null, null),
          "Should throw ResourceNotFoundException");

      verify(taskRepository).findById(999L);
      verify(timeEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user does not own task")
    void shouldThrowExceptionWhenUserDoesNotOwnTask() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.logManualTime(1L, 999L, 30, null, null),
          "Should throw IllegalArgumentException for non-owner");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Get Time Entries Tests")
  class GetTimeEntriesTests {

    @Test
    @DisplayName("Should get all time entries for task")
    void shouldGetAllTimeEntriesForTask() {
      TimeEntry entry1 =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusHours(2))
              .endTime(LocalDateTime.now().minusHours(1))
              .build();

      TimeEntry entry2 =
          TimeEntry.builder()
              .id(2L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findByTaskIdOrderByCreatedAtDesc(1L))
          .thenReturn(Arrays.asList(entry1, entry2));

      List<TimeEntryDTO> results = timeTrackingService.getTimeEntriesForTask(1L, 1L);

      assertNotNull(results, "Results should not be null");
      assertEquals(2, results.size(), "Should have 2 entries");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).findByTaskIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
      when(taskRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.getTimeEntriesForTask(999L, 1L),
          "Should throw ResourceNotFoundException");

      verify(taskRepository).findById(999L);
      verify(timeEntryRepository, never()).findByTaskIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when user does not have access to task")
    void shouldThrowExceptionWhenUserDoesNotHaveAccessToTask() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.getTimeEntriesForTask(1L, 999L),
          "Should throw IllegalArgumentException for non-owner");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository, never()).findByTaskIdOrderByCreatedAtDesc(anyLong());
    }
  }

  @Nested
  @DisplayName("Active Timer Tests")
  class ActiveTimerTests {

    @Test
    @DisplayName("Should get active timer for task")
    void shouldGetActiveTimerForTask() {
      TimeEntry activeTimer =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(15))
              .build();

      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findActiveTimerForTask(1L, 1L)).thenReturn(Optional.of(activeTimer));

      Optional<TimeEntryDTO> result = timeTrackingService.getActiveTimer(1L, 1L);

      assertTrue(result.isPresent(), "Active timer should be present");
      assertEquals(1L, result.get().getId(), "Timer ID should match");
      assertTrue(result.get().isRunning(), "Timer should be running");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).findActiveTimerForTask(1L, 1L);
    }

    @Test
    @DisplayName("Should return empty when no active timer")
    void shouldReturnEmptyWhenNoActiveTimer() {
      when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
      when(timeEntryRepository.findActiveTimerForTask(1L, 1L)).thenReturn(Optional.empty());

      Optional<TimeEntryDTO> result = timeTrackingService.getActiveTimer(1L, 1L);

      assertFalse(result.isPresent(), "No active timer should be present");

      verify(taskRepository).findById(1L);
      verify(timeEntryRepository).findActiveTimerForTask(1L, 1L);
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void shouldThrowExceptionWhenTaskNotFound() {
      when(taskRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.getActiveTimer(999L, 1L),
          "Should throw ResourceNotFoundException");

      verify(taskRepository).findById(999L);
      verify(timeEntryRepository, never()).findActiveTimerForTask(anyLong(), anyLong());
    }
  }

  @Nested
  @DisplayName("Total Time Tests")
  class TotalTimeTests {

    @Test
    @DisplayName("Should calculate total time for task")
    void shouldCalculateTotalTimeForTask() {
      when(timeEntryRepository.getTotalTimeForTask(1L)).thenReturn(180);

      int totalTime = timeTrackingService.getTotalTimeForTask(1L);

      assertEquals(180, totalTime, "Total time should be 180 minutes");

      verify(timeEntryRepository).getTotalTimeForTask(1L);
    }

    @Test
    @DisplayName("Should return zero when no time entries")
    void shouldReturnZeroWhenNoTimeEntries() {
      when(timeEntryRepository.getTotalTimeForTask(1L)).thenReturn(0);

      int totalTime = timeTrackingService.getTotalTimeForTask(1L);

      assertEquals(0, totalTime, "Total time should be 0 minutes");

      verify(timeEntryRepository).getTotalTimeForTask(1L);
    }
  }

  @Nested
  @DisplayName("Delete Time Entry Tests")
  class DeleteTimeEntryTests {

    @Test
    @DisplayName("Should delete time entry successfully")
    void shouldDeleteTimeEntrySuccessfully() {
      TimeEntry entry =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

      timeTrackingService.deleteTimeEntry(1L, 1L);

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository).delete(entry);
    }

    @Test
    @DisplayName("Should throw exception when time entry not found")
    void shouldThrowExceptionWhenTimeEntryNotFound() {
      when(timeEntryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> timeTrackingService.deleteTimeEntry(999L, 1L),
          "Should throw ResourceNotFoundException");

      verify(timeEntryRepository).findById(999L);
      verify(timeEntryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when user does not own time entry")
    void shouldThrowExceptionWhenUserDoesNotOwnTimeEntry() {
      TimeEntry entry =
          TimeEntry.builder()
              .id(1L)
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

      assertThrows(
          IllegalArgumentException.class,
          () -> timeTrackingService.deleteTimeEntry(1L, 999L),
          "Should throw IllegalArgumentException for non-owner");

      verify(timeEntryRepository).findById(1L);
      verify(timeEntryRepository, never()).delete(any());
    }
  }
}

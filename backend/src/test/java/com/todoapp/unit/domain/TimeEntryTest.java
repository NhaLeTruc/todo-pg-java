package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.EntryType;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TimeEntry;
import com.todoapp.domain.model.User;

@DisplayName("TimeEntry Entity Tests")
class TimeEntryTest {

  private User testUser;
  private Task testTask;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).email("user@test.com").build();

    testTask = Task.builder().id(1L).description("Test Task").user(testUser).build();
  }

  @Nested
  @DisplayName("Duration Calculation Tests")
  class DurationCalculationTests {

    @Test
    @DisplayName("Should calculate duration for completed timer entry")
    void shouldCalculateDurationForCompletedTimerEntry() {
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 30, 0);

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(start)
              .endTime(end)
              .build();

      assertEquals(90, entry.getDurationMinutes(), "Duration should be 90 minutes");
    }

    @Test
    @DisplayName("Should calculate duration for manual entry")
    void shouldCalculateDurationForManualEntry() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(45)
              .build();

      assertEquals(45, entry.getDurationMinutes(), "Duration should be 45 minutes");
    }

    @Test
    @DisplayName("Should calculate zero duration for same start and end time")
    void shouldCalculateZeroDurationForSameTime() {
      LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(time)
              .endTime(time)
              .build();

      assertEquals(0, entry.getDurationMinutes(), "Duration should be 0 minutes");
    }

    @Test
    @DisplayName("Should return null for running timer (no end time)")
    void shouldReturnNullForRunningTimer() {
      LocalDateTime start = LocalDateTime.now();

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(start)
              .build();

      assertNull(entry.getDurationMinutes(), "Duration should be null for running timer");
    }

    @Test
    @DisplayName("Should calculate duration across multiple hours")
    void shouldCalculateDurationAcrossMultipleHours() {
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 9, 15, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 1, 14, 45, 30);

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(start)
              .endTime(end)
              .build();

      // 5 hours 30 minutes 30 seconds = 330.5 minutes
      assertEquals(330, entry.getDurationMinutes(), "Duration should be 330 minutes (rounded)");
    }

    @Test
    @DisplayName("Should round fractional minutes down")
    void shouldRoundFractionalMinutesDown() {
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 1, 10, 5, 45);

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(start)
              .endTime(end)
              .build();

      // 5 minutes 45 seconds = 5.75 minutes
      assertEquals(5, entry.getDurationMinutes(), "Duration should be 5 minutes (rounded down)");
    }
  }

  @Nested
  @DisplayName("Timer State Tests")
  class TimerStateTests {

    @Test
    @DisplayName("Should identify running timer")
    void shouldIdentifyRunningTimer() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now())
              .build();

      assertTrue(entry.isRunning(), "Timer should be running");
    }

    @Test
    @DisplayName("Should identify stopped timer")
    void shouldIdentifyStoppedTimer() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(10))
              .endTime(LocalDateTime.now())
              .build();

      assertFalse(entry.isRunning(), "Timer should not be running");
    }

    @Test
    @DisplayName("Manual entry should not be running")
    void manualEntryShouldNotBeRunning() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      assertFalse(entry.isRunning(), "Manual entry should not be running");
    }

    @Test
    @DisplayName("Should stop running timer")
    void shouldStopRunningTimer() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(5))
              .build();

      assertTrue(entry.isRunning(), "Timer should be running initially");

      entry.stop();

      assertFalse(entry.isRunning(), "Timer should be stopped");
      assertNotNull(entry.getEndTime(), "End time should be set");
    }

    @Test
    @DisplayName("Should not allow stopping already stopped timer")
    void shouldNotAllowStoppingAlreadyStoppedTimer() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(LocalDateTime.now().minusMinutes(10))
              .endTime(LocalDateTime.now().minusMinutes(5))
              .build();

      assertThrows(
          IllegalStateException.class,
          () -> entry.stop(),
          "Should not allow stopping stopped timer");
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @Test
    @DisplayName("Should require task")
    void shouldRequireTask() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .user(testUser)
                  .entryType(EntryType.TIMER)
                  .startTime(LocalDateTime.now())
                  .build()
                  .validate(),
          "Should require task");
    }

    @Test
    @DisplayName("Should require user")
    void shouldRequireUser() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .entryType(EntryType.TIMER)
                  .startTime(LocalDateTime.now())
                  .build()
                  .validate(),
          "Should require user");
    }

    @Test
    @DisplayName("Should require entry type")
    void shouldRequireEntryType() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .startTime(LocalDateTime.now())
                  .build()
                  .validate(),
          "Should require entry type");
    }

    @Test
    @DisplayName("Timer entry should require start time")
    void timerEntryShouldRequireStartTime() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.TIMER)
                  .build()
                  .validate(),
          "Timer entry should require start time");
    }

    @Test
    @DisplayName("Manual entry should require duration")
    void manualEntryShouldRequireDuration() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.MANUAL)
                  .build()
                  .validate(),
          "Manual entry should require duration");
    }

    @Test
    @DisplayName("Manual entry should require positive duration")
    void manualEntryShouldRequirePositiveDuration() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.MANUAL)
                  .durationMinutes(0)
                  .build()
                  .validate(),
          "Manual entry should require positive duration");
    }

    @Test
    @DisplayName("Should not allow end time before start time")
    void shouldNotAllowEndTimeBeforeStartTime() {
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 1, 9, 0, 0);

      assertThrows(
          IllegalArgumentException.class,
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.TIMER)
                  .startTime(start)
                  .endTime(end)
                  .build()
                  .validate(),
          "Should not allow end time before start time");
    }

    @Test
    @DisplayName("Should allow valid timer entry")
    void shouldAllowValidTimerEntry() {
      assertDoesNotThrow(
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.TIMER)
                  .startTime(LocalDateTime.now().minusMinutes(10))
                  .endTime(LocalDateTime.now())
                  .build()
                  .validate(),
          "Should allow valid timer entry");
    }

    @Test
    @DisplayName("Should allow valid manual entry")
    void shouldAllowValidManualEntry() {
      assertDoesNotThrow(
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.MANUAL)
                  .durationMinutes(30)
                  .build()
                  .validate(),
          "Should allow valid manual entry");
    }

    @Test
    @DisplayName("Should allow running timer with no end time")
    void shouldAllowRunningTimerWithNoEndTime() {
      assertDoesNotThrow(
          () ->
              TimeEntry.builder()
                  .task(testTask)
                  .user(testUser)
                  .entryType(EntryType.TIMER)
                  .startTime(LocalDateTime.now())
                  .build()
                  .validate(),
          "Should allow running timer with no end time");
    }
  }

  @Nested
  @DisplayName("Builder Tests")
  class BuilderTests {

    @Test
    @DisplayName("Should build timer entry with all fields")
    void shouldBuildTimerEntryWithAllFields() {
      LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
      LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0, 0);
      String notes = "Working on feature implementation";

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.TIMER)
              .startTime(start)
              .endTime(end)
              .notes(notes)
              .build();

      assertEquals(testTask, entry.getTask(), "Task should match");
      assertEquals(testUser, entry.getUser(), "User should match");
      assertEquals(EntryType.TIMER, entry.getEntryType(), "Entry type should be TIMER");
      assertEquals(start, entry.getStartTime(), "Start time should match");
      assertEquals(end, entry.getEndTime(), "End time should match");
      assertEquals(notes, entry.getNotes(), "Notes should match");
      assertEquals(60, entry.getDurationMinutes(), "Duration should be 60 minutes");
    }

    @Test
    @DisplayName("Should build manual entry with all fields")
    void shouldBuildManualEntryWithAllFields() {
      LocalDateTime loggedAt = LocalDateTime.of(2025, 1, 1, 15, 0, 0);
      String notes = "Manually logged time";

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(120)
              .loggedAt(loggedAt)
              .notes(notes)
              .build();

      assertEquals(testTask, entry.getTask(), "Task should match");
      assertEquals(testUser, entry.getUser(), "User should match");
      assertEquals(EntryType.MANUAL, entry.getEntryType(), "Entry type should be MANUAL");
      assertEquals(120, entry.getDurationMinutes(), "Duration should be 120 minutes");
      assertEquals(loggedAt, entry.getLoggedAt(), "Logged at should match");
      assertEquals(notes, entry.getNotes(), "Notes should match");
    }
  }

  @Nested
  @DisplayName("Notes Tests")
  class NotesTests {

    @Test
    @DisplayName("Should allow optional notes")
    void shouldAllowOptionalNotes() {
      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .build();

      assertNull(entry.getNotes(), "Notes should be null");
    }

    @Test
    @DisplayName("Should store and retrieve notes")
    void shouldStoreAndRetrieveNotes() {
      String notes = "This is a test note with details about the work done.";

      TimeEntry entry =
          TimeEntry.builder()
              .task(testTask)
              .user(testUser)
              .entryType(EntryType.MANUAL)
              .durationMinutes(30)
              .notes(notes)
              .build();

      assertEquals(notes, entry.getNotes(), "Notes should match");
    }
  }
}

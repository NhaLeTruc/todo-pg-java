package com.todoapp.unit.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.todoapp.application.service.RecurrenceService;
import com.todoapp.domain.model.Frequency;
import com.todoapp.domain.model.RecurrencePattern;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.RecurrencePatternRepository;
import com.todoapp.domain.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrenceService Tests")
class RecurrenceServiceTest {

  @Mock private RecurrencePatternRepository recurrencePatternRepository;

  @Mock private TaskRepository taskRepository;

  @InjectMocks private RecurrenceService recurrenceService;

  private User testUser;
  private Task templateTask;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).email("test@example.com").username("testuser").build();

    templateTask =
        Task.builder()
            .id(1L)
            .description("Recurring task")
            .user(testUser)
            .isCompleted(false)
            .build();
  }

  @Nested
  @DisplayName("Generate Next Instance")
  class GenerateNextInstance {

    @Test
    @DisplayName("Should generate next daily instance")
    void shouldGenerateNextDailyInstance() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .task(templateTask)
              .generatedCount(0)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(startDate);

      // Verify pattern was updated
      ArgumentCaptor<RecurrencePattern> patternCaptor =
          ArgumentCaptor.forClass(RecurrencePattern.class);
      verify(recurrencePatternRepository).save(patternCaptor.capture());
      RecurrencePattern savedPattern = patternCaptor.getValue();
      assertThat(savedPattern.getGeneratedCount()).isEqualTo(1);
      assertThat(savedPattern.getLastGeneratedDate()).isEqualTo(startDate);

      // Verify task instance was created
      ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
      verify(taskRepository).save(taskCaptor.capture());
      Task savedTask = taskCaptor.getValue();
      assertThat(savedTask.getDescription()).isEqualTo("Recurring task");
      assertThat(savedTask.getDueDate()).isEqualTo(startDate);
      assertThat(savedTask.getUser()).isEqualTo(testUser);
      assertThat(savedTask.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("Should generate next daily instance with interval")
    void shouldGenerateNextDailyInstanceWithInterval() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(3) // Every 3 days
              .startDate(startDate)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 1, 4)); // 3 days after Jan 1
    }

    @Test
    @DisplayName("Should generate next weekly instance on specific days")
    void shouldGenerateNextWeeklyInstance() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 6); // Monday, Jan 6, 2025
      Set<DayOfWeek> daysOfWeek = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.WEEKLY)
              .intervalValue(1)
              .startDate(startDate)
              .daysOfWeek(daysOfWeek)
              .task(templateTask)
              .generatedCount(0)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 1, 6)); // First Monday
      assertThat(nextDate.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("Should generate next weekly instance after last generated")
    void shouldGenerateNextWeeklyInstanceAfterLast() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 6); // Monday
      LocalDate lastGenerated = LocalDate.of(2025, 1, 6); // Last was Monday
      Set<DayOfWeek> daysOfWeek = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.WEEKLY)
              .intervalValue(1)
              .startDate(startDate)
              .daysOfWeek(daysOfWeek)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 1, 8)); // Next Wednesday
      assertThat(nextDate.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
    }

    @Test
    @DisplayName("Should generate next monthly instance")
    void shouldGenerateNextMonthlyInstance() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 15);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.MONTHLY)
              .intervalValue(1)
              .startDate(startDate)
              .dayOfMonth(15)
              .task(templateTask)
              .generatedCount(0)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    @DisplayName("Should generate next monthly instance after last generated")
    void shouldGenerateNextMonthlyInstanceAfterLast() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 15);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 15);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.MONTHLY)
              .intervalValue(1)
              .startDate(startDate)
              .dayOfMonth(15)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 2, 15)); // Next month
    }

    @Test
    @DisplayName("Should generate next monthly instance with interval")
    void shouldGenerateNextMonthlyInstanceWithInterval() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 15);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 15);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.MONTHLY)
              .intervalValue(3) // Every 3 months
              .startDate(startDate)
              .dayOfMonth(15)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 4, 15)); // 3 months later
    }

    @Test
    @DisplayName("Should handle monthly recurrence for day 31 in shorter months")
    void shouldHandleMonthlyRecurrenceForDay31() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 31);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 31);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.MONTHLY)
              .intervalValue(1)
              .startDate(startDate)
              .dayOfMonth(31)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      // February 2025 has 28 days, so it should use the last day of the month
      assertThat(nextDate).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Should return null when pattern is completed by max occurrences")
    void shouldReturnNullWhenCompletedByMaxOccurrences() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .task(templateTask)
              .maxOccurrences(5)
              .generatedCount(5) // Already completed
              .build();

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isNull();
      verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should return null when pattern is completed by end date")
    void shouldReturnNullWhenCompletedByEndDate() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate endDate = LocalDate.of(2025, 1, 10);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 10);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .endDate(endDate)
              .task(templateTask)
              .generatedCount(10)
              .lastGeneratedDate(lastGenerated)
              .build();

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isNull();
      verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should skip dates beyond end date when generating")
    void shouldSkipDatesBeyondEndDate() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate endDate = LocalDate.of(2025, 1, 5);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 3);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(5) // Next would be Jan 8, beyond end date
              .startDate(startDate)
              .endDate(endDate)
              .task(templateTask)
              .generatedCount(1)
              .lastGeneratedDate(lastGenerated)
              .build();

      // When
      LocalDate nextDate = recurrenceService.generateNextInstance(pattern);

      // Then
      assertThat(nextDate).isNull();
      verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should copy task properties to generated instance")
    void shouldCopyTaskPropertiesToInstance() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      Task taskWithProperties =
          Task.builder()
              .id(1L)
              .description("Important recurring task")
              .user(testUser)
              .priority(com.todoapp.domain.model.Priority.HIGH)
              .isCompleted(false)
              .build();

      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .task(taskWithProperties)
              .generatedCount(0)
              .build();

      when(recurrencePatternRepository.save(any(RecurrencePattern.class))).thenReturn(pattern);
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      recurrenceService.generateNextInstance(pattern);

      // Then
      ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
      verify(taskRepository).save(taskCaptor.capture());
      Task savedTask = taskCaptor.getValue();
      assertThat(savedTask.getDescription()).isEqualTo("Important recurring task");
      assertThat(savedTask.getPriority()).isEqualTo(com.todoapp.domain.model.Priority.HIGH);
      assertThat(savedTask.getUser()).isEqualTo(testUser);
      assertThat(savedTask.getIsCompleted()).isFalse();
    }
  }

  @Nested
  @DisplayName("Process Pending Recurrences")
  class ProcessPendingRecurrences {

    @Test
    @DisplayName("Should process all pending recurrence patterns due today")
    void shouldProcessPendingPatternsDueToday() {
      // Given
      LocalDate today = LocalDate.now();
      RecurrencePattern pattern1 =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(today)
              .task(templateTask)
              .generatedCount(0)
              .build();

      Task templateTask2 =
          Task.builder()
              .id(2L)
              .description("Another recurring task")
              .user(testUser)
              .isCompleted(false)
              .build();

      RecurrencePattern pattern2 =
          RecurrencePattern.builder()
              .id(2L)
              .frequency(Frequency.WEEKLY)
              .intervalValue(1)
              .startDate(today)
              .daysOfWeek(Set.of(today.getDayOfWeek()))
              .task(templateTask2)
              .generatedCount(0)
              .build();

      when(recurrencePatternRepository.findPendingPatterns(any(LocalDate.class)))
          .thenReturn(java.util.List.of(pattern1, pattern2));
      when(recurrencePatternRepository.save(any(RecurrencePattern.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(taskRepository.save(any(Task.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // When
      int processed = recurrenceService.processPendingRecurrences();

      // Then
      assertThat(processed).isEqualTo(2);
      verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should skip completed patterns")
    void shouldSkipCompletedPatterns() {
      // Given
      LocalDate today = LocalDate.now();
      RecurrencePattern completedPattern =
          RecurrencePattern.builder()
              .id(1L)
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(today.minusDays(10))
              .task(templateTask)
              .maxOccurrences(5)
              .generatedCount(5)
              .build();

      when(recurrencePatternRepository.findPendingPatterns(any(LocalDate.class)))
          .thenReturn(java.util.List.of(completedPattern));

      // When
      int processed = recurrenceService.processPendingRecurrences();

      // Then
      assertThat(processed).isEqualTo(0);
      verify(taskRepository, never()).save(any(Task.class));
    }
  }
}

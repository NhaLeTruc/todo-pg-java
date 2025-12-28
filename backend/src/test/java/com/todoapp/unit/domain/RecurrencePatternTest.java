package com.todoapp.unit.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.Frequency;
import com.todoapp.domain.model.RecurrencePattern;

@DisplayName("RecurrencePattern Domain Model Tests")
class RecurrencePatternTest {

  @Nested
  @DisplayName("Creation and Validation")
  class CreationAndValidation {

    @Test
    @DisplayName("Should create valid daily recurrence pattern")
    void shouldCreateValidDailyRecurrence() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate endDate = LocalDate.of(2025, 12, 31);

      // When
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .endDate(endDate)
              .build();

      // Then
      assertThat(pattern.getFrequency()).isEqualTo(Frequency.DAILY);
      assertThat(pattern.getIntervalValue()).isEqualTo(1);
      assertThat(pattern.getStartDate()).isEqualTo(startDate);
      assertThat(pattern.getEndDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("Should create valid weekly recurrence pattern with specific days")
    void shouldCreateValidWeeklyRecurrence() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      Set<DayOfWeek> daysOfWeek = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

      // When
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.WEEKLY)
              .intervalValue(1)
              .startDate(startDate)
              .daysOfWeek(daysOfWeek)
              .build();

      // Then
      assertThat(pattern.getFrequency()).isEqualTo(Frequency.WEEKLY);
      assertThat(pattern.getDaysOfWeek())
          .containsExactlyInAnyOrder(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    }

    @Test
    @DisplayName("Should create valid monthly recurrence pattern with day of month")
    void shouldCreateValidMonthlyRecurrence() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 15);

      // When
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.MONTHLY)
              .intervalValue(1)
              .startDate(startDate)
              .dayOfMonth(15)
              .build();

      // Then
      assertThat(pattern.getFrequency()).isEqualTo(Frequency.MONTHLY);
      assertThat(pattern.getDayOfMonth()).isEqualTo(15);
    }

    @Test
    @DisplayName("Should reject null frequency")
    void shouldRejectNullFrequency() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(null)
                      .intervalValue(1)
                      .startDate(startDate)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Frequency cannot be null");
    }

    @Test
    @DisplayName("Should reject null start date")
    void shouldRejectNullStartDate() {
      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.DAILY)
                      .intervalValue(1)
                      .startDate(null)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Start date cannot be null");
    }

    @Test
    @DisplayName("Should reject interval value less than 1")
    void shouldRejectInvalidInterval() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.DAILY)
                      .intervalValue(0)
                      .startDate(startDate)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Interval value must be at least 1");
    }

    @Test
    @DisplayName("Should reject end date before start date")
    void shouldRejectEndDateBeforeStartDate() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 12, 31);
      LocalDate endDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.DAILY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .endDate(endDate)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("End date cannot be before start date");
    }

    @Test
    @DisplayName("Should reject weekly recurrence without days of week")
    void shouldRejectWeeklyWithoutDaysOfWeek() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.WEEKLY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Weekly recurrence must specify days of week");
    }

    @Test
    @DisplayName("Should reject monthly recurrence without day of month")
    void shouldRejectMonthlyWithoutDayOfMonth() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.MONTHLY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Monthly recurrence must specify day of month");
    }

    @Test
    @DisplayName("Should reject day of month less than 1")
    void shouldRejectInvalidDayOfMonthLow() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.MONTHLY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .dayOfMonth(0)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Day of month must be between 1 and 31");
    }

    @Test
    @DisplayName("Should reject day of month greater than 31")
    void shouldRejectInvalidDayOfMonthHigh() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.MONTHLY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .dayOfMonth(32)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Day of month must be between 1 and 31");
    }

    @Test
    @DisplayName("Should accept maximum occurrences")
    void shouldAcceptMaxOccurrences() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .maxOccurrences(10)
              .build();

      // Then
      assertThat(pattern.getMaxOccurrences()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should reject max occurrences less than 1")
    void shouldRejectInvalidMaxOccurrences() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);

      // When/Then
      assertThatThrownBy(
              () ->
                  RecurrencePattern.builder()
                      .frequency(Frequency.DAILY)
                      .intervalValue(1)
                      .startDate(startDate)
                      .maxOccurrences(0)
                      .build())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Max occurrences must be at least 1");
    }
  }

  @Nested
  @DisplayName("Recurrence State Management")
  class RecurrenceStateManagement {

    @Test
    @DisplayName("Should track generated instance count")
    void shouldTrackGeneratedCount() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .generatedCount(5)
              .build();

      // When/Then
      assertThat(pattern.getGeneratedCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should track last generated date")
    void shouldTrackLastGeneratedDate() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 5);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .lastGeneratedDate(lastGenerated)
              .build();

      // When/Then
      assertThat(pattern.getLastGeneratedDate()).isEqualTo(lastGenerated);
    }

    @Test
    @DisplayName("Should identify when pattern is completed by max occurrences")
    void shouldIdentifyCompletionByMaxOccurrences() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .maxOccurrences(10)
              .generatedCount(10)
              .build();

      // When/Then
      assertThat(pattern.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should identify when pattern is completed by end date")
    void shouldIdentifyCompletionByEndDate() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      LocalDate endDate = LocalDate.of(2025, 1, 31);
      LocalDate lastGenerated = LocalDate.of(2025, 1, 31);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .endDate(endDate)
              .lastGeneratedDate(lastGenerated)
              .build();

      // When/Then
      assertThat(pattern.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should identify when pattern is not yet completed")
    void shouldIdentifyNotCompleted() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .maxOccurrences(10)
              .generatedCount(5)
              .build();

      // When/Then
      assertThat(pattern.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("Should identify indefinite pattern (no end date or max occurrences)")
    void shouldIdentifyIndefinitePattern() {
      // Given
      LocalDate startDate = LocalDate.of(2025, 1, 1);
      RecurrencePattern pattern =
          RecurrencePattern.builder()
              .frequency(Frequency.DAILY)
              .intervalValue(1)
              .startDate(startDate)
              .generatedCount(100)
              .build();

      // When/Then
      assertThat(pattern.isCompleted()).isFalse();
    }
  }
}

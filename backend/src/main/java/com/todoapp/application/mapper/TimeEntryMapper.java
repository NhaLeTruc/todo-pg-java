package com.todoapp.application.mapper;

import com.todoapp.application.dto.TimeEntryDTO;
import com.todoapp.domain.model.TimeEntry;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TimeEntry entities and DTOs.
 */
@Component
public class TimeEntryMapper {

  /**
   * Convert TimeEntry entity to DTO.
   *
   * @param timeEntry the time entry entity
   * @return time entry DTO
   */
  public TimeEntryDTO toDTO(TimeEntry timeEntry) {
    if (timeEntry == null) {
      return null;
    }

    return TimeEntryDTO.builder()
        .id(timeEntry.getId())
        .taskId(timeEntry.getTask().getId())
        .userId(timeEntry.getUser().getId())
        .entryType(timeEntry.getEntryType())
        .startTime(timeEntry.getStartTime())
        .endTime(timeEntry.getEndTime())
        .durationMinutes(timeEntry.getDurationMinutes())
        .loggedAt(timeEntry.getLoggedAt())
        .notes(timeEntry.getNotes())
        .createdAt(timeEntry.getCreatedAt())
        .running(timeEntry.isRunning())
        .build();
  }
}

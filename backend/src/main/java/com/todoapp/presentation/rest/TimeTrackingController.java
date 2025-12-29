package com.todoapp.presentation.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.todoapp.application.dto.TimeEntryDTO;
import com.todoapp.application.service.TimeTrackingService;
import com.todoapp.infrastructure.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for time tracking operations.
 *
 * <p>Handles timer-based tracking (start/stop) and manual time logging.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Time Tracking", description = "Time tracking and reporting APIs")
public class TimeTrackingController {

  private final TimeTrackingService timeTrackingService;

  @PostMapping("/tasks/{taskId}/time-entries/start")
  @Operation(
      summary = "Start a timer for a task",
      description = "Starts tracking time for the specified task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Timer started successfully",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Active timer already exists")
      })
  public ResponseEntity<TimeEntryDTO> startTimer(
      @Parameter(description = "Task ID") @PathVariable Long taskId,
      @RequestBody(required = false) StartTimerRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    String notes = request != null ? request.getNotes() : null;
    TimeEntryDTO timeEntry =
        timeTrackingService.startTimer(taskId, userPrincipal.getUserId(), notes);

    return ResponseEntity.status(HttpStatus.CREATED).body(timeEntry);
  }

  @PostMapping("/time-entries/{id}/stop")
  @Operation(summary = "Stop a running timer", description = "Stops the specified timer")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Timer stopped successfully",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Time entry not found"),
        @ApiResponse(responseCode = "400", description = "Timer is not running")
      })
  public ResponseEntity<TimeEntryDTO> stopTimer(
      @Parameter(description = "Time entry ID") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    TimeEntryDTO timeEntry = timeTrackingService.stopTimer(id, userPrincipal.getUserId());

    return ResponseEntity.ok(timeEntry);
  }

  @PostMapping("/tasks/{taskId}/time-entries")
  @Operation(
      summary = "Log time manually for a task",
      description = "Creates a manual time entry for the specified task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Time logged successfully",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid duration")
      })
  public ResponseEntity<TimeEntryDTO> logManualTime(
      @Parameter(description = "Task ID") @PathVariable Long taskId,
      @Valid @RequestBody ManualTimeLogRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    TimeEntryDTO timeEntry =
        timeTrackingService.logManualTime(
            taskId,
            userPrincipal.getUserId(),
            request.getDurationMinutes(),
            request.getNotes(),
            request.getLoggedAt());

    return ResponseEntity.status(HttpStatus.CREATED).body(timeEntry);
  }

  @GetMapping("/tasks/{taskId}/time-entries")
  @Operation(
      summary = "Get all time entries for a task",
      description = "Returns all time entries for the specified task")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Time entries retrieved successfully",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  public ResponseEntity<List<TimeEntryDTO>> getTimeEntriesForTask(
      @Parameter(description = "Task ID") @PathVariable Long taskId,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    List<TimeEntryDTO> entries =
        timeTrackingService.getTimeEntriesForTask(taskId, userPrincipal.getUserId());

    return ResponseEntity.ok(entries);
  }

  @GetMapping("/tasks/{taskId}/time-entries/active")
  @Operation(
      summary = "Get active timer for a task",
      description = "Returns the active timer for the specified task, if one exists")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active timer retrieved (may be empty)",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Task not found")
      })
  public ResponseEntity<TimeEntryDTO> getActiveTimer(
      @Parameter(description = "Task ID") @PathVariable Long taskId,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Optional<TimeEntryDTO> activeTimer =
        timeTrackingService.getActiveTimer(taskId, userPrincipal.getUserId());

    return activeTimer.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
  }

  @GetMapping("/time-entries/active")
  @Operation(
      summary = "Get active timer for user",
      description = "Returns the active timer for the current user across all tasks")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active timer retrieved (may be empty)",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class)))
      })
  public ResponseEntity<TimeEntryDTO> getActiveTimerForUser(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Optional<TimeEntryDTO> activeTimer =
        timeTrackingService.getActiveTimerForUser(userPrincipal.getUserId());

    return activeTimer.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
  }

  @GetMapping("/tasks/{taskId}/time-entries/total")
  @Operation(
      summary = "Get total time for a task",
      description = "Returns the total time tracked for the specified task in minutes")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total time retrieved successfully",
            content = @Content(schema = @Schema(implementation = Map.class)))
      })
  public ResponseEntity<Map<String, Integer>> getTotalTimeForTask(
      @Parameter(description = "Task ID") @PathVariable Long taskId) {

    int totalTime = timeTrackingService.getTotalTimeForTask(taskId);

    return ResponseEntity.ok(Map.of("totalMinutes", totalTime));
  }

  @GetMapping("/time-entries/report")
  @Operation(
      summary = "Get time tracking report",
      description = "Returns time entries for the current user within the specified date range")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Report generated successfully",
            content = @Content(schema = @Schema(implementation = TimeReportResponse.class)))
      })
  public ResponseEntity<TimeReportResponse> getTimeReport(
      @Parameter(description = "Start date (ISO format)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startDate,
      @Parameter(description = "End date (ISO format)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endDate,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    List<TimeEntryDTO> entries =
        timeTrackingService.getTimeEntriesForUserInRange(
            userPrincipal.getUserId(), startDate, endDate);

    int totalTime =
        timeTrackingService.getTotalTimeForUserInRange(
            userPrincipal.getUserId(), startDate, endDate);

    TimeReportResponse response = new TimeReportResponse();
    response.setEntries(entries);
    response.setTotalMinutes(totalTime);
    response.setStartDate(startDate);
    response.setEndDate(endDate);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/time-entries/{id}")
  @Operation(summary = "Delete a time entry", description = "Deletes the specified time entry")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Time entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Time entry not found")
      })
  public ResponseEntity<Void> deleteTimeEntry(
      @Parameter(description = "Time entry ID") @PathVariable Long id,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    timeTrackingService.deleteTimeEntry(id, userPrincipal.getUserId());

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/time-entries/{id}/notes")
  @Operation(
      summary = "Update time entry notes",
      description = "Updates the notes for the specified time entry")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notes updated successfully",
            content = @Content(schema = @Schema(implementation = TimeEntryDTO.class))),
        @ApiResponse(responseCode = "404", description = "Time entry not found")
      })
  public ResponseEntity<TimeEntryDTO> updateNotes(
      @Parameter(description = "Time entry ID") @PathVariable Long id,
      @RequestBody UpdateNotesRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    TimeEntryDTO timeEntry =
        timeTrackingService.updateNotes(id, userPrincipal.getUserId(), request.getNotes());

    return ResponseEntity.ok(timeEntry);
  }

  @Data
  public static class StartTimerRequest {
    @Schema(description = "Optional notes about the work", example = "Working on feature X")
    private String notes;
  }

  @Data
  public static class ManualTimeLogRequest {
    @Schema(description = "Duration in minutes", example = "45", required = true, minimum = "1")
    private Integer durationMinutes;

    @Schema(description = "Notes about the work", example = "Fixed authentication bug")
    private String notes;

    @Schema(
        description = "When the time was logged (defaults to now)",
        example = "2025-01-15T14:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime loggedAt;
  }

  @Data
  public static class UpdateNotesRequest {
    @Schema(description = "Updated notes", example = "Completed feature implementation")
    private String notes;
  }

  @Data
  public static class TimeReportResponse {
    @Schema(description = "List of time entries in the date range")
    private List<TimeEntryDTO> entries;

    @Schema(description = "Total time in minutes", example = "480")
    private int totalMinutes;

    @Schema(description = "Report start date", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Report end date", example = "2025-01-31T23:59:59")
    private LocalDateTime endDate;
  }
}

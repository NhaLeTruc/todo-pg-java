package com.todoapp.application.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for batch operations on multiple tasks.
 *
 * <p>Supports operations like:
 *
 * <ul>
 *   <li>COMPLETE - Mark tasks as completed
 *   <li>DELETE - Delete tasks
 *   <li>ASSIGN_CATEGORY - Assign a category to tasks
 *   <li>ASSIGN_TAGS - Assign tags to tasks
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Batch operation request for multiple tasks")
public class BatchOperationDTO {

  @Schema(
      description = "List of task IDs to operate on",
      example = "[1, 2, 3, 4, 5]",
      required = true)
  @NotEmpty(message = "Task IDs list cannot be empty")
  private List<Long> taskIds;

  @Schema(
      description = "Type of operation to perform",
      example = "COMPLETE",
      required = true,
      allowableValues = {"COMPLETE", "DELETE", "ASSIGN_CATEGORY", "ASSIGN_TAGS"})
  @NotNull(message = "Operation type is required")
  private BatchOperationType operationType;

  @Schema(description = "Category ID (for ASSIGN_CATEGORY operation)", example = "10")
  private Long categoryId;

  @Schema(description = "List of tag IDs (for ASSIGN_TAGS operation)", example = "[5, 6, 7]")
  private List<Long> tagIds;

  /** Enum representing the type of batch operation. */
  public enum BatchOperationType {
    /** Mark tasks as completed */
    COMPLETE,

    /** Delete tasks */
    DELETE,

    /** Assign a category to tasks */
    ASSIGN_CATEGORY,

    /** Assign tags to tasks */
    ASSIGN_TAGS
  }

  /**
   * Validate operation-specific requirements.
   *
   * @throws IllegalArgumentException if validation fails
   */
  public void validate() {
    if (operationType == BatchOperationType.ASSIGN_CATEGORY && categoryId == null) {
      throw new IllegalArgumentException("Category ID is required for ASSIGN_CATEGORY operation");
    }
    if (operationType == BatchOperationType.ASSIGN_TAGS && (tagIds == null || tagIds.isEmpty())) {
      throw new IllegalArgumentException("Tag IDs are required for ASSIGN_TAGS operation");
    }
  }
}

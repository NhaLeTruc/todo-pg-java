package com.todoapp.application.dto;

import com.todoapp.domain.model.Priority;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

  private Long id;

  private String description;

  private Boolean isCompleted;

  private Priority priority;

  private LocalDateTime dueDate;

  private LocalDateTime completedAt;

  private Integer position;

  private Long categoryId;

  private String categoryName;

  private String categoryColor;

  private List<TagDTO> tags;

  private Integer estimatedDurationMinutes;

  private Integer actualDurationMinutes;

  private Boolean isOverdue;

  private Long parentTaskId;

  private Integer depth;

  private Integer subtaskProgress;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

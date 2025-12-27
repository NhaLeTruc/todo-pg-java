package com.todoapp.application.dto;

import com.todoapp.domain.model.Priority;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateDTO {

  @Size(min = 1, max = 5000, message = "Description must be between 1 and 5000 characters")
  private String description;

  private Priority priority;

  private LocalDateTime dueDate;

  private Long categoryId;

  private Integer estimatedDurationMinutes;
}

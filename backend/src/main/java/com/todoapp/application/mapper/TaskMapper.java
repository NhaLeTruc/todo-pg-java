package com.todoapp.application.mapper;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.domain.model.Category;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public Task toEntity(TaskCreateDTO dto, User user) {
    Task task = new Task();
    task.setUser(user);
    task.setDescription(dto.getDescription());
    task.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
    task.setDueDate(dto.getDueDate());
    task.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
    task.setIsCompleted(false);
    task.setPosition(0);
    task.setDepth(0);
    return task;
  }

  public TaskResponseDTO toResponseDTO(Task task) {
    TaskResponseDTO dto = new TaskResponseDTO();
    dto.setId(task.getId());
    dto.setDescription(task.getDescription());
    dto.setIsCompleted(task.getIsCompleted());
    dto.setPriority(task.getPriority());
    dto.setDueDate(task.getDueDate());
    dto.setCompletedAt(task.getCompletedAt());
    dto.setPosition(task.getPosition());
    dto.setEstimatedDurationMinutes(task.getEstimatedDurationMinutes());
    dto.setActualDurationMinutes(task.getActualDurationMinutes());
    dto.setIsOverdue(task.isOverdue());
    dto.setDepth(task.getDepth());
    dto.setCreatedAt(task.getCreatedAt());
    dto.setUpdatedAt(task.getUpdatedAt());

    if (task.getCategory() != null) {
      dto.setCategoryId(task.getCategory().getId());
      dto.setCategoryName(task.getCategory().getName());
    }

    if (task.getParentTask() != null) {
      dto.setParentTaskId(task.getParentTask().getId());
    }

    return dto;
  }

  public void updateEntityFromDTO(TaskCreateDTO dto, Task task, Category category) {
    task.setDescription(dto.getDescription());
    if (dto.getPriority() != null) {
      task.setPriority(dto.getPriority());
    }
    task.setDueDate(dto.getDueDate());
    task.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
    task.setCategory(category);
  }
}

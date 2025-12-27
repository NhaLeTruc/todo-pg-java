package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.application.service.TaskService;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;

  @Mock private UserRepository userRepository;

  @Mock private TaskMapper taskMapper;

  @InjectMocks private TaskService taskService;

  private User testUser;
  private Task testTask;
  private TaskCreateDTO createDTO;
  private TaskResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");

    testTask = new Task();
    testTask.setId(1L);
    testTask.setUser(testUser);
    testTask.setDescription("Test task");
    testTask.setPriority(Priority.MEDIUM);
    testTask.setIsCompleted(false);

    createDTO = new TaskCreateDTO();
    createDTO.setDescription("Test task");
    createDTO.setPriority(Priority.MEDIUM);

    responseDTO = new TaskResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setDescription("Test task");
    responseDTO.setPriority(Priority.MEDIUM);
    responseDTO.setIsCompleted(false);
  }

  @Test
  @DisplayName("Should create task successfully")
  void shouldCreateTaskSuccessfully() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(taskMapper.toEntity(createDTO, testUser)).thenReturn(testTask);
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.createTask(createDTO, 1L);

    assertNotNull(result);
    assertEquals("Test task", result.getDescription());
    assertEquals(Priority.MEDIUM, result.getPriority());
    assertFalse(result.getIsCompleted());

    verify(userRepository).findById(1L);
    verify(taskRepository).save(any(Task.class));
    verify(taskMapper).toResponseDTO(testTask);
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> taskService.createTask(createDTO, 999L), "User not found");

    verify(userRepository).findById(999L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should throw exception for empty description")
  void shouldThrowExceptionForEmptyDescription() {
    createDTO.setDescription("");

    assertThrows(
        IllegalArgumentException.class,
        () -> taskService.createTask(createDTO, 1L),
        "Description cannot be empty");
  }

  @Test
  @DisplayName("Should get user tasks successfully")
  void shouldGetUserTasksSuccessfully() {
    Task task2 = new Task();
    task2.setId(2L);
    task2.setUser(testUser);
    task2.setDescription("Second task");
    task2.setPriority(Priority.HIGH);

    List<Task> tasks = Arrays.asList(testTask, task2);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 20);

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class)))
        .thenReturn(responseDTO)
        .thenReturn(
            new TaskResponseDTO() {
              {
                setId(2L);
                setDescription("Second task");
                setPriority(Priority.HIGH);
              }
            });

    Page<TaskResponseDTO> result = taskService.getUserTasks(1L, pageable);

    assertNotNull(result);
    assertEquals(2, result.getContent().size());

    verify(taskRepository).findByUserId(1L, pageable);
    verify(taskMapper, times(2)).toResponseDTO(any(Task.class));
  }

  @Test
  @DisplayName("Should return empty page when user has no tasks")
  void shouldReturnEmptyPageWhenUserHasNoTasks() {
    Page<Task> emptyPage = new PageImpl<>(Arrays.asList());
    Pageable pageable = PageRequest.of(0, 20);

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(emptyPage);

    Page<TaskResponseDTO> result = taskService.getUserTasks(1L, pageable);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.getContent().size());

    verify(taskRepository).findByUserId(1L, pageable);
  }

  @Test
  @DisplayName("Should respect pagination parameters")
  void shouldRespectPaginationParameters() {
    Pageable customPageable = PageRequest.of(1, 10);
    Page<Task> taskPage = new PageImpl<>(Arrays.asList(testTask));

    when(taskRepository.findByUserId(1L, customPageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.getUserTasks(1L, customPageable);

    assertNotNull(result);
    verify(taskRepository).findByUserId(1L, customPageable);
  }

  @Test
  @DisplayName("Should toggle task completion from incomplete to complete")
  void shouldToggleTaskCompletionFromIncompleteToComplete() {
    testTask.setIsCompleted(false);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.toggleCompletion(1L, 1L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskRepository).save(testTask);
    verify(taskMapper).toResponseDTO(testTask);
  }

  @Test
  @DisplayName("Should toggle task completion from complete to incomplete")
  void shouldToggleTaskCompletionFromCompleteToIncomplete() {
    testTask.setIsCompleted(true);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.toggleCompletion(1L, 1L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskRepository).save(testTask);
    verify(taskMapper).toResponseDTO(testTask);
  }

  @Test
  @DisplayName("Should throw exception when toggling non-existent task")
  void shouldThrowExceptionWhenTogglingNonExistentTask() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> taskService.toggleCompletion(999L, 1L), "Task not found");

    verify(taskRepository).findById(999L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should throw exception when toggling task owned by different user")
  void shouldThrowExceptionWhenTogglingTaskOwnedByDifferentUser() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    assertThrows(
        RuntimeException.class,
        () -> taskService.toggleCompletion(1L, 999L),
        "User not authorized to modify task");

    verify(taskRepository).findById(1L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should update task successfully")
  void shouldUpdateTaskSuccessfully() {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated task description");
    updateDTO.setPriority(Priority.HIGH);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.updateTask(1L, updateDTO, 1L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskRepository).save(testTask);
    verify(taskMapper).toResponseDTO(testTask);
  }

  @Test
  @DisplayName("Should throw exception when updating non-existent task")
  void shouldThrowExceptionWhenUpdatingNonExistentTask() {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated description");

    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> taskService.updateTask(999L, updateDTO, 1L), "Task not found");

    verify(taskRepository).findById(999L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should throw exception when updating task owned by different user")
  void shouldThrowExceptionWhenUpdatingTaskOwnedByDifferentUser() {
    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated description");

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    assertThrows(
        RuntimeException.class,
        () -> taskService.updateTask(1L, updateDTO, 999L),
        "User not authorized to modify task");

    verify(taskRepository).findById(1L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should delete task successfully")
  void shouldDeleteTaskSuccessfully() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    taskService.deleteTask(1L, 1L);

    verify(taskRepository).findById(1L);
    verify(taskRepository).delete(testTask);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent task")
  void shouldThrowExceptionWhenDeletingNonExistentTask() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> taskService.deleteTask(999L, 1L), "Task not found");

    verify(taskRepository).findById(999L);
    verify(taskRepository, never()).delete(any(Task.class));
  }

  @Test
  @DisplayName("Should throw exception when deleting task owned by different user")
  void shouldThrowExceptionWhenDeletingTaskOwnedByDifferentUser() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    assertThrows(
        RuntimeException.class,
        () -> taskService.deleteTask(1L, 999L),
        "User not authorized to delete task");

    verify(taskRepository).findById(1L);
    verify(taskRepository, never()).delete(any(Task.class));
  }
}

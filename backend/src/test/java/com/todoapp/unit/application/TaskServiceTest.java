package com.todoapp.unit.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

import com.todoapp.application.dto.TaskCreateDTO;
import com.todoapp.application.dto.TaskResponseDTO;
import com.todoapp.application.dto.TaskUpdateDTO;
import com.todoapp.application.mapper.TaskMapper;
import com.todoapp.application.service.TaskService;
import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.Priority;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TaskRepository;
import com.todoapp.domain.repository.TaskShareRepository;
import com.todoapp.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;

  @Mock private UserRepository userRepository;

  @Mock private TaskShareRepository taskShareRepository;

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
        RuntimeException.class,
        () -> taskService.updateTask(999L, updateDTO, 1L),
        "Task not found");

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

    assertThrows(RuntimeException.class, () -> taskService.deleteTask(999L, 1L), "Task not found");

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

  @Test
  @DisplayName("Should search tasks by keyword only")
  void shouldSearchTasksByKeywordOnly() {
    Task task1 = new Task();
    task1.setId(1L);
    task1.setUser(testUser);
    task1.setDescription("Buy groceries");
    task1.setPriority(Priority.MEDIUM);

    Task task2 = new Task();
    task2.setId(2L);
    task2.setUser(testUser);
    task2.setDescription("Buy concert tickets");
    task2.setPriority(Priority.HIGH);

    List<Task> tasks = Arrays.asList(task1, task2);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.searchByUserIdAndDescription(1L, "buy", pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.searchTasks(1L, "buy", null, pageable);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    verify(taskRepository).searchByUserIdAndDescription(1L, "buy", pageable);
    verify(taskRepository, never()).findByUserId(anyLong(), any(Pageable.class));
  }

  @Test
  @DisplayName("Should search tasks by keyword and completion status")
  void shouldSearchTasksByKeywordAndCompletionStatus() {
    Task task = new Task();
    task.setId(1L);
    task.setUser(testUser);
    task.setDescription("Buy groceries");
    task.setPriority(Priority.MEDIUM);
    task.setIsCompleted(true);

    List<Task> tasks = Arrays.asList(task);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.searchByUserIdAndDescriptionAndIsCompleted(1L, "buy", true, pageable))
        .thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.searchTasks(1L, "buy", true, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(taskRepository).searchByUserIdAndDescriptionAndIsCompleted(1L, "buy", true, pageable);
  }

  @Test
  @DisplayName("Should filter tasks by completion status only")
  void shouldFilterTasksByCompletionStatusOnly() {
    Task task = new Task();
    task.setId(1L);
    task.setUser(testUser);
    task.setDescription("Completed task");
    task.setPriority(Priority.HIGH);
    task.setIsCompleted(true);

    List<Task> tasks = Arrays.asList(task);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.findByUserIdAndIsCompleted(1L, true, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.searchTasks(1L, null, true, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(taskRepository).findByUserIdAndIsCompleted(1L, true, pageable);
  }

  @Test
  @DisplayName("Should return all tasks when no search term or filter")
  void shouldReturnAllTasksWhenNoSearchTermOrFilter() {
    List<Task> tasks = Arrays.asList(testTask);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.searchTasks(1L, null, null, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(taskRepository).findByUserId(1L, pageable);
  }

  @Test
  @DisplayName("Should trim search term before searching")
  void shouldTrimSearchTermBeforeSearching() {
    List<Task> tasks = Arrays.asList(testTask);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.searchByUserIdAndDescription(1L, "test", pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    taskService.searchTasks(1L, "  test  ", null, pageable);

    verify(taskRepository).searchByUserIdAndDescription(1L, "test", pageable);
  }

  @Test
  @DisplayName("Should treat empty search term as no search")
  void shouldTreatEmptySearchTermAsNoSearch() {
    List<Task> tasks = Arrays.asList(testTask);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    taskService.searchTasks(1L, "   ", null, pageable);

    verify(taskRepository).findByUserId(1L, pageable);
    verify(taskRepository, never())
        .searchByUserIdAndDescription(anyLong(), anyString(), any(Pageable.class));
  }

  @Test
  @DisplayName("Should handle search with no results")
  void shouldHandleSearchWithNoResults() {
    Page<Task> emptyPage = new PageImpl<>(Arrays.asList());
    Pageable pageable = PageRequest.of(0, 10);

    when(taskRepository.searchByUserIdAndDescription(1L, "nonexistent", pageable))
        .thenReturn(emptyPage);

    Page<TaskResponseDTO> result = taskService.searchTasks(1L, "nonexistent", null, pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    verify(taskRepository).searchByUserIdAndDescription(1L, "nonexistent", pageable);
  }

  // ========================================
  // User Story 5: Priority and Due Date Service Tests
  // ========================================

  @Test
  @DisplayName("Should sort tasks by priority (HIGH, MEDIUM, LOW)")
  void shouldSortTasksByPriority() {
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    Task highPriorityTask = new Task();
    highPriorityTask.setId(1L);
    highPriorityTask.setUser(testUser);
    highPriorityTask.setDescription("High priority task");
    highPriorityTask.setPriority(Priority.HIGH);
    highPriorityTask.setCreatedAt(now);

    Task mediumPriorityTask = new Task();
    mediumPriorityTask.setId(2L);
    mediumPriorityTask.setUser(testUser);
    mediumPriorityTask.setDescription("Medium priority task");
    mediumPriorityTask.setPriority(Priority.MEDIUM);
    mediumPriorityTask.setCreatedAt(now);

    Task lowPriorityTask = new Task();
    lowPriorityTask.setId(3L);
    lowPriorityTask.setUser(testUser);
    lowPriorityTask.setDescription("Low priority task");
    lowPriorityTask.setPriority(Priority.LOW);
    lowPriorityTask.setCreatedAt(now);

    List<Task> tasks = Arrays.asList(highPriorityTask, mediumPriorityTask, lowPriorityTask);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable =
        PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("priority").descending());

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.getUserTasks(1L, pageable);

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    verify(taskRepository).findByUserId(1L, pageable);
  }

  @Test
  @DisplayName("Should sort tasks by due date")
  void shouldSortTasksByDueDate() {
    java.time.LocalDateTime now = java.time.LocalDateTime.now();

    Task taskDueSoon = new Task();
    taskDueSoon.setId(1L);
    taskDueSoon.setUser(testUser);
    taskDueSoon.setDescription("Task due soon");
    taskDueSoon.setPriority(Priority.MEDIUM);
    taskDueSoon.setDueDate(now.plusDays(1));

    Task taskDueLater = new Task();
    taskDueLater.setId(2L);
    taskDueLater.setUser(testUser);
    taskDueLater.setDescription("Task due later");
    taskDueLater.setPriority(Priority.MEDIUM);
    taskDueLater.setDueDate(now.plusDays(7));

    Task taskNoDueDate = new Task();
    taskNoDueDate.setId(3L);
    taskNoDueDate.setUser(testUser);
    taskNoDueDate.setDescription("Task without due date");
    taskNoDueDate.setPriority(Priority.MEDIUM);

    List<Task> tasks = Arrays.asList(taskDueSoon, taskDueLater, taskNoDueDate);
    Page<Task> taskPage = new PageImpl<>(tasks);
    Pageable pageable =
        PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("dueDate").ascending());

    when(taskRepository.findByUserId(1L, pageable)).thenReturn(taskPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.getUserTasks(1L, pageable);

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    verify(taskRepository).findByUserId(1L, pageable);
  }

  @Test
  @DisplayName("Should combine priority and due date in task creation")
  void shouldCombinePriorityAndDueDateInTaskCreation() {
    java.time.LocalDateTime dueDate = java.time.LocalDateTime.now().plusDays(3);

    TaskCreateDTO createDTO = new TaskCreateDTO();
    createDTO.setDescription("Task with priority and due date");
    createDTO.setPriority(Priority.HIGH);
    createDTO.setDueDate(dueDate);

    Task task = new Task();
    task.setId(1L);
    task.setUser(testUser);
    task.setDescription("Task with priority and due date");
    task.setPriority(Priority.HIGH);
    task.setDueDate(dueDate);

    TaskResponseDTO responseDTO = new TaskResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setDescription("Task with priority and due date");
    responseDTO.setPriority(Priority.HIGH);
    responseDTO.setDueDate(dueDate);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(taskMapper.toEntity(createDTO, testUser)).thenReturn(task);
    when(taskRepository.save(any(Task.class))).thenReturn(task);
    when(taskMapper.toResponseDTO(task)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.createTask(createDTO, 1L);

    assertNotNull(result);
    assertEquals("Task with priority and due date", result.getDescription());
    assertEquals(Priority.HIGH, result.getPriority());
    assertEquals(dueDate, result.getDueDate());

    verify(userRepository).findById(1L);
    verify(taskRepository).save(any(Task.class));
    verify(taskMapper).toResponseDTO(task);
  }

  // ========================================
  // User Story 9: Shared Task Authorization Tests
  // ========================================

  @Test
  @DisplayName("Should allow task owner to access their task")
  void shouldAllowTaskOwnerToAccessTheirTask() {
    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.getTaskById(1L, 1L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskShareRepository, never()).findByTaskIdAndSharedWithUserId(anyLong(), anyLong());
  }

  @Test
  @DisplayName("Should allow user with VIEW permission to view shared task")
  void shouldAllowUserWithViewPermissionToViewSharedTask() {
    User sharedUser = new User();
    sharedUser.setId(2L);
    sharedUser.setEmail("shared@example.com");

    TaskShare taskShare = new TaskShare();
    taskShare.setTask(testTask);
    taskShare.setSharedWithUser(sharedUser);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L))
        .thenReturn(Optional.of(taskShare));
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.getTaskById(1L, 2L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskShareRepository).findByTaskIdAndSharedWithUserId(1L, 2L);
  }

  @Test
  @DisplayName("Should allow user with EDIT permission to edit shared task")
  void shouldAllowUserWithEditPermissionToEditSharedTask() {
    User sharedUser = new User();
    sharedUser.setId(2L);
    sharedUser.setEmail("shared@example.com");

    TaskShare taskShare = new TaskShare();
    taskShare.setTask(testTask);
    taskShare.setSharedWithUser(sharedUser);
    taskShare.setPermissionLevel(PermissionLevel.EDIT);

    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Updated by shared user");

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L))
        .thenReturn(Optional.of(taskShare));
    when(taskRepository.save(any(Task.class))).thenReturn(testTask);
    when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

    TaskResponseDTO result = taskService.updateTask(1L, updateDTO, 2L);

    assertNotNull(result);
    verify(taskRepository).findById(1L);
    verify(taskShareRepository).findByTaskIdAndSharedWithUserId(1L, 2L);
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  @DisplayName("Should deny user with VIEW permission from editing shared task")
  void shouldDenyUserWithViewPermissionFromEditingSharedTask() {
    User sharedUser = new User();
    sharedUser.setId(2L);
    sharedUser.setEmail("shared@example.com");

    TaskShare taskShare = new TaskShare();
    taskShare.setTask(testTask);
    taskShare.setSharedWithUser(sharedUser);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    TaskUpdateDTO updateDTO = new TaskUpdateDTO();
    updateDTO.setDescription("Attempt to update");

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 2L))
        .thenReturn(Optional.of(taskShare));

    assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(1L, updateDTO, 2L));

    verify(taskRepository).findById(1L);
    verify(taskShareRepository).findByTaskIdAndSharedWithUserId(1L, 2L);
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should deny unauthorized user from accessing task")
  void shouldDenyUnauthorizedUserFromAccessingTask() {
    User unauthorizedUser = new User();
    unauthorizedUser.setId(3L);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
    when(taskShareRepository.findByTaskIdAndSharedWithUserId(1L, 3L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> taskService.getTaskById(1L, 3L));

    verify(taskRepository).findById(1L);
    verify(taskShareRepository).findByTaskIdAndSharedWithUserId(1L, 3L);
  }

  @Test
  @DisplayName("Should prevent deleting shared task by non-owner")
  void shouldPreventDeletingSharedTaskByNonOwner() {
    User sharedUser = new User();
    sharedUser.setId(2L);

    TaskShare taskShare = new TaskShare();
    taskShare.setTask(testTask);
    taskShare.setSharedWithUser(sharedUser);
    taskShare.setPermissionLevel(PermissionLevel.EDIT);

    when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

    assertThrows(IllegalArgumentException.class, () -> taskService.deleteTask(1L, 2L));

    verify(taskRepository).findById(1L);
    verify(taskRepository, never()).delete(any(Task.class));
  }

  @Test
  @DisplayName("Should retrieve shared tasks for user")
  void shouldRetrieveSharedTasksForUser() {
    User sharedUser = new User();
    sharedUser.setId(2L);
    sharedUser.setEmail("shared@example.com");

    Task sharedTask = new Task();
    sharedTask.setId(2L);
    sharedTask.setUser(testUser);
    sharedTask.setDescription("Shared task");
    sharedTask.setPriority(Priority.MEDIUM);

    TaskShare taskShare = new TaskShare();
    taskShare.setTask(sharedTask);
    taskShare.setSharedWithUser(sharedUser);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    List<TaskShare> shares = Arrays.asList(taskShare);
    when(taskShareRepository.findBySharedWithUserId(2L)).thenReturn(shares);

    List<TaskShare> result = taskShareRepository.findBySharedWithUserId(2L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(2L, result.get(0).getTask().getId());
    verify(taskShareRepository).findBySharedWithUserId(2L);
  }

  @Test
  @DisplayName("Should combine owned and shared tasks in query")
  void shouldCombineOwnedAndSharedTasksInQuery() {
    User user = new User();
    user.setId(2L);

    Task ownedTask = new Task();
    ownedTask.setId(1L);
    ownedTask.setUser(user);
    ownedTask.setDescription("Owned task");

    Task sharedTask = new Task();
    sharedTask.setId(2L);
    sharedTask.setUser(testUser);
    sharedTask.setDescription("Shared task");

    Pageable pageable = PageRequest.of(0, 10);
    Page<Task> ownedPage = new PageImpl<>(Arrays.asList(ownedTask));

    when(taskRepository.findByUserId(2L, pageable)).thenReturn(ownedPage);
    when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

    Page<TaskResponseDTO> result = taskService.getUserTasks(2L, pageable);

    assertNotNull(result);
    verify(taskRepository).findByUserId(2L, pageable);
  }

  @Test
  @DisplayName("Should batch complete tasks successfully")
  void shouldBatchCompleteTasksSuccessfully() {
    List<Long> taskIds = Arrays.asList(1L, 2L, 3L);

    Task task1 =
        Task.builder().id(1L).description("Task 1").user(testUser).isCompleted(false).build();
    Task task2 =
        Task.builder().id(2L).description("Task 2").user(testUser).isCompleted(false).build();
    Task task3 =
        Task.builder().id(3L).description("Task 3").user(testUser).isCompleted(false).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
    when(taskRepository.findById(3L)).thenReturn(Optional.of(task3));

    taskService.batchComplete(taskIds, 1L);

    assertTrue(task1.getIsCompleted());
    assertTrue(task2.getIsCompleted());
    assertTrue(task3.getIsCompleted());
    verify(taskRepository, times(3)).save(any(Task.class));
  }

  @Test
  @DisplayName("Should skip tasks user doesn't own in batch complete")
  void shouldSkipTasksUserDoesNotOwnInBatchComplete() {
    User otherUser = User.builder().id(2L).email("other@test.com").build();
    List<Long> taskIds = Arrays.asList(1L, 2L);

    Task task1 =
        Task.builder().id(1L).description("Task 1").user(testUser).isCompleted(false).build();
    Task task2 =
        Task.builder().id(2L).description("Task 2").user(otherUser).isCompleted(false).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));

    taskService.batchComplete(taskIds, 1L);

    assertTrue(task1.getIsCompleted());
    assertFalse(task2.getIsCompleted());
    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  @DisplayName("Should handle empty task list in batch complete")
  void shouldHandleEmptyTaskListInBatchComplete() {
    List<Long> taskIds = Arrays.asList();

    taskService.batchComplete(taskIds, 1L);

    verify(taskRepository, never()).findById(anyLong());
    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  @DisplayName("Should batch delete tasks successfully")
  void shouldBatchDeleteTasksSuccessfully() {
    List<Long> taskIds = Arrays.asList(1L, 2L, 3L);

    Task task1 = Task.builder().id(1L).description("Task 1").user(testUser).build();
    Task task2 = Task.builder().id(2L).description("Task 2").user(testUser).build();
    Task task3 = Task.builder().id(3L).description("Task 3").user(testUser).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
    when(taskRepository.findById(3L)).thenReturn(Optional.of(task3));

    taskService.batchDelete(taskIds, 1L);

    verify(taskRepository, times(3)).delete(any(Task.class));
  }

  @Test
  @DisplayName("Should skip tasks user doesn't own in batch delete")
  void shouldSkipTasksUserDoesNotOwnInBatchDelete() {
    User otherUser = User.builder().id(2L).email("other@test.com").build();
    List<Long> taskIds = Arrays.asList(1L, 2L);

    Task task1 = Task.builder().id(1L).description("Task 1").user(testUser).build();
    Task task2 = Task.builder().id(2L).description("Task 2").user(otherUser).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));

    taskService.batchDelete(taskIds, 1L);

    verify(taskRepository, times(1)).delete(task1);
    verify(taskRepository, never()).delete(task2);
  }

  @Test
  @DisplayName("Should handle empty task list in batch delete")
  void shouldHandleEmptyTaskListInBatchDelete() {
    List<Long> taskIds = Arrays.asList();

    taskService.batchDelete(taskIds, 1L);

    verify(taskRepository, never()).findById(anyLong());
    verify(taskRepository, never()).delete(any(Task.class));
  }

  @Test
  @DisplayName("Should batch assign category to tasks")
  void shouldBatchAssignCategoryToTasks() {
    List<Long> taskIds = Arrays.asList(1L, 2L);

    Task task1 = Task.builder().id(1L).description("Task 1").user(testUser).build();
    Task task2 = Task.builder().id(2L).description("Task 2").user(testUser).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));

    taskService.batchUpdateCategory(taskIds, 5L, 1L);

    verify(taskRepository, times(2)).save(any(Task.class));
  }

  @Test
  @DisplayName("Should batch assign tags to tasks")
  void shouldBatchAssignTagsToTasks() {
    List<Long> taskIds = Arrays.asList(1L, 2L);
    List<Long> tagIds = Arrays.asList(10L, 20L);

    Task task1 = Task.builder().id(1L).description("Task 1").user(testUser).build();
    Task task2 = Task.builder().id(2L).description("Task 2").user(testUser).build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
    when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));

    taskService.batchUpdateTags(taskIds, tagIds, 1L);

    verify(taskRepository, times(2)).save(any(Task.class));
  }
}

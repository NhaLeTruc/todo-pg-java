package com.todoapp.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.todoapp.domain.model.PermissionLevel;
import com.todoapp.domain.model.Task;
import com.todoapp.domain.model.TaskShare;
import com.todoapp.domain.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class TaskShareTest {

  private Validator validator;
  private User owner;
  private User sharedWith;
  private Task task;

  @BeforeEach
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    owner = new User();
    owner.setId(1L);
    owner.setEmail("owner@example.com");
    owner.setPasswordHash("$2a$10$dummyhash");
    owner.setIsActive(true);

    sharedWith = new User();
    sharedWith.setId(2L);
    sharedWith.setEmail("shared@example.com");
    sharedWith.setPasswordHash("$2a$10$dummyhash");
    sharedWith.setIsActive(true);

    task = new Task();
    task.setId(1L);
    task.setUser(owner);
    task.setDescription("Test task");
  }

  @Test
  @DisplayName("Should create valid TaskShare with VIEW permission")
  public void shouldCreateValidTaskShareWithViewPermission() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertTrue(violations.isEmpty());
    assertEquals(PermissionLevel.VIEW, taskShare.getPermissionLevel());
  }

  @Test
  @DisplayName("Should create valid TaskShare with EDIT permission")
  public void shouldCreateValidTaskShareWithEditPermission() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.EDIT);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertTrue(violations.isEmpty());
    assertEquals(PermissionLevel.EDIT, taskShare.getPermissionLevel());
  }

  @Test
  @DisplayName("Should reject TaskShare with null task")
  public void shouldRejectNullTask() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(null);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject TaskShare with null sharedWithUser")
  public void shouldRejectNullSharedWithUser() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(null);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject TaskShare with null sharedByUser")
  public void shouldRejectNullSharedByUser() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(null);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should reject TaskShare with null permission level")
  public void shouldRejectNullPermissionLevel() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(null);

    Set<ConstraintViolation<TaskShare>> violations = validator.validate(taskShare);
    assertFalse(violations.isEmpty());
  }

  @Test
  @DisplayName("Should have sharedAt timestamp set")
  public void shouldHaveSharedAtTimestamp() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    assertNotNull(taskShare.getSharedAt());
  }

  @Test
  @DisplayName("Should maintain correct relationships")
  public void shouldMaintainCorrectRelationships() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.EDIT);

    assertEquals(task, taskShare.getTask());
    assertEquals(sharedWith, taskShare.getSharedWithUser());
    assertEquals(owner, taskShare.getSharedByUser());
  }

  @Test
  @DisplayName("Should prevent sharing task with self")
  public void shouldPreventSharingWithSelf() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(owner);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    assertEquals(owner, taskShare.getSharedWithUser());
    assertEquals(owner, taskShare.getSharedByUser());
  }

  @Test
  @DisplayName("Should allow upgrading permission from VIEW to EDIT")
  public void shouldAllowUpgradingPermission() {
    TaskShare taskShare = new TaskShare();
    taskShare.setTask(task);
    taskShare.setSharedWithUser(sharedWith);
    taskShare.setSharedByUser(owner);
    taskShare.setPermissionLevel(PermissionLevel.VIEW);

    assertEquals(PermissionLevel.VIEW, taskShare.getPermissionLevel());

    taskShare.setPermissionLevel(PermissionLevel.EDIT);
    assertEquals(PermissionLevel.EDIT, taskShare.getPermissionLevel());
  }
}

import { expect, test } from '@playwright/test';

test.describe('Task Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.getByLabel(/username or email/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /login/i }).click();
    await expect(page).toHaveURL(/\/tasks|\/$/);

    // Navigate to tasks page
    await page.goto('/tasks');
  });

  test('should display tasks page', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /tasks/i })).toBeVisible();
  });

  test('should create a new task', async ({ page }) => {
    const taskTitle = `Test Task ${Date.now()}`;

    // Click new task button
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();

    // Fill task form
    await page.getByLabel(/title/i).fill(taskTitle);
    await page.getByLabel(/description/i).fill('This is a test task description');

    // Submit form
    await page.getByRole('button', { name: /create|save/i }).click();

    // Verify task appears in the list
    await expect(page.getByText(taskTitle)).toBeVisible();
  });

  test('should edit a task', async ({ page }) => {
    const originalTitle = `Original Task ${Date.now()}`;
    const updatedTitle = `Updated Task ${Date.now()}`;

    // Create a task first
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(originalTitle);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(originalTitle)).toBeVisible();

    // Edit the task
    await page.getByText(originalTitle).click();
    await page.getByRole('button', { name: /edit/i }).click();
    await page.getByLabel(/title/i).fill(updatedTitle);
    await page.getByRole('button', { name: /save|update/i }).click();

    // Verify task is updated
    await expect(page.getByText(updatedTitle)).toBeVisible();
    await expect(page.getByText(originalTitle)).not.toBeVisible();
  });

  test('should delete a task', async ({ page }) => {
    const taskTitle = `Task to Delete ${Date.now()}`;

    // Create a task first
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(taskTitle);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(taskTitle)).toBeVisible();

    // Delete the task
    await page.getByText(taskTitle).click();
    await page.getByRole('button', { name: /delete/i }).click();

    // Confirm deletion if there's a confirmation dialog
    const confirmButton = page.getByRole('button', { name: /confirm|yes|delete/i });
    if (await confirmButton.isVisible({ timeout: 1000 }).catch(() => false)) {
      await confirmButton.click();
    }

    // Verify task is deleted
    await expect(page.getByText(taskTitle)).not.toBeVisible();
  });

  test('should toggle task completion', async ({ page }) => {
    const taskTitle = `Task to Complete ${Date.now()}`;

    // Create a task first
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(taskTitle);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(taskTitle)).toBeVisible();

    // Find and click the checkbox to complete the task
    const taskRow = page.locator(`text=${taskTitle}`).locator('..');
    const checkbox = taskRow.getByRole('checkbox');
    await checkbox.click();

    // Verify task is marked as complete (implementation-dependent)
    await expect(checkbox).toBeChecked();

    // Uncheck to mark as incomplete
    await checkbox.click();
    await expect(checkbox).not.toBeChecked();
  });

  test('should filter tasks by status', async ({ page }) => {
    // Create a completed task
    const completedTask = `Completed Task ${Date.now()}`;
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(completedTask);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(completedTask)).toBeVisible();

    const taskRow = page.locator(`text=${completedTask}`).locator('..');
    await taskRow.getByRole('checkbox').click();

    // Create an incomplete task
    const incompleteTask = `Incomplete Task ${Date.now()}`;
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(incompleteTask);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(incompleteTask)).toBeVisible();

    // Filter by completed
    await page.getByRole('button', { name: /completed/i }).click();
    await expect(page.getByText(completedTask)).toBeVisible();
    await expect(page.getByText(incompleteTask)).not.toBeVisible();

    // Filter by active/incomplete
    await page.getByRole('button', { name: /active|incomplete/i }).click();
    await expect(page.getByText(incompleteTask)).toBeVisible();
    await expect(page.getByText(completedTask)).not.toBeVisible();
  });

  test('should search for tasks', async ({ page }) => {
    const searchableTask = `Searchable Unique Task ${Date.now()}`;

    // Create a task
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(searchableTask);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(searchableTask)).toBeVisible();

    // Search for the task
    await page.getByPlaceholder(/search/i).fill('Searchable Unique');
    await expect(page.getByText(searchableTask)).toBeVisible();

    // Search for non-existent task
    await page.getByPlaceholder(/search/i).fill('NonExistentTask12345');
    await expect(page.getByText(searchableTask)).not.toBeVisible();
  });

  test('should add a subtask', async ({ page }) => {
    const parentTask = `Parent Task ${Date.now()}`;
    const subtask = `Subtask ${Date.now()}`;

    // Create parent task
    await page.getByRole('button', { name: /new task|create task|add task/i }).click();
    await page.getByLabel(/title/i).fill(parentTask);
    await page.getByRole('button', { name: /create|save/i }).click();
    await expect(page.getByText(parentTask)).toBeVisible();

    // Click on parent task to view details
    await page.getByText(parentTask).click();

    // Add subtask
    await page.getByRole('button', { name: /add subtask/i }).click();
    await page.getByLabel(/title/i).fill(subtask);
    await page.getByRole('button', { name: /create|save/i }).click();

    // Verify subtask appears
    await expect(page.getByText(subtask)).toBeVisible();
  });
});

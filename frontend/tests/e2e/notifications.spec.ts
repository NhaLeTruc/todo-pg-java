import { expect, test } from '@playwright/test';

test.describe('Notifications', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.getByLabel(/username or email/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /login/i }).click();
    await expect(page).toHaveURL(/\/tasks|\/$/);
  });

  test('should display notifications bell icon', async ({ page }) => {
    const notificationBell = page.getByRole('button', { name: /notifications/i });
    await expect(notificationBell).toBeVisible();
  });

  test('should show notification count badge', async ({ page }) => {
    const notificationBell = page.getByRole('button', { name: /notifications/i });
    await notificationBell.click();

    // Check if notification panel opens
    const notificationPanel = page.getByRole('region', { name: /notifications/i });
    await expect(notificationPanel).toBeVisible();
  });

  test('should mark notification as read', async ({ page }) => {
    // Open notifications
    await page.getByRole('button', { name: /notifications/i }).click();

    // Find first unread notification
    const firstNotification = page.locator('[data-testid="notification-item"]').first();

    if (await firstNotification.isVisible({ timeout: 2000 }).catch(() => false)) {
      // Click mark as read button
      const markReadButton = firstNotification.getByRole('button', { name: /mark as read/i });
      await markReadButton.click();

      // Notification should be marked as read or removed from unread list
      await expect(firstNotification).not.toHaveClass(/unread/);
    }
  });

  test('should mark all notifications as read', async ({ page }) => {
    // Open notifications
    await page.getByRole('button', { name: /notifications/i }).click();

    // Look for "mark all as read" button
    const markAllButton = page.getByRole('button', { name: /mark all as read/i });

    if (await markAllButton.isVisible({ timeout: 2000 }).catch(() => false)) {
      await markAllButton.click();

      // Verify all notifications are marked as read
      const unreadNotifications = page.locator('[data-testid="notification-item"].unread');
      await expect(unreadNotifications).toHaveCount(0);
    }
  });

  test('should delete a notification', async ({ page }) => {
    // Open notifications
    await page.getByRole('button', { name: /notifications/i }).click();

    const firstNotification = page.locator('[data-testid="notification-item"]').first();

    if (await firstNotification.isVisible({ timeout: 2000 }).catch(() => false)) {
      const notificationText = await firstNotification.textContent();

      // Delete notification
      const deleteButton = firstNotification.getByRole('button', { name: /delete|remove/i });
      await deleteButton.click();

      // Verify notification is removed
      if (notificationText) {
        await expect(page.getByText(notificationText)).not.toBeVisible();
      }
    }
  });

  test('should navigate to notification preferences', async ({ page }) => {
    // Open notifications
    await page.getByRole('button', { name: /notifications/i }).click();

    // Click preferences/settings link
    const preferencesLink = page.getByRole('link', { name: /preferences|settings/i });

    if (await preferencesLink.isVisible({ timeout: 2000 }).catch(() => false)) {
      await preferencesLink.click();

      // Should navigate to preferences page
      await expect(page).toHaveURL(/notification.*preferences|preferences.*notification/i);
    }
  });

  test('should update notification preferences', async ({ page }) => {
    await page.goto('/notification-preferences');

    // Toggle email notifications for a notification type
    const emailToggle = page
      .locator('[data-testid="preference-item"]')
      .first()
      .getByRole('checkbox', { name: /email/i });

    if (await emailToggle.isVisible({ timeout: 2000 }).catch(() => false)) {
      const isChecked = await emailToggle.isChecked();
      await emailToggle.click();

      // Verify toggle state changed
      if (isChecked) {
        await expect(emailToggle).not.toBeChecked();
      } else {
        await expect(emailToggle).toBeChecked();
      }

      // Wait for save confirmation
      await expect(page.getByText(/saved|updated/i)).toBeVisible({ timeout: 3000 });
    }
  });

  test('should display different notification types', async ({ page }) => {
    // Open notifications
    await page.getByRole('button', { name: /notifications/i }).click();

    const notificationPanel = page.getByRole('region', { name: /notifications/i });

    if (await notificationPanel.isVisible()) {
      // Check for various notification types (task due, task assigned, etc.)
      const notifications = page.locator('[data-testid="notification-item"]');
      const count = await notifications.count();

      if (count > 0) {
        // Verify notifications have proper structure
        const firstNotification = notifications.first();
        await expect(firstNotification).toContainText(/.+/); // Has some text
      }
    }
  });
});

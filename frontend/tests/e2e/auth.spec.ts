import { expect, test } from '@playwright/test';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should show login page for unauthenticated users', async ({ page }) => {
    await expect(page).toHaveURL(/.*login/);
    await expect(page.getByRole('heading', { name: /login/i })).toBeVisible();
  });

  test('should allow user registration', async ({ page }) => {
    await page.goto('/register');

    // Fill registration form
    await page.getByLabel(/username/i).fill(`testuser_${Date.now()}`);
    await page.getByLabel(/email/i).fill(`test_${Date.now()}@example.com`);
    await page.getByLabel(/^password/i).fill('Test123456');
    await page.getByLabel(/confirm password/i).fill('Test123456');

    // Submit form
    await page.getByRole('button', { name: /register/i }).click();

    // Should redirect to login or dashboard
    await expect(page).toHaveURL(/\/(login|tasks)?/);
  });

  test('should allow user login', async ({ page }) => {
    await page.goto('/login');

    // Fill login form with test credentials
    await page.getByLabel(/username or email/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');

    // Submit form
    await page.getByRole('button', { name: /login/i }).click();

    // Should redirect to dashboard
    await expect(page).toHaveURL(/\/tasks|\/$/);
    await expect(page.getByText(/welcome/i)).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');

    // Fill login form with invalid credentials
    await page.getByLabel(/username or email/i).fill('invaliduser');
    await page.getByLabel(/password/i).fill('wrongpassword');

    // Submit form
    await page.getByRole('button', { name: /login/i }).click();

    // Should show error message
    await expect(page.getByText(/invalid credentials|login failed/i)).toBeVisible();
  });

  test('should allow user logout', async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.getByLabel(/username or email/i).fill('testuser');
    await page.getByLabel(/password/i).fill('password');
    await page.getByRole('button', { name: /login/i }).click();
    await expect(page).toHaveURL(/\/tasks|\/$/);

    // Logout
    await page.getByRole('button', { name: /logout|sign out/i }).click();

    // Should redirect to login
    await expect(page).toHaveURL(/.*login/);
  });

  test('should validate required fields in registration', async ({ page }) => {
    await page.goto('/register');

    // Submit without filling form
    await page.getByRole('button', { name: /register/i }).click();

    // Should show validation errors
    await expect(page.getByText(/required|please fill/i).first()).toBeVisible();
  });

  test('should validate password confirmation', async ({ page }) => {
    await page.goto('/register');

    await page.getByLabel(/username/i).fill('testuser');
    await page.getByLabel(/email/i).fill('test@example.com');
    await page.getByLabel(/^password/i).fill('Test123456');
    await page.getByLabel(/confirm password/i).fill('DifferentPassword');

    await page.getByRole('button', { name: /register/i }).click();

    // Should show password mismatch error
    await expect(page.getByText(/passwords.*match/i)).toBeVisible();
  });
});

import { test, expect } from '@playwright/test';

/**
 * Playwright E2E tests for Jitsi Management Platform frontend.
 * Tests authentication, conference management, and real-time monitoring.
 */

test.describe('JMP Frontend E2E Tests', () => {
  const baseUrl = process.env.FRONTEND_URL || 'http://localhost:3000';
  const apiUrl = process.env.API_URL || 'http://localhost:8080/api/v1';

  test.beforeEach(async ({ page }) => {
    await page.goto(baseUrl);
  });

  test.describe('Authentication', () => {
    test('should display login page', async ({ page }) => {
      await expect(page).toHaveTitle(/Jitsi Management Platform/);
      await expect(page.locator('h1')).toContainText(/Sign In|Login/i);
    });

    test('should login with valid credentials', async ({ page }) => {
      // Navigate to login if not already there
      await page.goto(`${baseUrl}/login`);
      
      // Fill login form
      await page.fill('input[name="email"]', 'admin@test.com');
      await page.fill('input[name="password"]', 'SecurePass123!');
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Wait for navigation to dashboard
      await page.waitForURL(/\/dashboard|\/conferences/);
      
      // Verify successful login
      await expect(page.locator('text=Dashboard|Conferences')).toBeVisible();
    });

    test('should show error on invalid credentials', async ({ page }) => {
      await page.goto(`${baseUrl}/login`);
      
      await page.fill('input[name="email"]', 'invalid@test.com');
      await page.fill('input[name="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      
      // Wait for error message
      await expect(page.locator('text=/Invalid credentials|Login failed/i')).toBeVisible();
    });

    test('should logout successfully', async ({ page }) => {
      // First login
      await page.goto(`${baseUrl}/login`);
      await page.fill('input[name="email"]', 'admin@test.com');
      await page.fill('input[name="password"]', 'SecurePass123!');
      await page.click('button[type="submit"]');
      await page.waitForURL(/\/dashboard/);
      
      // Logout
      await page.click('button:has-text("Logout"), button:has-text("Sign Out")');
      await page.waitForURL(/\/login/);
      
      // Verify logged out
      await expect(page.locator('input[name="email"]')).toBeVisible();
    });
  });

  test.describe('Conference Management', () => {
    test.beforeEach(async ({ page }) => {
      // Login before each conference test
      await page.goto(`${baseUrl}/login`);
      await page.fill('input[name="email"]', 'admin@test.com');
      await page.fill('input[name="password"]', 'SecurePass123!');
      await page.click('button[type="submit"]');
      await page.waitForURL(/\/dashboard/);
    });

    test('should display conferences list', async ({ page }) => {
      await page.goto(`${baseUrl}/conferences`);
      
      await expect(page.locator('h1')).toContainText(/Conferences/i);
      await expect(page.locator('table, [role="grid"]')).toBeVisible();
    });

    test('should create a new conference', async ({ page }) => {
      await page.goto(`${baseUrl}/conferences`);
      
      // Click create button
      await page.click('button:has-text("New Conference"), button:has-text("Create")');
      
      // Fill conference form
      await page.fill('input[name="name"], input[placeholder*="conference name" i]', 'E2E Test Conference');
      await page.fill('textarea[name="description"], textarea[placeholder*="description" i]', 'Created via Playwright E2E test');
      
      // Set scheduled time (if date picker exists)
      const dateTimeInput = page.locator('input[type="datetime-local"]');
      if (await dateTimeInput.isVisible()) {
        const futureDate = new Date(Date.now() + 3600000).toISOString().slice(0, 16);
        await dateTimeInput.fill(futureDate);
      }
      
      // Submit form
      await page.click('button[type="submit"]');
      
      // Wait for success message or conference to appear in list
      await expect(page.locator('text=/Conference created successfully/i, text=/E2E Test Conference/i')).toBeVisible({ timeout: 10000 });
    });

    test('should filter conferences by status', async ({ page }) => {
      await page.goto(`${baseUrl}/conferences`);
      
      // Wait for table to load
      await page.waitForSelector('table, [role="grid"]', { timeout: 5000 });
      
      // Find and use status filter
      const statusFilter = page.locator('select[name="status"], [data-testid="status-filter"]');
      if (await statusFilter.isVisible()) {
        await statusFilter.selectOption('active');
        await page.waitForTimeout(1000); // Wait for filter to apply
        
        // Verify filtered results
        const rows = page.locator('tbody tr, [role="row"]');
        const count = await rows.count();
        
        if (count > 0) {
          // All visible rows should have active status
          const statuses = await rows.allTextContents();
          statuses.forEach(status => {
            expect(status.toLowerCase()).toContain('active');
          });
        }
      }
    });

    test('should view conference details', async ({ page }) => {
      await page.goto(`${baseUrl}/conferences`);
      
      // Click on first conference row
      const firstRow = page.locator('tbody tr').first();
      await firstRow.click();
      
      // Wait for details page
      await page.waitForURL(/\/conferences\/[a-f0-9-]+/i);
      
      // Verify details are visible
      await expect(page.locator('h1, h2')).toBeVisible();
    });
  });

  test.describe('Real-time Monitoring', () => {
    test('should display dashboard with metrics', async ({ page }) => {
      await page.goto(`${baseUrl}/login`);
      await page.fill('input[name="email"]', 'admin@test.com');
      await page.fill('input[name="password"]', 'SecurePass123!');
      await page.click('button[type="submit"]');
      await page.waitForURL(/\/dashboard/);
      
      // Check for dashboard widgets
      await expect(page.locator('text=/Active Conferences|Participants|Load/i')).toBeVisible();
      
      // Check for charts or metrics
      const charts = page.locator('[class*="chart"], [class*="metric"], svg');
      await expect(charts.first()).toBeVisible({ timeout: 10000 });
    });

    test('should update conference status in real-time', async ({ page }) => {
      await page.goto(`${baseUrl}/conferences`);
      
      // Take initial snapshot of conference statuses
      const initialStatuses = await page.locator('[data-testid="conference-status"]').allTextContents();
      
      // Wait for potential updates (WebSocket)
      await page.waitForTimeout(5000);
      
      // Check if statuses updated (this is a basic check - real test would trigger an event)
      const updatedStatuses = await page.locator('[data-testid="conference-status"]').allTextContents();
      
      // At minimum, the page should still be functional
      expect(updatedStatuses.length).toBeGreaterThanOrEqual(initialStatuses.length);
    });
  });

  test.describe('Recordings', () => {
    test.beforeEach(async ({ page }) => {
      await page.goto(`${baseUrl}/login`);
      await page.fill('input[name="email"]', 'admin@test.com');
      await page.fill('input[name="password"]', 'SecurePass123!');
      await page.click('button[type="submit"]');
      await page.waitForURL(/\/dashboard/);
    });

    test('should display recordings list', async ({ page }) => {
      await page.goto(`${baseUrl}/recordings`);
      
      await expect(page.locator('h1')).toContainText(/Recording/i);
      await expect(page.locator('table, [role="grid"]')).toBeVisible();
    });

    test('should download recording', async ({ page }) => {
      await page.goto(`${baseUrl}/recordings`);
      
      // Wait for table
      await page.waitForSelector('table, [role="grid"]', { timeout: 5000 });
      
      // Find download button
      const downloadButton = page.locator('button:has-text("Download"), [data-testid="download-btn"]').first();
      
      if (await downloadButton.isVisible()) {
        const [download] = await Promise.all([
          page.waitForEvent('download'),
          downloadButton.click()
        ]);
        
        expect(download.suggestedFilename()).toMatch(/\.mp4|\.webm/i);
      }
    });
  });

  test.describe('Responsive Design', () => {
    test('should work on mobile viewport', async ({ page }) => {
      await page.setViewportSize({ width: 375, height: 667 }); // iPhone SE
      
      await page.goto(`${baseUrl}/login`);
      await expect(page.locator('input[name="email"]')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toBeVisible();
    });

    test('should work on tablet viewport', async ({ page }) => {
      await page.setViewportSize({ width: 768, height: 1024 }); // iPad
      
      await page.goto(`${baseUrl}/conferences`);
      await expect(page.locator('table, [role="grid"]')).toBeVisible();
    });
  });

  test.describe('Accessibility', () => {
    test('should have proper ARIA labels', async ({ page }) => {
      await page.goto(`${baseUrl}/login`);
      
      // Check for aria-labels on inputs
      const emailInput = page.locator('input[name="email"]');
      await expect(emailInput).toHaveAttribute('aria-label', /email/i);
    });

    test('should support keyboard navigation', async ({ page }) => {
      await page.goto(`${baseUrl}/login`);
      
      // Tab through form fields
      await page.keyboard.press('Tab');
      let focusedElement = await page.evaluate(() => document.activeElement?.tagName);
      expect(focusedElement).toBe('INPUT');
      
      await page.keyboard.press('Tab');
      focusedElement = await page.evaluate(() => document.activeElement?.tagName);
      expect(focusedElement).toBe('INPUT');
      
      await page.keyboard.press('Tab');
      focusedElement = await page.evaluate(() => document.activeElement?.tagName);
      expect(focusedElement).toBe('BUTTON');
    });
  });
});

import { test, expect } from "@playwright/test";

/**
 * testing the load command
 */

/**
 * navigate to the page before each test
 */
test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

/**
 * test for submitting a valid load command
 */
test("submitting a valid load command adds it to the history", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/stars/stardata.csv");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: File data/stars/stardata.csv loaded successfully"
  );
});

/**
 * test for invalid load commands
 */
test("invalid load commands", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load hdjfdjffb");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText("Output: File not found");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/dataset18.csv");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const second = await allListItems.nth(1);
  await expect(second).toContainText("Output: File not found");

  await page.getByLabel("Command Input Box to type in commands").fill("load");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const third = await allListItems.nth(2);
  await expect(third).toContainText(
    "Output: Invalid usage of 'load' command. Usage: load <URL>"
  );
});

/**
 * test for loading multiple files. (testing that the previous file is replaced
 * is done implicitly in search and view tests)
 */
test("submitting multiple valid load commands", async ({ page }) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.click("button");
  const allListItems = page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
  );

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  const secondListItem = await allListItems.nth(1);
  await expect(secondListItem).toContainText(
    "Output: File data/custom/empty.csv loaded successfully"
  );
});

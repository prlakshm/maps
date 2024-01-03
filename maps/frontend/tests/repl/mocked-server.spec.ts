import { test, expect } from "@playwright/test";

/**
 * testing the mock commands
 */

/**
 * navigate to the page before each test
 */
test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

//----------------------------------------------------------------------------
/**
 * test for submitting a valid mockload command with proper filepath
 */
test("submitting a valid mockload command with proper filepath", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockload data/census/dol_ri_earnings_disparity.csv");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
  );
});

/**
 * test for submitting a valid mockload command with random filepath
 */
test("submitting a valid mockload command with random filepath", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockload sdsfs");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: File sdsfs loaded successfully"
  );
});

/**
 * test for invalid mockload command
 */
test("invalid mockload command displays error", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockload hdjfdjffb fsdfdfs");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Invalid usage of 'mockload' command. Usage: mockload <URL>"
  );
});

//-----------------------------------------------------------------------------

/**
 * test for invalid mockview command
 */
test("invalid mockview command displays error", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockview hdjfdjffb");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Invalid usage of 'mockview' command. Usage: mockview"
  );
});

/**
 * test for submitting a valid mockview command
 */
test("submitting an valid mockview command displays table", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockview");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("State");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("Data Type");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText(
    "Average Weekly Earnings"
  );
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText(
    "Number of Workers"
  );
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("Earnings ");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("RI");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("White");
  await expect(tableRow2.locator("td:nth-child(4)")).toContainText(
    "395773.6521"
  );
  await expect(tableRow2.locator("td:nth-child(5)")).toContainText("$1.00");
  await expect(tableRow2.locator("td:nth-child(6)")).toContainText("75%");
});

//----------------------------------------------------------------------------

/**
 * test for submitting a valid mocksearch command with proper arguments
 */
test("submitting a valid mocksearch command with proper arguments", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mocksearch true White 1");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("RI");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("White");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText(
    '" $1,058.47 "'
  );
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText(
    "395773.6521"
  );
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("1.00");
  await expect(tableRow1.locator("td:nth-child(6)")).toContainText("75%");
});

/**
 * test for submitting a valid mocksearch command with random arguments
 */
test("submitting a valid mocksearch command with random arguments", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mocksearch i love you");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("RI");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("White");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText(
    '" $1,058.47 "'
  );
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText(
    "395773.6521"
  );
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("1.00");
  await expect(tableRow1.locator("td:nth-child(6)")).toContainText("75%");
});

/**
 * test for invalid mocksearch command
 */
test("invalid mocksearch command displays error", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mocksearch hdjfdjffb fsdfdfs");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Invalid mocksearch command. Usage: mocksearch <hasHeaders> <value> <columnId>"
  );
});

//---------------------------------------------------------------------------

/**
 * test for submitting a valid mockbroadband command with proper arguments
 */
test("submitting a valid mockbroadband command with proper arguments", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockbroadband North_Carolina Durham");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText("broadband access percent: 90");

  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockbroadband North_Carolina Orange");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const secondListItem = await allListItems.nth(1);
  await expect(secondListItem).toContainText("broadband access percent: 90");

  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("broadband North_Carolina Durham");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const thirdListItem = await allListItems.nth(2);
  await expect(thirdListItem).toContainText("broadband access percent: 90");
});

/**
 * test for submitting an invalid mockbroadband command with improper arguments
 */
test("submitting an invalid mockbroadband command with improper arguments", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockbroadband state county");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText("broadband access percent: 90");
});

/**
 * test for invalid mockbroadband command
 */
test("invalid mockbroadband command displays error", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockbroadband hdjfdjffb");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const allListItems = page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Invalid mockbroadband retrieval command. Usage: mockbroadband <state> <county>"
  );
});

//---------------------------------------------------------------------------

/**
 * test for mockview and mocksearch together
 */
test("mockview and mocksearch together", async ({ page }) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mockview");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("State");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("Data Type");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText(
    "Average Weekly Earnings"
  );
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText(
    "Number of Workers"
  );
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("Earnings ");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("RI");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("White");
  await expect(tableRow2.locator("td:nth-child(4)")).toContainText(
    "395773.6521"
  );
  await expect(tableRow2.locator("td:nth-child(5)")).toContainText("$1.00");
  await expect(tableRow2.locator("td:nth-child(6)")).toContainText("75%");

  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mocksearch i love you");
  await page.click("button");
  const table2 = await page.getByRole("table").nth(1);
  const table2Row = table2.locator("tr:nth-child(1)");
  await expect(table2Row.locator("td:nth-child(1)")).toContainText("RI");
  await expect(table2Row.locator("td:nth-child(2)")).toContainText("White");
  await expect(table2Row.locator("td:nth-child(3)")).toContainText(
    '" $1,058.47 "'
  );
  await expect(table2Row.locator("td:nth-child(4)")).toContainText(
    "395773.6521"
  );
  await expect(table2Row.locator("td:nth-child(5)")).toContainText("1.00");
  await expect(table2Row.locator("td:nth-child(6)")).toContainText("75%");
});

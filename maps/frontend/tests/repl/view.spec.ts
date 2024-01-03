import { test, expect } from "@playwright/test";

/**
 * tests for viewing CSVs
 */

// If you needed to do something before every test case...
test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
});

// /**
//  * TEST COMMENTED OUT AS IT MUST BE RUN FIRST IN THE SUITE, without any datasets previously loaded
//  * test for view errors (no dataset loaded, or invalid dataset loaded)
//  * MUST be run first out of all the tests (or with restarted server - to not store the loaded file
//  * on the backend)
//  */
// test("viewing errors when loading an invalid file or not loading", async ({
//   page,
// }) => {
//   //////////// viewing without loading first /////////////////
//   await page.getByLabel("Command Input Box to type in commands").fill("view");
//   await page.click("button");
//   await expect(
//     page.getByRole("listitem").nth(0).getByRole("paragraph")
//   ).toContainText("Output: CSV file not loaded");

//   //////////// viewing after loading invalid dataset ////////////
//   await expect(
//     page.getByLabel("Command Input Box to type in commands")
//   ).toBeVisible();
//   // dataset10 doesnt exist, so it won't be loaded
//   await page
//     .getByLabel("Command Input Box to type in commands")
//     .fill("load data/dataset10.csv");
//   await page.click("button");
//   const wholeHistory = page.locator(".repl-history ul");
//   const allListElements = wholeHistory.locator("li");
//   const count = await allListElements.count();
//   expect(count).toBe(2);
//   // view
//   await page.getByLabel("Command Input Box to type in commands").fill("view");
//   await page.click("button");
//   const wholeHistory2 = await page.locator(".repl-history ul");
//   const allListElements2 = await wholeHistory2.locator("li");
//   const divElements2 = await allListElements2.locator("div");
//   const divCount2 = await divElements2.count();
//   expect(divCount2).toBe(3);
//   // no table present in view on wrong load
//   await expect(divElements2.locator("table")).toBeHidden();
//   // check the error message
//   await expect(
//     page.getByRole("listitem").nth(2).getByRole("paragraph")
//   ).toContainText("Output: CSV file not loaded");
// });

/**
 * test for viewing a properly loaded file
 */
test("viewing a properly loaded file produces a table", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/stars/ten-star.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(0)
  ).toContainText("Output: File data/stars/ten-star.csv loaded successfully");
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("StarID");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText(
    "ProperName"
  );
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText("X");
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText("Y");
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("Z");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("Sol");
  await expect(tableRow2.locator("td:nth-child(3)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(4)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(5)")).toContainText("0");
});

/**
 * test for viewing an empty CSV
 */
test("viewing an empty CSV doesn't produce a table", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  const table = page.locator("table");
  await expect(table).toBeHidden();
  await expect(
    page.getByRole("listitem").nth(1).getByRole("paragraph")
  ).toContainText("Output: No data to display");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mode verbose");
  await page.click("button");
  const table2 = page.locator("table");
  await expect(table2).toBeHidden();
});

/**
 * test for viewing a single row CSV
 */
test("viewing a single row CSV produces a single row table", async ({
  page,
}) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/single_row.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  await expect(page.getByRole("table")).toBeVisible;
  const tableBody = page.locator("div table tbody");
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  const rows = await page.getByRole("table").locator("tr");
  await expect(rows).toHaveCount(1);

  // check the correct dataset is loaded
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("huda");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("julia");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText("partners");
});

/**
 * test for viewing a single column CSV
 */
test("viewing a single column CSV produces a single column table (same num of rows as cells)", async ({
  page,
}) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/single_column.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  const table = await page.locator("table");
  await expect(table).toBeVisible();
  const rows = await table.locator("tr");
  await expect(rows).toHaveCount(4);
  const cells = await table.locator("td");
  await expect(cells).toHaveCount(4);

  // check the correct dataset is loaded
  const tableBody = page.locator("div table tbody");
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("this");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("is");

  const tableRow3 = tableBody.locator("tr:nth-child(3)");
  await expect(tableRow3.locator("td:nth-child(1)")).toContainText("a");

  const tableRow4 = tableBody.locator("tr:nth-child(4)");
  await expect(tableRow4.locator("td:nth-child(1)")).toContainText("csv");
});

/**
 * test for loading multiple files (checking that the old file gets replaced)
 */

test("viewing different files", async ({ page }) => {
  ////////////////// load and view dataset1 ////////////////////
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/zillow.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");

  const tableBody = page.locator("div table tbody");

  // check first row
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1).toContainText("Owner");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText("Price");
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText("City");

  // check second row
  const tableRow = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(tableRow.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(tableRow.locator("td:nth-child(3)")).toContainText("New York");

  // check third row
  const tableRow2 = tableBody.locator("tr:nth-child(3)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("Bob");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("250000000");
  await expect(tableRow2.locator("td:nth-child(3)")).toContainText(
    "San Francisco"
  );

  ////////////////// load and view dataset5 ////////////////////
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/single_column.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");

  // check the correct dataset is loaded
  const tableBody2 = page.locator("div table tbody").nth(1);
  const tableRow21 = tableBody2.locator("tr:nth-child(1)");
  await expect(tableRow21.locator("td:nth-child(1)")).toContainText("this");

  const tableRow22 = tableBody2.locator("tr:nth-child(2)");
  await expect(tableRow22.locator("td:nth-child(1)")).toContainText("is");

  const tableRow23 = tableBody2.locator("tr:nth-child(3)");
  await expect(tableRow23.locator("td:nth-child(1)")).toContainText("a");

  const tableRow24 = tableBody2.locator("tr:nth-child(4)");
  await expect(tableRow24.locator("td:nth-child(1)")).toContainText("csv");

  //////////////// loading a non-existing dataset doesn't change anything //////
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/datasetNot.csv");
  await page.click("button");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");

  // check the correct dataset is loaded
  const tableBody3 = page.locator("div table tbody").nth(2);
  const tableRow31 = tableBody3.locator("tr:nth-child(1)");
  await expect(tableRow31.locator("td:nth-child(1)")).toContainText("this");

  const tableRow32 = tableBody3.locator("tr:nth-child(2)");
  await expect(tableRow32.locator("td:nth-child(1)")).toContainText("is");

  const tableRow33 = tableBody3.locator("tr:nth-child(3)");
  await expect(tableRow33.locator("td:nth-child(1)")).toContainText("a");

  const tableRow34 = tableBody3.locator("tr:nth-child(4)");
  await expect(tableRow34.locator("td:nth-child(1)")).toContainText("csv");
});

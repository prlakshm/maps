/**
 * tests for searching
 */

import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

test("after I click the button, my command gets pushed", async ({ page }) => {
  const button = page.getByRole("button");
  const initialLabelText = await button.innerText();
  const initialCommandCount = Number(initialLabelText);
  await button.click();
  const updatedLabelText = await button.innerText();
  const updatedCommandCount = Number(updatedLabelText);
  await expect(updatedCommandCount).toBe(initialCommandCount + 1);
});

// /**
//  * test for searching without loading a dataset first
//  * test commented out as it MUST be run first in the suite (data stored in server from previous
//  * tests)
// */
// test("searching for data without loading first", async ({ page }) => {
//   await page
//     .getByLabel("Command Input Box to type in commands")
//     .fill("search true Alice 1");
//   await page.click("button");
//   await expect(page.getByRole("table")).toBeHidden;
// });

/**
 * test for searching with a 1-row result
 */
test("searching for specific data in a loaded dataset", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/zillow.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Alice 0");
  await page.click("button");

  const tableBody = page.locator("div table tbody");
  const tableRow = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(tableRow.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(tableRow.locator("td:nth-child(3)")).toContainText("New York");
});
/**
 * test for multiple search results
 */
test("searching multiple results", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/dataset3.csv");
  await page.click("button");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false black 1");
  await page.click("button");

  const tableBody = page.locator("div table tbody");
  const tableRow = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow.locator("td:nth-child(1)")).toContainText("shirt");
  await expect(tableRow.locator("td:nth-child(2)")).toContainText("black");
  await expect(tableRow.locator("td:nth-child(3)")).toContainText("P&B");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("blazer");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("black");
  await expect(tableRow2.locator("td:nth-child(3)")).toContainText("H&M");
});

/**
 * test for searching single-row dataset
 */
test("searching in single-row dataset", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/single_row.csv");
  await page.click("button");
  // not specifying a column
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false huda *");
  await page.click("button");

  const tableBody = page.locator("table");
  const tableRow = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow.locator("td:nth-child(1)")).toContainText("huda");
  await expect(tableRow.locator("td:nth-child(2)")).toContainText("julia");
  await expect(tableRow.locator("td:nth-child(3)")).toContainText("partners");

  // specifying a column
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false huda 0");
  await page.click("button");

  const tableBody2 = page.locator("table").nth(1);
  const tableRow2 = tableBody2.locator("tr:nth-child(1)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("huda");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("julia");
  await expect(tableRow2.locator("td:nth-child(3)")).toContainText("partners");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search 1 huda");
  await page.click("button");
  // No results found. Check if the loaded dataset is empty
  await expect(page.getByRole("paragraph").nth(4)).toContainText(
    "Output: Invalid search command. Usage: search <hasHeaders> <value> <columnId>"
  );
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true huda 1");
  await page.click("button");
  // No results found. Check if the loaded dataset is empty
  await expect(page.getByRole("paragraph").nth(5)).toContainText(
    "Output: No data to display"
  );
});

/**
 * test for searching for data in an empty dataset
 */
test("searching for data in an empty dataset", async ({ page }) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Alice 1");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Alice *");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false 100 *");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search anything");
  await page.click("button");
  await expect(page.locator("table")).toBeHidden;
  await expect(page.getByText("Output: No data to display")).toHaveCount(3);
});

/**
 * test for searching through multiple files
 */
test("searching through multiple files", async ({ page }) => {
  ////////////// load dataset6 which is empty
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  await expect(page.locator("table")).toBeHidden;
  await expect(page.getByRole("paragraph").nth(1)).toContainText(
    "Output: File data/custom/empty.csv loaded successfully"
  );
  ////////// searching empty dataset
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true value *");
  await page.click("button");
  await expect(page.locator("table")).toBeHidden;
  await expect(page.getByRole("paragraph").nth(2)).toContainText(
    "Output: No data to display"
  );
  /// view empty dataset
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  await expect(page.getByRole("paragraph").nth(3)).toContainText(
    "Output: No data to display"
  );

  ///////// load dataset1
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/zillow.csv");
  await page.click("button");
  await expect(page.getByRole("paragraph").nth(4)).toContainText(
    "Output: File data/custom/zillow.csv loaded successfully"
  );

  /// search dataset1
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Alice 0");
  await page.click("button");
  const fifth = await page.getByRole("table");
  const tableRow = fifth.locator("tr:nth-child(1)");
  await expect(tableRow.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(tableRow.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(tableRow.locator("td:nth-child(3)")).toContainText("New York");

  ////////// load dataset6 which is empty
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true value 0");
  await page.click("button");
  await expect(page.getByRole("paragraph").nth(7)).toContainText(
    "Output: No data to display"
  );
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  await expect(page.getByRole("paragraph").nth(8)).toContainText(
    "Output: No data to display"
  );

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/zillow.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false Alice 0");
  await page.click("button");
  const table2 = await page.getByRole("table").nth(1);
  const table2Row = table2.locator("tr:nth-child(1)");
  await expect(table2Row.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(table2Row.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(table2Row.locator("td:nth-child(3)")).toContainText("New York");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false Alice *");
  await page.click("button");
  const table3 = await page.getByRole("table").nth(2);
  const table3Row = table3.locator("tr:nth-child(1)");
  await expect(table3Row.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(table3Row.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(table3Row.locator("td:nth-child(3)")).toContainText("New York");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true New_York *");
  await page.click("button");
  const table4 = await page.getByRole("table").nth(3);
  const table4Row = table4.locator("tr:nth-child(1)");
  await expect(table4Row.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(table4Row.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(table4Row.locator("td:nth-child(3)")).toContainText("New York");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false Alice 1");
  await page.click("button");
  await expect(page.getByRole("table").nth(4)).toBeHidden;
  await expect(page.getByText("Output: No data to display")).toHaveCount(5);

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false New_York *");
  await page.click("button");
  const table5 = await page.getByRole("table").nth(4);
  const table5Row = table5.locator("tr:nth-child(1)");
  await expect(table5Row.locator("td:nth-child(1)")).toContainText("Alice");
  await expect(table5Row.locator("td:nth-child(2)")).toContainText("300000000");
  await expect(table5Row.locator("td:nth-child(3)")).toContainText("New York");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search false New_York City");
  await page.click("button");
  await expect(page.getByRole("table").nth(5)).toBeHidden;
  await expect(page.getByRole("paragraph").nth(15)).toContainText(
    "Output: Cannot search non-numeric column City if no headers. Can search numeric column identifiers between 0 and 2 inclusive."
  );

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true White Data_Type");
  await page.click("button");
  await expect(page.getByRole("paragraph").nth(16)).toContainText(
    "Output: Column identifier Data Type not found. Valid column identifiers include [Owner, Price, City] and numbers between 0 and 2 inclusive."
  );

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true White Data_Type");
  await page.click("button");
  const table6 = await page.getByRole("table").nth(5);
  const table6Row = table6.locator("tr:nth-child(1)");
  await expect(table6Row.locator("td:nth-child(1)")).toContainText("RI");
  await expect(table6Row.locator("td:nth-child(2)")).toContainText("White");
});

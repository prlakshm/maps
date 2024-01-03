import { test, expect } from "@playwright/test";

/**
testing basic functionality of the app (elements appear,
can click the submit button, can push commands, etc)
and testing invalid inputs
*/

// If you needed to do something before every test case...
test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

/**
 * Don't worry about the "async" yet. We'll cover it in more detail
 * for the next sprint. For now, just think about "await" as something
 * you put before parts of your test that might take time to run,
 * like any interaction with the page.
 */
test("on page load, i see an input bar", async ({ page }) => {
  // Notice: http, not https! Our front-end is not set up for HTTPs.
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
});

test("after I type into the input box, its text changes", async ({ page }) => {
  // Step 1: Navigate to a URL
  // Step 2: Interact with the page
  // Locate the element you are looking for
  await page.getByLabel("Command Input Box to type in commands").click();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("Awesome command");

  // Step 3: Assert something about the page
  // Assertions are done by using the expect() function
  const mock_input = `Awesome command`;
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toHaveValue(mock_input);
});

test("input field for commands is functional", async ({ page }) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load_file data/dataset1.csv");
  const inputText = await page
    .getByLabel("Command Input Box to type in commands")
    .inputValue();
  await expect(inputText).toBe("load_file data/dataset1.csv");
});

test("input field for commands is functional before entering a command", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page.getByLabel("Command Input Box to type in commands").fill("");
  const inputText = await page
    .getByLabel("Command Input Box to type in commands")
    .inputValue();
  await expect(inputText).toBe("");
});

test("on page load, i see a button", async ({ page }) => {
  await expect(page.getByRole("button")).toBeVisible();
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

/**
 * test for submitting an invalid command
 */
test("submitting an invalid command adds it to the history", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("this is a test");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  await expect(page.locator(".repl-history ul li div")).toHaveText(
    'Output: Command not found: this. Input "register <commandName> <function>" to register new command'
  );
});

/**
 * test for submitting an empty command
 */
test("submitting an empty command doesn't add it to history", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page.getByLabel("Command Input Box to type in commands").fill("");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeHidden();
});

/**
 * test for submitting various commands
 */
test("submitting various commands adds the appropriate ones to history", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.click("button");
  await expect(page.locator(".repl-history ul li div")).toHaveText(
    "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
  );
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load hdjfdjffb");
  await page.click("button");
  const allListItems = page.locator(".history-element .text-box");
  const secondListItem = await allListItems.nth(1);
  await expect(secondListItem).toContainText("Output: File not found");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/dataset7.csv");
  await page.click("button");
  const thirdListItem = await allListItems.nth(2);
  await expect(secondListItem).toContainText("Output: File not found");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/dataset3.csv");
  await page.click("button");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mode verbose");
  await page.click("button");
  // await expect(page.locator(".repl-history ul li div")).toHaveText(
  // "Command: mode verbose"
  // );
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mode fhjhfhfhfh");
  await page.click("button");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const parentElement = await page.locator(".repl-history ul");
  const count = await parentElement.locator("li").count();
  expect(count).toBe(6);
});

/**
 * test for submitting a large number of commands
 */
test("submitting a large number of commands updates count", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  let updatedLabelText = "";
  for (let i = 1; i <= 30; i++) {
    const button = await page.getByRole("button");
    await page
      .getByLabel("Command Input Box to type in commands")
      .fill("load_file data/dataset3.csv");
    await button.click();
    updatedLabelText = await button.innerText();
  }
  await expect(updatedLabelText.includes("30")).toBeTruthy;
});

/**
 * test for checking the functionality of register command - MUST be run after the
 * "submitting a large number of commands (...)" test, since uses real data stored on server
 * (wouldn't work with different file loaded initially)
 */
test("registering a command works properly with load/view/search", async ({
  page,
}) => {
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load2 data/stars/ten-star.csv");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(0)
  ).toContainText(
    'Output: Command not found: load2. Input "register <commandName> <function>" to register new command'
  );
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("mode verbose");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(0)
  ).toContainText("Command: load2 data/stars/ten-star.csv");
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(1)
  ).toContainText(
    'Output: Command not found: load2. Input "register <commandName> <function>" to register new command'
  );
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  // table visible from previous load (stored in the server - must rerun for it to be hidden)
  await expect(page.getByRole("table")).toBeVisible;
  await expect(page.getByText("Sol")).toBeHidden;
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Sol 0");
  await page.click("button");
  // table visible from previous load (stored in the server - must rerun for it to be hidden)
  await expect(page.getByRole("table").nth(1)).toBeHidden;
  await expect(page.getByText("Sol")).toBeHidden;
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("register load2 handleLoad");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(4).getByRole("paragraph").nth(1)
  ).toContainText("Output: Command registered: load2");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load2 data/stars/ten-star.csv");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(5).getByRole("paragraph").nth(1)
  ).toContainText("Output: File data/stars/ten-star.csv loaded successfully");
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.click("button");
  const tableBody = page.locator("div table tbody").nth(1);
  await expect(page.getByRole("table").nth(0)).toBeVisible;
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

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("search true Sol 1");
  await page.click("button");
  await expect(page.getByRole("table").nth(2)).toBeVisible;
  const tableBody3 = page.getByRole("table").nth(2);
  const table3Row = tableBody3.locator("tr:nth-child(1)");
  await expect(table3Row.locator("td:nth-child(1)")).toContainText("0");
  await expect(table3Row.locator("td:nth-child(2)")).toContainText("Sol");
  await expect(table3Row.locator("td:nth-child(3)")).toContainText("0");
  await expect(table3Row.locator("td:nth-child(4)")).toContainText("0");
  await expect(table3Row.locator("td:nth-child(5)")).toContainText("0");

  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load data/custom/empty.csv");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(8).getByRole("paragraph").nth(1)
  ).toContainText("Output: File data/custom/empty.csv loaded successfully");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load2 data/custom/zillow.csv");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(9).getByRole("paragraph").nth(1)
  ).toContainText("Output: File data/custom/zillow.csv loaded successfully");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("register load handleView");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(10).getByRole("paragraph").nth(1)
  ).toContainText("Output: Command: load is already registered");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("register not handleNot");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(11).getByRole("paragraph").nth(1)
  ).toContainText("Output: Error executing command. ReferenceError:");
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("not test test");
  await page.click("button");
  await expect(
    page.getByRole("listitem").nth(12).getByRole("paragraph").nth(1)
  ).toContainText(
    'Output: Command not found: not. Input "register <commandName> <function>" to register new command'
  );
});

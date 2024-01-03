import { test, expect } from "@playwright/test";
import { FillLayer } from "react-map-gl";

/**
 * Tests searchareas command works
 */

/**
 * navigate to the page before each test
 */
test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

/**
 * tests that map moves when use searchareas command
 */
test("mapbox moves when searchareas is valid", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }

  //input searchareas command
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas club");
  await page.getByLabel("repl-input-submit-button").click();
  const allListItems = await page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Areas descriptions with club highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the updated map state
  const updatedMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!updatedMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Compare the initial and updated map states to check if the map has moved
  const hasMapMoved =
    initialMapState.latitude !== updatedMapState.latitude ||
    initialMapState.longitude !== updatedMapState.longitude ||
    initialMapState.zoom !== updatedMapState.zoom;

  // Assert that the map has moved
  await expect(hasMapMoved).toBe(true);
});

//----------------------------------------------------------------------------------------------------

/**
 * tests that map doesn't move with invalid searchareas command
 */
test("mapbox doesn't move when searchareas is invalid", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }

  //use searchareas command without parameters (results in error message)
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas");
  await page.getByLabel("repl-input-submit-button").click();
  const allListItems = await page.locator(".history-element .text-box");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Invalid usage of 'searchareas' command. Usage: searchareas <keyword>"
  );

  await page.waitForTimeout(5000);

  // Capture the updated map state
  const updatedMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!updatedMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Compare the initial and updated map states to check if the map has moved
  const hasMapMoved =
    initialMapState.latitude !== updatedMapState.latitude ||
    initialMapState.longitude !== updatedMapState.longitude ||
    initialMapState.zoom !== updatedMapState.zoom;

  // Assert that the map has not moved
  await expect(hasMapMoved).toBe(false);
});

//------------------------------------------------------------------------------------

/**
 * tests that map moves multiple times with multiple searchareas searches
 */
test("mapbox moves with multiple searchareas", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }

  //first use searchareas command
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas ohio");
  await page.getByLabel("repl-input-submit-button").click();
  const allListItems = await page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Areas descriptions with ohio highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the second map state
  const secondMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!secondMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Compare the initial and seond map states to check if the map has moved
  const hasMapMoved =
    initialMapState.latitude !== secondMapState.latitude ||
    initialMapState.longitude !== secondMapState.longitude ||
    initialMapState.zoom !== secondMapState.zoom;

  // Assert that the map has moved
  await expect(hasMapMoved).toBe(true);

  //second use searchareas command
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas merchant");
  await page.getByLabel("repl-input-submit-button").click();
  const secondItem = await allListItems.nth(1);
  await expect(secondItem).toContainText(
    "Output: Areas descriptions with merchant highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the third map state
  const thirdMapState = await page.locator(".map-container").evaluate((map) => {
    if (map) {
      return {
        latitude: map.getAttribute("data-lat"),
        longitude: map.getAttribute("data-long"),
        zoom: map.getAttribute("data-zoom"),
      };
    } else {
      return null;
    }
  });

  if (!thirdMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Compare the second and third map states to check if the map has moved
  const hasMapMovedAgain =
    secondMapState.latitude !== thirdMapState.latitude ||
    secondMapState.longitude !== thirdMapState.longitude ||
    secondMapState.zoom !== thirdMapState.zoom;

  // Assert that the map has moved
  await expect(hasMapMovedAgain).toBe(true);
});

//----------------------------------------------------------------------------------

/**
 * tests that map doesn't move when search same keyword
 */
test("mapbox doesn't move if same searchareas", async ({ page }) => {
  //searchareas by keyword
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas ohio");
  await page.getByLabel("repl-input-submit-button").click();
  const allListItems = await page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText(
    "Output: Areas descriptions with ohio highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }

  //searchareas same keyword
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas ohio");
  await page.getByLabel("repl-input-submit-button").click();
  const secondItem = await allListItems.nth(1);
  await expect(secondItem).toContainText(
    "Output: Areas descriptions with ohio highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the updated map state
  const updatedMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!updatedMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Define a delta value for comparing latitude, longitude, and zoom
  const tolerance = 0.000001;

  // Parse string values for latitude, longitude, and zoom to compare
  const initialLatitude =
    initialMapState.latitude !== null
      ? parseFloat(initialMapState.latitude)
      : null;
  const updatedLatitude =
    updatedMapState.latitude !== null
      ? parseFloat(updatedMapState.latitude)
      : null;

  const initialLongitude =
    initialMapState.longitude !== null
      ? parseFloat(initialMapState.longitude)
      : null;
  const updatedLongitude =
    updatedMapState.longitude !== null
      ? parseFloat(updatedMapState.longitude)
      : null;

  const initialZoom =
    initialMapState.zoom !== null ? parseFloat(initialMapState.zoom) : null;
  const updatedZoom =
    updatedMapState.zoom !== null ? parseFloat(updatedMapState.zoom) : null;

  // Compare the parsed numeric values, ignoring null values
  if (
    initialLatitude !== null &&
    updatedLatitude !== null &&
    initialLongitude !== null &&
    updatedLongitude !== null &&
    initialZoom !== null &&
    updatedZoom !== null
  ) {
    const areStatesEqual =
      Math.abs(initialLatitude - updatedLatitude) < tolerance &&
      Math.abs(initialLongitude - updatedLongitude) < tolerance &&
      Math.abs(initialZoom - updatedZoom) < tolerance;

    // Assert that the map states are equal within the specified delta
    await expect(areStatesEqual).toBe(true);
  } else {
    // Handle the case where some values were null (not valid numbers)
    throw new Error("Some map state values are null and cannot be compared.");
  }
});

//------------------------------------------------------------------------------------

/**
 * tests searchareas still moves map when used with broadband
 */
test("broadband and searchareas called in sequence", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }

  //use broadband command
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("broadband North_Carolina Durham");
  await page.getByLabel("repl-input-submit-button").click();
  const allListItems = await page.locator(".history-element");
  const firstItem = await allListItems.nth(0);
  await expect(firstItem).toContainText("broadband access percent: 90");

  //use searchareas command
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("searchareas club");
  await page.getByLabel("repl-input-submit-button").click();
  const secondItem = await allListItems.nth(1);
  await expect(secondItem).toContainText(
    "Output: Areas descriptions with club highlighted successfully"
  );

  await page.waitForTimeout(5000);

  // Capture the updated map state
  const updatedMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          latitude: map.getAttribute("data-lat"),
          longitude: map.getAttribute("data-long"),
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });

  if (!updatedMapState) {
    throw new Error(
      "Map not found or is not visible on the page after the action."
    );
  }

  // Compare the initial and updated map states to check if the map has moved
  const hasMapMoved =
    initialMapState.latitude !== updatedMapState.latitude ||
    initialMapState.longitude !== updatedMapState.longitude ||
    initialMapState.zoom !== updatedMapState.zoom;

  // Assert that the map has moved
  await expect(hasMapMoved).toBe(true);
});

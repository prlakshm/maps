import { test, expect } from "@playwright/test";
import { FillLayer } from "react-map-gl";

/**
 * Tests mapbox map component functionality
 */

/**
 * navigate to the page before each test
 */
test.beforeEach(async ({ page }) => {
  await page.goto("http://127.0.0.1:5173/");
});

/**
 * tests that map initially starts off at Providence
 */
test("mapbox starts off displaying providence", async ({ page }) => {
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

  //check long, lat, and zoom fit Providence
  await expect(initialMapState.latitude).toBe("41.825226");
  await expect(initialMapState.longitude).toBe("-71.418884");
  await expect(initialMapState.zoom).toBe("10");
});

//------------------------------------------------------------------------------------

/**
 * tests that map moves when enter long and lat into input feilds
 */
test("mapbox moves with longitude and latitude input", async ({ page }) => {
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

  // Enter latitude and longitude separately
  await expect(page.locator(".input-label").nth(0)).toContainText("Latitude:");
  await page.locator(".input-field").nth(0).fill("48.858844"); // Enter latitude

  await expect(page.locator(".input-label").nth(1)).toContainText("Longitude:");
  await page.locator(".input-field").nth(1).fill("2.294351"); // Enter longitude

  // Click the Geocode button
  await page.click(".geocode-button");

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
 * tests if zoom out button works
 */
test("mapbox zooms with zoom out button", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });
  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }
  // Click the Geocode button
  await page.click(".mapboxgl-ctrl-zoom-out");
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
  const hasMapMoved = initialMapState.zoom !== updatedMapState.zoom;
  // Assert that the map has moved
  await expect(hasMapMoved).toBe(true);
});

//------------------------------------------------------------------------------

/**
 * tests if zoom in button works
 */
test("mapbox zooms with zoom in button", async ({ page }) => {
  // Capture the initial map state
  const initialMapState = await page
    .locator(".map-container")
    .evaluate((map) => {
      if (map) {
        return {
          zoom: map.getAttribute("data-zoom"),
        };
      } else {
        return null;
      }
    });
  if (!initialMapState) {
    throw new Error("Map not found or is not visible on the page.");
  }
  // Click the Geocode button
  await page.click(".mapboxgl-ctrl-zoom-in");
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
  const hasMapMoved = initialMapState.zoom !== updatedMapState.zoom;
  // Assert that the map has moved
  await expect(hasMapMoved).toBe(true);
});

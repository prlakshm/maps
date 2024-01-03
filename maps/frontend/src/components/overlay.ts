import { FillLayer } from "mapbox-gl";
import { FeatureCollection } from "geojson";

// Import the raw JSON file containing geospatial data
import rl_data from "../geodata/fullDownload.json";

// Define a type guard function to check if the JSON is a FeatureCollection
function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

// Function to provide overlay data as a GeoJSON FeatureCollection
export function overlayData(): GeoJSON.FeatureCollection | undefined {
  return isFeatureCollection(rl_data) ? rl_data : undefined;
}

// Define the property name used for styling
const propertyName = "holc_grade";

// Define the style for the geo layer (fill layer)
export const geoLayer: FillLayer = {
  id: "geo_data", // Layer ID
  type: "fill", // Layer type (fill)
  paint: {
    "fill-color": [
      "match", // Use the "match" expression for conditional styling
      ["get", propertyName], // Get the property value "holc_grade"
      "A",
      "#5bcc04", // If "holc_grade" is "A", fill color is green
      "B",
      "#04b8cc", // If "holc_grade" is "B", fill color is blue
      "C",
      "#e9ed0e", // If "holc_grade" is "C", fill color is yellow
      "D",
      "#d11d1d", // If "holc_grade" is "D", fill color is red
      "#ccc", // Default fill color for unmatched values
    ],
    "fill-opacity": 0.2, // Set fill opacity to 0.2
  },
};

// Add a new style definition for the highlighted regions
// export const highlightLayerStyle: FillLayer = {
//   id: "highlighted_geo_data",
//   type: "fill",
//   paint: {
//     "fill-color": "#f08", // Choose a color that stands out
//     "fill-opacity": 0.75, // Make it slightly opaque
//     // Add any other paint properties you need
//   },
// };
// highlightLayerStyle example
export const highlightLayerStyle: FillLayer = {
  id: "highlighted_geo_data",
  type: "fill",
  paint: {
    "fill-color": "#ff0000", // Example: Bright red color
    "fill-opacity": 0.5, // Semi-transparent
  },
};

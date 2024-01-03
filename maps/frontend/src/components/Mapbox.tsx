// Import necessary libraries and dependencies
import Map, {
  Layer,
  LngLatBoundsLike,
  MapLayerMouseEvent,
  NavigationControl,
  Source,
  ViewStateChangeEvent,
} from "react-map-gl";
import React, {
  Dispatch,
  SetStateAction,
  useEffect,
  useRef,
  useState,
} from "react";
import { MutableRefObject } from "react";
import { MapRef } from "react-map-gl";
import { ACCESS_TOKEN } from "../private/api.js"; // Import an API access token
import "mapbox-gl/dist/mapbox-gl.css"; // Import Mapbox CSS
import "../styles/map.css"; // Import custom styles
import { geoLayer, overlayData } from "./overlay.js"; // Import custom overlay data and settings

// Define interfaces for Mapbox features and responses
interface MapboxFeature {
  place_type: string[];
  text: string;
}

interface MapboxResponse {
  features: MapboxFeature[];
}

interface MapBoxProps {
  coordinates: number[][];
  setCoordinates: Dispatch<SetStateAction<number[][]>>;
}

// Function to geocode coordinates
const coordinatesGeocoder = function (query: string) {
  // Match anything which looks like decimal degrees coordinate pair
  const matches = query.match(
    /^[ ]*(?:Lat: )?(-?\d+\.?\d*)[, ]+(?:Lng: )?(-?\d+\.?\d*)[ ]*$/i
  );
  if (!matches) {
    return null;
  }

  // Define a coordinate feature based on latitude and longitude
  function coordinateFeature(lng: number, lat: number) {
    return {
      center: [lng, lat],
      geometry: {
        type: "Point",
        coordinates: [lng, lat],
      },
      place_name: "Lat: " + lat + " Lng: " + lng,
      place_type: ["coordinate"],
      properties: {},
      type: "Feature",
    };
  }

  const coord1 = Number(matches[1]);
  const coord2 = Number(matches[2]);
  const geocodes = [];

  // Check if coordinate values are valid (latitude should be -90 to 90, longitude -180 to 180)
  if (coord1 < -90 || coord1 > 90) {
    // If the first value is outside valid latitude range, it must be a longitude
    geocodes.push(coordinateFeature(coord1, coord2));
  }

  if (coord2 < -90 || coord2 > 90) {
    // If the second value is outside valid latitude range, it must be a longitude
    geocodes.push(coordinateFeature(coord2, coord1));
  }

  if (geocodes.length === 0) {
    // If neither value is outside valid latitude range, it could be either order (lng, lat) or (lat, lng)
    geocodes.push(coordinateFeature(coord1, coord2)); // Assume (lng, lat)
    geocodes.push(coordinateFeature(coord2, coord1)); // Assume (lat, lng)
  }

  return geocodes;
};

// Initial coordinates for Providence, Rhode Island
const ProvidenceLatLong = {
  long: -71.418884,
  lat: 41.825226,
};

const initialZoom = 10;

// Main Mapbox component
function Mapbox({ coordinates, setCoordinates }: MapBoxProps) {
  // State for managing the map view
  const [viewState, setViewState] = useState({
    longitude: ProvidenceLatLong.long,
    latitude: ProvidenceLatLong.lat,
    zoom: initialZoom,
  });

  // State for managing the overlay data
  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );

  // State for managing latitude and longitude inputs
  const [inputLat, setInputLat] = useState("");
  const [inputLng, setInputLng] = useState("");

  // State for managing the highlighted coordinates
  const [highlightedCoordinates, setHighlightedCoordinates] = useState<
    GeoJSON.FeatureCollection | undefined
  >(undefined);

  // Create a ref for the longitude input element
  const latitudeInputRef = useRef<HTMLInputElement | null>(null);

  const mapRef: MutableRefObject<MapRef | null> = useRef<MapRef | null>(null);

  // Load the overlay data when the component mounts
  useEffect(() => {
    setOverlay(overlayData());
  }, []);

  // Update highlighted coordinates when the coordinates prop changes
  useEffect(() => {
    if (coordinates) {
      const coordinateFeatures: GeoJSON.Feature[] = coordinates.map(
        ([lng, lat]) => ({
          type: "Feature",
          properties: {},
          geometry: {
            type: "Point",
            coordinates: [lng, lat],
          },
        })
      );

      setHighlightedCoordinates({
        type: "FeatureCollection",
        features: coordinateFeatures,
      });
    }
  }, [coordinates]);

  useEffect(() => {
    if (coordinates) {
      const coordinateFeatures: GeoJSON.Feature[] = coordinates.map(
        ([lng, lat]) => ({
          type: "Feature",
          properties: {},
          geometry: {
            type: "Point",
            coordinates: [lng, lat],
          },
        })
      );

      setHighlightedCoordinates({
        type: "FeatureCollection",
        features: coordinateFeatures,
      });

      // Calculate the bounding box for the highlighted coordinates
      const bounds: LngLatBoundsLike = coordinates.reduce(
        (bbox, [lng, lat]) => {
          return [
            [Math.min(bbox[0][0], lng), Math.min(bbox[0][1], lat)],
            [Math.max(bbox[1][0], lng), Math.max(bbox[1][1], lat)],
          ];
        },
        [
          [180, 90], // Initial max values
          [-180, -90], // Initial min values
        ]
      );

      if (mapRef.current) {
        const map = mapRef.current;

        // Use the fitBounds method to set the map's viewport
        map.fitBounds(bounds, {
          padding: 40, // Optional padding
          maxZoom: 15, // Adjust the maximum zoom level if needed
        });
      }
    }
  }, [coordinates]);

  // Function to handle map click events
  function onMapClick(e: MapLayerMouseEvent) {
    console.log(e);
    console.log(e.lngLat.lat);
    console.log(e.lngLat.lng);
  }

  // Function to handle geocoding based on input latitude and longitude
  function handleGeocode() {
    // Parse latitude and longitude input values as numbers
    const lat = parseFloat(inputLat);
    const lng = parseFloat(inputLng);

    // Check if the values are valid numbers within the expected range
    if (
      isNaN(lat) ||
      isNaN(lng) ||
      lat < -90 ||
      lat > 90 ||
      lng < -180 ||
      lng > 180
    ) {
      alert(
        "Invalid latitude or longitude values. Please enter valid coordinates."
      );
      return;
    }

    // Use the coordinatesGeocoder function to create a GeoJSON feature
    const geocodedFeature = coordinatesGeocoder(`${lng}, ${lat}`);

    if (geocodedFeature) {
      // Update the map view state with the geocoded coordinates
      setViewState({
        longitude: lng,
        latitude: lat,
        zoom: initialZoom,
      });
    } else {
      alert("Geocoding failed. Please check your input values.");
    }
  }

  //------------------------------------------------------------------------------
  /**
   * Handles keyboard shortcut to submit by pressing Enter in long or lat box
   * @param e keyboard event of pressing Enter key
   */
  function handleEnterPress(e: React.KeyboardEvent) {
    if (e.key === "Enter") {
      handleGeocode();
    }
  }

  /**
   * Webpage always listens out for Ctrl+q to navigate cursor to longitude input box
   */
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if (e.key === "q" && e.ctrlKey) {
        // Focus on the longitude input when Ctrl+q is pressed
        if (latitudeInputRef.current) {
          latitudeInputRef.current.focus();
        }
      }
    };

    document.addEventListener("keydown", handleKeyPress);

    return () => {
      document.removeEventListener("keydown", handleKeyPress);
    };
  }, []);

  //------------------------------------------------------------------------------

  return (
    <div>
      {/* Latitude and Longitude Input Section */}
      <div className="geocode-container">
        <label className="input-label">Latitude:</label>
        <input
          type="text"
          className="input-field"
          value={inputLat}
          onChange={(e) => setInputLat(e.target.value)}
          placeholder="Enter latitude"
          onKeyDown={handleEnterPress}
          ref={latitudeInputRef}
        />
        <label className="input-label"> Longitude:</label>
        <input
          type="text"
          className="input-field"
          value={inputLng}
          onChange={(e) => setInputLng(e.target.value)}
          placeholder="Enter longitude"
          onKeyDown={handleEnterPress}
        />
        <button className="geocode-button" onClick={handleGeocode}>
          Geocode
        </button>
      </div>

      {/* Map Container */}
      <div
        className="map-container"
        data-lat={viewState.latitude}
        data-long={viewState.longitude}
        data-zoom={viewState.zoom}
      >
        <Map
          ref={mapRef}
          mapboxAccessToken={ACCESS_TOKEN}
          longitude={viewState.longitude}
          latitude={viewState.latitude}
          zoom={viewState.zoom}
          onMove={(ev: ViewStateChangeEvent) => setViewState(ev.viewState)}
          style={{ width: "100%", height: "96vh" }}
          mapStyle={"mapbox://styles/mapbox/outdoors-v12"}
          onClick={onMapClick}
        >
          <NavigationControl showZoom={true} />

          {/* Overlay Data */}
          <Source id="geo_data" type="geojson" data={overlay}>
            <Layer
              id={geoLayer.id}
              type={geoLayer.type}
              paint={geoLayer.paint}
            />
          </Source>

          {/* Render highlighted coordinates as circles on the map */}
          {highlightedCoordinates && (
            <Source
              id="highlighted_coordinates"
              type="geojson"
              data={highlightedCoordinates}
            >
              <Layer
                id="highlighted_coordinates_layer"
                type="circle"
                paint={{
                  "circle-radius": 8,
                  "circle-color": "#FF4E40", // red color for highlights
                }}
              />
            </Source>
          )}
        </Map>
      </div>
    </div>
  );
}

// Export the Mapbox component
export default Mapbox;

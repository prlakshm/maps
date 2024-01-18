// BoundaryBoxHandler.java

// This class implements the Spark `Route` interface and is responsible for handling HTTP requests related to bounding box queries.
package edu.brown.cs.student.main.server;

// Import necessary libraries and modules
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.maptypes.Feature;
import edu.brown.cs.student.main.maptypes.FeatureCollection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kotlin.Pair;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This class handles bounding box queries and implements the Spark Route interface.
 * It is responsible for processing HTTP requests related to bounding box queries and
 * providing responses with features that fall within the specified bounding box.
 * 
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class BoundaryBoxHandler implements Route {

  // List to store features that satisfy the bounding box query
  private List<Feature> successFeatures;

  // Optional caching mechanism for storing and retrieving bounding box query results
  private final Optional<LoadingCache<String, Object>> cache;
  //filepath to geojson data
  private final String filepath;

  /**
   * Constructs a BoundaryBoxHandler instance.
   *
   * @param cacheBuilder CacheBuilder for optional caching, or null to disable caching.
   */
  public BoundaryBoxHandler(String file, CacheBuilder cacheBuilder) {
    this.filepath = file;
    if (cacheBuilder == null) {
      this.cache = Optional.empty();
    } else {
      // Initialize the cache with a CacheLoader that handles cache misses
      LoadingCache<String, Object> loadingCache =
              cacheBuilder.build(
                      new CacheLoader<String, Object>() {
                        @Override
                        public Object load(String cachekey) {
                          return handleCacheMiss(successFeatures);
                        }
                      });
      this.cache = Optional.of(loadingCache);
    }
  }

  /**
   * Handles HTTP requests related to bounding box queries.
   *
   * @param request  The HTTP request.
   * @param response The HTTP response.
   * @return The response to be sent back to the client.
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      // Read the GEOJSON content from the file
      ClassLoader classLoader = getClass().getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream(this.filepath);

      //Check geojson file in resource folder
      if (inputStream == null) {
        return new LoadCsvHandler.LoadFailureResponse(
                "error_datasource", "File not found in resources: " + this.filepath, this.filepath)
                .serialize();
      }

      String jsonContent = new String(inputStream.readAllBytes());

      // Parse GeoJSON data using Moshi library
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);
      FeatureCollection collection = adapter.fromJson(jsonContent);

      List<Feature> boundaryFeatures = new ArrayList<>();

      // Extract bounding box coordinates from request parameters
      double minLat = Double.parseDouble(request.queryParams("minLat"));
      double maxLat = Double.parseDouble(request.queryParams("maxLat"));
      double minLon = Double.parseDouble(request.queryParams("minLng"));
      double maxLon = Double.parseDouble(request.queryParams("maxLng"));

      // Iterate over features and check if they are within the bounding box
      for (Feature feature : collection.getFeatures()) {
        if (feature.getGeometry() == null) {
          continue;
        }

        // Extract the coordinates of the feature's geometry
        List<List<List<List<Double>>>> coordinates = feature.getGeometry().getCoordinates();

        // Check if any coordinate point falls within the bounding box
        if (isFeatureWithinBoundingBox(coordinates, minLat, maxLat, minLon, maxLon)) {
          boundaryFeatures.add(feature);
        }
      }

      this.successFeatures = boundaryFeatures;
      String uniqueCacheKey = String.valueOf(minLat) + maxLat + minLon + maxLon;

      // Check if caching is enabled
      if (cache.isEmpty()) {
        return handleCacheMiss(boundaryFeatures);
      }
      return cache.get().get(uniqueCacheKey);
    } catch (IOException e) {
      return new BBFailureResponse("error_bad_request", "file cannot be parsed").serialize();
    } catch (NumberFormatException e) {
      return new BBFailureResponse(
              "error_bad_request", "Missing required any/all parameters: minLat, maxLat, minLng, maxLng")
              .serialize();
    } catch (Exception e) {
      return new BBFailureResponse(
              "error_bad_request", "Missing required any/all parameters: minLat, maxLat, minLng, maxLng")
              .serialize();
    }
  }

  /**
   * Handles cache misses by returning a response containing the bounding box query results.
   *
   * @param featureList The list of features within the bounding box.
   * @return The response to be sent back to the client.
   */
  private Object handleCacheMiss(List<Feature> featureList) {
    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    try {
      return new BBSuccessResponse("success", dateTimeFormatted, featureList)
              .serialize();
    } catch (Exception e) {
      return new BBFailureResponse("error_bad_request",
              "Missing required any/all parameters: minLat, maxLat, minLng, maxLng")
              .serialize();
    }
  }

  /**
   * Checks if all coordinates of a feature are within the bounding box.
   *
   * @param coordinates The coordinates of the feature.
   * @param minLat      The minimum latitude of the bounding box.
   * @param maxLat      The maximum latitude of the bounding box.
   * @param minLon      The minimum longitude of the bounding box.
   * @param maxLon      The maximum longitude of the bounding box.
   * @return True if all points are within the bounding box, false otherwise.
   */
  public boolean isFeatureWithinBoundingBox(
          List<List<List<List<Double>>>> coordinates, double minLat, double maxLat, double minLon, double maxLon) {
    for (List<List<List<Double>>> polygon : coordinates) {
      for (List<List<Double>> ring : polygon) {
        for (List<Double> point : ring) {
          double latitude = point.get(1);
          double longitude = point.get(0);

          // If any point is outside the bounding box, return false
          if (latitude < minLat || latitude > maxLat || longitude < minLon || longitude > maxLon) {
            return false;
          }
        }
      }
    }

    // If all points are within the bounding box, return true
    return true;
  }

  /**
   * Represents a failure response.
   */
  public record BBFailureResponse(String result, String error_message) {
    /**
     * Serializes the failure response to JSON.
     *
     * @return The JSON representation of the failure response.
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BBFailureResponse.class).toJson(this);
    }
  }

  /**
   * Represents a successful response.
   */
  public record BBSuccessResponse(String result, String dateTime, List<Feature> featureList) {
    /**
     * Constructs the success response.
     *
     * @param dateTime    The date and time of the response.
     * @param featureList The list of features within the bounding box.
     */
    public BBSuccessResponse(String dateTime, List<Feature> featureList) {
      this("success", dateTime, featureList);
    }

    /**
     * Serializes the success response to JSON.
     *
     * @return The JSON representation of the success response.
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BBSuccessResponse.class).toJson(this);
    }
  }

  /**
   * Gets all elements stored in the cache. If no CacheBuilder has been provided (caching disabled),
   * returns an empty list.
   *
   * @return A list of cached elements.
   */
  public List<Object> getCache() {
    List<Object> cachedElements = new ArrayList<>();
    if (cache.isPresent()) {
      Map<String, Object> cacheMap = cache.get().asMap();
      cachedElements.addAll(cacheMap.values());
    }
    return cachedElements;
  }
}
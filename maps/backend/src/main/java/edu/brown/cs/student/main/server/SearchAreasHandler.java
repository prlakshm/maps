// SearchAreasHandler.java

// This class implements the Spark `Route` interface and is responsible for handling HTTP requests related to searching areas in GEOJSON data.
package edu.brown.cs.student.main.server;

// Import necessary libraries and modules
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.maptypes.Feature;
import edu.brown.cs.student.main.maptypes.FeatureCollection;
import kotlin.Pair;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 
 * The class handles searching areas in GEOJSON data based on a provided keyword and implements the Spark Route interface.
 * 
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class SearchAreasHandler implements Route {

  // The FeatureCollection to store loaded GEOJSON data
  private FeatureCollection featureCollection;
  //filepath to geojson data
  private final String filepath;

  // Optional caching mechanism for storing and retrieving search results
  private final Optional<LoadingCache<Pair<List<List<Double>>, String>, Object>> cache;

  // Constructor for SearchAreasHandler, taking a CacheBuilder as an argument for optional caching
  public SearchAreasHandler(String file, CacheBuilder cacheBuilder) {
    this.filepath = file;
    if (cacheBuilder == null) {
      this.cache = Optional.empty();
    } else {
      // Initialize the cache with a CacheLoader that handles cache misses
      LoadingCache<Pair<List<List<Double>>, String>, Object> loadingCache =
              cacheBuilder.build(
                      new CacheLoader<Pair<List<List<Double>>, String>, Object>() {
                        @Override
                        public Object load(Pair<List<List<Double>>, String> coordinatesAndKeyword) {
                          List<List<Double>> coordinatesList = coordinatesAndKeyword.component1();
                          String keyword = coordinatesAndKeyword.component2();
                          return handleCacheMiss(coordinatesList, keyword);
                        }
                      });
      this.cache = Optional.of(loadingCache);
    }
  }

  // Implementation of the handle method required by the Spark Route interface
  @Override
  public Object handle(Request request, Response response) {
    String keyword = request.queryParams("keyword");

    if (keyword == null) {
      return new AreaFailureResponse(
              "error_bad_request", "Missing required parameter: keyword", "")
              .serialize();
    }
    try {
      String jsonContent = new String(Files.readAllBytes(Paths.get(this.filepath)));

      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);
      FeatureCollection collection = adapter.fromJson(jsonContent);
      this.featureCollection = collection;

      List<Feature> containedFeatures = new ArrayList<>();
      for (Feature feature : this.featureCollection.getFeatures()) {
        Map<String, String> descriptions = feature.properties().getArea_description_data();
        for (String description : descriptions.values()) {
          if (description.toLowerCase().contains(keyword.toLowerCase())) {
            containedFeatures.add(feature);
          }
          break;
        }
      }
      if (cache.isEmpty()) {
        return handleCacheMiss(getCoordinates(containedFeatures), keyword);
      }
      return cache.get().get(new Pair<>(new ArrayList<>(getCoordinates(containedFeatures)), new String(keyword)));
    } catch (IOException e) {
      return new AreaFailureResponse(
              "error_bad_request", "file cannot be parsed", keyword)
              .serialize();
    } catch (Exception e) {
      return new AreaFailureResponse(
              "error_bad_request", "keyword cannot be searched", keyword)
              .serialize();
    }
  }

  // Handles cache misses by returning a response containing the coordinates of the contained features
  private Object handleCacheMiss(List<List<Double>> coordinatesList, String keyword) {
    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    try {
      return new AreaSuccessResponse("success", dateTimeFormatted, coordinatesList)
              .serialize();
    } catch (Exception e) {
      return new AreaFailureResponse("error_bad_request", e.getMessage(), keyword).serialize();
    }
  }

  // Extracts and returns the coordinates from a list of features
  public List<List<Double>> getCoordinates(List<Feature> featureList) {
    List<List<Double>> allCoordinates = new ArrayList<>();

    for (Feature feature : featureList) {
      if (feature.geometry() != null) {
        List<List<List<List<Double>>>> coordinates = feature.geometry().getCoordinates();
        for (List<List<List<Double>>> polygon : coordinates) {
          for (List<List<Double>> ring : polygon) {
            for (List<Double> point : ring) {
              allCoordinates.add(point);
            }
          }
        }
      }
    }
    return allCoordinates;
  }

  // Represents a failure response for area searching
  public record AreaFailureResponse(String result, String error_message, String keyword) {
    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchAreasHandler.AreaFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /loadcsv handler, containing a result of
   * success, as well as the given filepath parameter.
   *
   * @param result the String containing "success"
   * @param coordinatesList the List containing the coordinates of the searched areas
   */
  public record AreaSuccessResponse(String result, String dateTime, List<List<Double>> coordinatesList) {
    /**
     * The constructor for the area success response record.
     *
     * @param coordinatesList the List of coordinates provided
     */
    public AreaSuccessResponse(String dateTime, List<List<Double>> coordinatesList) {
      this("success", dateTime, coordinatesList);
    }
    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchAreasHandler.AreaSuccessResponse.class).toJson(this);
    }
  }

  /**
   * Gets all elements stored in the cache. If no CacheBuilder has been provided (caching disabled),
   * returns an empty list.
   *
   * @return a list of cached elements
   */
  public List<Object> getCache() {
    List<Object> cachedElements = new ArrayList<>();
    if (cache.isPresent()) {
      Map<Pair<List<List<Double>>, String>, Object> cacheMap = cache.get().asMap();
      cachedElements.addAll(cacheMap.values());
    }
    return cachedElements;
  }
}
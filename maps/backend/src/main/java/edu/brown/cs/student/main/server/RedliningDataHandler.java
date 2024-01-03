// RedliningDataHandler.java

// This class implements the Spark `Route` interface and is responsible for handling HTTP requests related to loading and caching GEOJSON data for redlining analysis.
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
 * This class implements the Spark `Route` interface and is responsible for handling HTTP requests
 * related to loading and caching GEOJSON data for redlining analysis.
 *
 * The class handles loading and caching of GEOJSON data for redlining analysis and implements the
 * Spark Route interface.
 */
public class RedliningDataHandler implements Route {

  // The FeatureCollection to store loaded GEOJSON data
  private FeatureCollection featureCollection;

  // Optional caching mechanism for storing and retrieving GEOJSON data
  private final Optional<LoadingCache<String, Object>> cache;
  //filepath to geojson data
  private final String filepath;

  /**
   * Constructor for RedliningDataHandler, taking a CacheBuilder as an argument for optional caching.
   *
   * @param cacheBuilder The CacheBuilder for caching GEOJSON data.
   */
  public RedliningDataHandler(String file, CacheBuilder cacheBuilder) {
    this.filepath = file;
    if (cacheBuilder == null) {
      this.cache = Optional.empty();
    } else {
      // Initialize the cache with a CacheLoader that handles cache misses
      LoadingCache<String, Object> loadingCache = cacheBuilder.build(
              new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) {
                  return handleCacheMiss(featureCollection);
                }
              });
      this.cache = Optional.of(loadingCache);
    }
  }

  /**
   * Implementation of the handle method required by the Spark Route interface.
   *
   * @param request  The HTTP request object.
   * @param response The HTTP response object;
   * @return The response object containing GEOJSON data or an error message.
   */
  @Override
  public Object handle(Request request, Response response) {
    try {

      // Read the GEOJSON content from the file
      String jsonContent = new String(Files.readAllBytes(Paths.get(this.filepath)));

      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);

      // Deserialize the GEOJSON content into a FeatureCollection
      FeatureCollection collection = adapter.fromJson(jsonContent);
      this.featureCollection = collection;
      String cacheKey = "unique_cache_key";

      // Return a success response with the deserialized FeatureCollection
      if (cache.isEmpty()) {
        return handleCacheMiss(this.featureCollection);
      }
      return cache.get().get(cacheKey);
    } catch (IOException e) {
      // Handle an error when the file cannot be parsed
      return new RedliningDataHandler.RedlineFailureResponse(
              "error_bad_request", "file cannot be parsed")
              .serialize();
    } catch (Exception e) {
      // Handle an error when there is an issue in fetching GEOJSON
      return new RedliningDataHandler.RedlineFailureResponse(
              "error_bad_request", "error in fetching GEOJSON")
              .serialize();
    }
  }

  /**
   * Handles cache misses by returning a response containing the loaded GEOJSON data.
   *
   * @param featureCollection The FeatureCollection to be serialized in the response.
   * @return The serialized success response containing the loaded GEOJSON data.
   */
  private Object handleCacheMiss(FeatureCollection featureCollection) {
    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    try {
      return new RedlineSuccessResponse("success", dateTimeFormatted, featureCollection)
              .serialize();
    } catch (Exception e) {
      return new RedlineFailureResponse("error_bad_request", e.getMessage()).serialize();
    }
  }

  /**
   * Represents a failure response for GEOJSON handling.
   */
  public record RedlineFailureResponse(String result, String error_message) {
    /**
     * Serializes a failure response object.
     *
     * @return The failure response object, serialized as JSON.
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(RedliningDataHandler.RedlineFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /loadcsv handler, containing a result of
   * success, as well as the provided FeatureCollection.
   */
  public record RedlineSuccessResponse(String result, String dateTime, FeatureCollection collection) {
    /**
     * Constructor for the GEOJSON success response record.
     *
     * @param collection The FeatureCollection provided.
     */
    public RedlineSuccessResponse(String dateTime, FeatureCollection collection) {
      this("success", dateTime, collection);
    }
    /**
     * Serializes a success response object.
     *
     * @return The success response object, serialized as JSON.
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(RedliningDataHandler.RedlineSuccessResponse.class).toJson(this);
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

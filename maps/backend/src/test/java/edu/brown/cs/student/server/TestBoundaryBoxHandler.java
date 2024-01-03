// TestBoundaryBoxHandler.java

// This class contains JUnit tests for the BoundaryBoxHandler class, which handles requests related to bounding box searches.
package edu.brown.cs.student.server;

// Import necessary libraries and modules
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.BoundaryBoxHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.util.Random;

/**
 * This class contains JUnit tests for the BoundaryBoxHandler class, which handles requests
 * related to bounding box searches.
 *
 * The class contains JUnit tests for the BoundaryBoxHandler class.
 */
public class TestBoundaryBoxHandler {
  // Type definition for mapping JSON to a Map<String, Object>
  private final Type mapStringObject =
          Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  // Length of trial for fuzz testing
  final static int TRIAL_LENGTH = 100;

  //filepaths to geojsons
  final static String ACTUAL_PATH = "C:\\Users\\prana\\Documents\\GitHub\\maps-prlakshm-tbonas\\maps\\backend\\data\\geojson\\fullDownload.geojson";
  final static String MOCK_PATH = "C:\\Users\\prana\\Documents\\GitHub\\maps-prlakshm-tbonas\\maps\\backend\\data\\geojson\\mocked.geojson";

  // Setup method executed before each test
  @BeforeEach
  public void setup() {
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  // Teardown method executed after each test
  @AfterEach
  public void tearDown() {
    Spark.unmap("/boundarybox");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params.
   *
   * @param apiCall The call string, including endpoint.
   * @return The connection for the given URL, just after connecting.
   * @throws IOException If the connection fails for some reason.
   */
  private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    clientConnection.setRequestProperty("Content-Type", "application/json");
    clientConnection.setRequestProperty("Accept", "application/json");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Test for the BoundaryBoxHandler with caching mechanism.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testBoundaryBoxCache() throws IOException {
    BoundaryBoxHandler handler = new BoundaryBoxHandler(ACTUAL_PATH,
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    double minLat = 35.0;
    double maxLat = 36.0;
    double minLon = -80.0;
    double maxLon = -79.0;

    HttpURLConnection connection1 = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
            "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
            adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second call - cache checking
    HttpURLConnection connection2 = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
            "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
            adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();

    double newMinLat = 34.0;
    double newMaxLat = 35.0;
    double newMinLon = -79.0;
    double newMaxLon = -78.0;

    HttpURLConnection connection3 = tryRequest("boundarybox?minLat=" + newMinLat + "&maxLat=" + newMaxLat +
            "&minLng=" + newMinLon + "&maxLng=" + newMaxLon);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
            adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));

    assertEquals(2, handler.getCache().size());
    connection3.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler with invalid request and caching mechanism.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testBoundaryBoxCacheInvalidRequest() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    double invalidMinLat = 95.0; // Invalid latitude value

    HttpURLConnection connection = tryRequest("boundarybox?minLat=" + invalidMinLat);
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler without caching mechanism.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testBoundaryBoxNoCache() throws IOException {
    BoundaryBoxHandler handler = new BoundaryBoxHandler(ACTUAL_PATH,null);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    double minLat = 35.0;
    double maxLat = 36.0;
    double minLon = -80.0;
    double maxLon = -79.0;

    HttpURLConnection connection = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
            "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(0, handler.getCache().size());
    connection.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler with missing parameters and caching mechanism.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testBoundaryBoxMissingParameters() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    // Missing minLat parameter
    HttpURLConnection connection1 = tryRequest("boundarybox?maxLat=36.0&minLng=-80.0&maxLng=-79.0");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
            adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("error_bad_request", body1.get("result"));
    connection1.disconnect();

    // Missing maxLng parameter
    HttpURLConnection connection2 = tryRequest("boundarybox?minLat=35.0&maxLat=36.0&minLng=-80.0");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
            adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("error_bad_request", body2.get("result"));
    connection2.disconnect();

    // Missing all parameters
    HttpURLConnection connection3 = tryRequest("boundarybox");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
            adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("error_bad_request", body3.get("result"));
    connection3.disconnect();
  }

  /**
   * Test for fuzzing the BoundaryBoxHandler.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testBoundaryBoxFuzz() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    Random random = new Random();

    for (int i = 0; i < TRIAL_LENGTH; i++) {
      double minLat = getRandomCoordinate();
      double maxLat = getRandomCoordinate();
      double minLon = getRandomCoordinate();
      double maxLon = getRandomCoordinate();

      HttpURLConnection connection = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
              "&minLng=" + minLon + "&maxLng=" + maxLon);
      assertEquals(200, connection.getResponseCode());

      Map<String, Object> responseBody =
          adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

      // Check if the response body contains "success"
      assertEquals("success", responseBody.get("result"));
      connection.disconnect();
    }
  }

  //-------------------------------------------------------------------------------------------

  /**
   * Test for the BoundaryBoxHandler with caching mechanism for mocked data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testMockBoundaryBoxCache() throws IOException {
    BoundaryBoxHandler handler = new BoundaryBoxHandler(MOCK_PATH,
        CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    double minLat = 32.472256;
    double maxLat = 34.501794;
    double minLon = -87.724829;
    double maxLon = -85.773296;

    HttpURLConnection connection1 = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
        "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertNotEquals("[]", body1.get("featureList"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second call - cache checking
    HttpURLConnection connection2 = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
        "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertNotEquals("[]", body2.get("featureList"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();

    double newMinLat = 32.493384;
    double newMaxLat = 34.512401;
    double newMinLon = -89.794576;
    double newMaxLon = -84.756886;

    HttpURLConnection connection3 = tryRequest("boundarybox?minLat=" + newMinLat + "&maxLat=" + newMaxLat +
        "&minLng=" + newMinLon + "&maxLng=" + newMaxLon);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertNotEquals("[]", body3.get("featureList"));
    assertEquals(2, handler.getCache().size());
    connection3.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler with invalid request and caching mechanism for mocked data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testMockBoundaryBoxCacheInvalidRequest() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    double invalidMinLat = 95.0; // Invalid latitude value

    HttpURLConnection connection = tryRequest("boundarybox?minLat=" + invalidMinLat);
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler without caching mechanism with mocked data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testMockBoundaryBoxNoCache() throws IOException {
    BoundaryBoxHandler handler = new BoundaryBoxHandler(MOCK_PATH,null);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    double minLat = 32.472256;
    double maxLat = 34.501794;
    double minLon = -87.724829;
    double maxLon = -85.773296;

    HttpURLConnection connection = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
        "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertNotEquals("", body.get("featureList"));
    assertEquals(0, handler.getCache().size());
    connection.disconnect();
  }

  /**
   * Test for the BoundaryBoxHandler with missing parameters and caching mechanism for mocked data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testMockBoundaryBoxMissingParameters() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());

    // Missing minLat parameter
    HttpURLConnection connection1 = tryRequest("boundarybox?maxLat=36.0&minLng=-80.0&maxLng=-79.0");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("error_bad_request", body1.get("result"));
    connection1.disconnect();

    // Missing maxLng parameter
    HttpURLConnection connection2 = tryRequest("boundarybox?minLat=35.0&maxLat=36.0&minLng=-80.0");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("error_bad_request", body2.get("result"));
    connection2.disconnect();

    // Missing all parameters
    HttpURLConnection connection3 = tryRequest("boundarybox");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("error_bad_request", body3.get("result"));
    connection3.disconnect();
  }

  /**
   * Test for fuzzing the BoundaryBoxHandler for mocked data.
   *
   * @throws IOException If an I/O exception occurs.
   */
  @Test
  public void testMockBoundaryBoxFuzz() throws IOException {
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler handler = new BoundaryBoxHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/boundarybox", handler);
    Spark.awaitInitialization();

    Random random = new Random();

    for (int i = 0; i < TRIAL_LENGTH; i++) {
      double minLat = getRandomCoordinate();
      double maxLat = getRandomCoordinate();
      double minLon = getRandomCoordinate();
      double maxLon = getRandomCoordinate();

      HttpURLConnection connection = tryRequest("boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
          "&minLng=" + minLon + "&maxLng=" + maxLon);
      assertEquals(200, connection.getResponseCode());

      Map<String, Object> responseBody =
          adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

      // Check if the response body contains "success"
      assertEquals("success", responseBody.get("result"));
      connection.disconnect();
    }
  }

  //-------------------------------------------------------------------------------------------

  // Helper method to get a random coordinate value
  private double getRandomCoordinate() {
    // Generate a random coordinate between -90 and 90 (for latitude) or -180 and 180 (for longitude)
    return -90 + (180 * new Random().nextDouble());
  }


  /**
   * Helper method to show details if an error is encountered in the response body.
   *
   * @param body The response body as a Map.
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}
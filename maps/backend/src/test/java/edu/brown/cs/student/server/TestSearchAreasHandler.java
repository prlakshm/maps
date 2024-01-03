package edu.brown.cs.student.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.SearchAreasHandler;
import kotlin.Pair;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * This class contains JUnit tests for the SearchAreasHandler class.
 */
public class TestSearchAreasHandler {

  // JSON adapter for parsing response bodies
  private JsonAdapter<Map<String, Object>> adapter;

  // Number of trials for fuzz testing
  final static int TRIAL_LENGTH = 100;
  //filepaths to geojsons
  final static String ACTUAL_PATH = "C:\\Users\\prana\\Documents\\GitHub\\maps-prlakshm-tbonas\\maps\\backend\\data\\geojson\\fullDownload.geojson";
  final static String MOCK_PATH = "C:\\Users\\prana\\Documents\\GitHub\\maps-prlakshm-tbonas\\maps\\backend\\data\\geojson\\mocked.geojson";


  /**
   * Setup method executed before each test.
   */
  @BeforeEach
  public void setup() {
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
  }

  /**
   * Teardown method executed after each test.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/searchareas");
    Spark.awaitStop();
  }

  /**
   * Helper method to start a connection to a specific API endpoint/params.
   *
   * @param apiCall the call string, including endpoint.
   * @return the connection for the given URL, just after connecting.
   * @throws IOException if the connection fails for some reason.
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
   * Test for checking cache functionality in SearchAreasHandler.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testSearchAreasCache() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    SearchAreasHandler handler = new SearchAreasHandler(ACTUAL_PATH,
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // API call with a keyword
    String keyword = "merchant";
    HttpURLConnection connection1 = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
            adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second API call with the same keyword - cache checking
    HttpURLConnection connection2 = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
            adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();

    // API call with a new keyword - cache checking
    String newKeyword = "club";
    HttpURLConnection connection3 = tryRequest("searchareas?keyword=" + newKeyword);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
            adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertEquals(2, handler.getCache().size());
    connection3.disconnect();
  }

  /**
   * Test for checking invalid requests and cache behavior in SearchAreasHandler.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testSearchAreasCacheInvalidRequest() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler handler = new SearchAreasHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // Missing keyword parameter - invalid request
    HttpURLConnection connection1 = tryRequest("searchareas?keywor");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
            adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("error_bad_request", body1.get("result"));
    connection1.disconnect();

    // Empty keyword parameter - invalid request
    HttpURLConnection connection2 = tryRequest("searchareas?");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
            adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("error_bad_request", body2.get("result"));
    connection2.disconnect();
  }

  /**
   * Test for checking SearchAreasHandler without caching.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testSearchAreasNoCache() throws IOException {
    // Create a SearchAreasHandler instance with caching disabled
    SearchAreasHandler handler = new SearchAreasHandler(ACTUAL_PATH, null);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // API call with a keyword
    String keyword = "sales";
    HttpURLConnection connection = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    connection.disconnect();
  }

  /**
   * Fuzz testing for SearchAreasHandler.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testSearchAreasFuzz() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler handler = new SearchAreasHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    Random random = new Random();

    // Fuzz testing with random keywords
    for (int i = 0; i < TRIAL_LENGTH; i++) {
      String keyword = getRandomString();

      // API call with a random keyword
      HttpURLConnection connection = tryRequest("searchareas?keyword=" + keyword);
      assertEquals(200, connection.getResponseCode());

      // Parse the response body
      Map<String, Object> responseBody =
              adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

      // Check if the response body contains "success"
      assertEquals("success", responseBody.get("result"));
      connection.disconnect();
    }
  }

  //------------------------------------------------------------------------------------------

  /**
   * Test for checking cache functionality in SearchAreasHandler for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockSearchAreasCache() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    SearchAreasHandler handler = new SearchAreasHandler(MOCK_PATH,
        CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // API call with a keyword
    String keyword = "community";
    HttpURLConnection connection1 = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertNotEquals("[]", body1.get("coordinateList"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second API call with the same keyword - cache checking
    HttpURLConnection connection2 = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertNotEquals("[]", body2.get("coordinateList"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();

    // API call with a new keyword - cache checking
    String newKeyword = "rental";
    HttpURLConnection connection3 = tryRequest("searchareas?keyword=" + newKeyword);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertNotEquals("[]", body3.get("coordinateList"));
    assertEquals(2, handler.getCache().size());
    connection3.disconnect();
  }

  /**
   * Test for checking invalid requests and cache behavior in SearchAreasHandler for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockSearchAreasCacheInvalidRequest() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler handler = new SearchAreasHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // Missing keyword parameter - invalid request
    HttpURLConnection connection1 = tryRequest("searchareas?keywor");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("error_bad_request", body1.get("result"));
    connection1.disconnect();

    // Empty keyword parameter - invalid request
    HttpURLConnection connection2 = tryRequest("searchareas?");
    assertEquals(0, handler.getCache().size());
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("error_bad_request", body2.get("result"));
    connection2.disconnect();
  }

  /**
   * Test for checking SearchAreasHandler without caching for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockSearchAreasNoCache() throws IOException {
    // Create a SearchAreasHandler instance with caching disabled
    SearchAreasHandler handler = new SearchAreasHandler(MOCK_PATH, null);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    // API call with a keyword
    String keyword = "community";
    HttpURLConnection connection = tryRequest("searchareas?keyword=" + keyword);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertNotEquals("[]", body.get("coordinateList"));
    connection.disconnect();
  }

  /**
   * Fuzz testing for SearchAreasHandler for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockSearchAreasFuzz() throws IOException {
    // Create a SearchAreasHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler handler = new SearchAreasHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/searchareas", handler);
    Spark.awaitInitialization();

    Random random = new Random();

    // Fuzz testing with random keywords
    for (int i = 0; i < TRIAL_LENGTH; i++) {
      String keyword = getRandomString();

      // API call with a random keyword
      HttpURLConnection connection = tryRequest("searchareas?keyword=" + keyword);
      assertEquals(200, connection.getResponseCode());

      // Parse the response body
      Map<String, Object> responseBody =
          adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));

      // Check if the response body contains "success"
      assertEquals("success", responseBody.get("result"));
      connection.disconnect();
    }
  }


  //--------------------------------------------------------------------------------------------

  /**
   * Helper method to generate a random string.
   *
   * @return a random string of characters.
   */
  private String getRandomString() {
    // Generate a random string of length between 1 and 10
    int length = 1 + new Random().nextInt(10);
    StringBuilder randomString = new StringBuilder();

    for (int i = 0; i < length; i++) {
      char randomChar = (char) ('a' + new Random().nextInt(26));
      randomString.append(randomChar);
    }

    return randomString.toString();
  }

  /**
   * Helper method to display details if an error occurs.
   *
   * @param body the response body.
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}

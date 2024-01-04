package edu.brown.cs.student.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.RedliningDataHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class contains JUnit tests for the RedliningDataHandler class.
 * 
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class TestRedliningDataHandler {

  // JSON adapter for parsing response bodies
  private JsonAdapter<Map<String, Object>> adapter;
  //filepaths to geojsons
  final static String ACTUAL_PATH = "C:\\Users\\prana\\OneDrive\\Documents\\GitHub\\maps\\maps\\backend\\data\\geojson\\fullDownload.geojson";
  final static String MOCK_PATH = "C:\\Users\\prana\\OneDrive\\Documents\\GitHub\\maps\\maps\\backend\\data\\geojson\\mocked.geojson";

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
    Spark.unmap("/redliningdata");
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
   * Test for checking cache functionality in RedliningDataHandler.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testRedliningDataHandlerCache() throws IOException {
    // Create a RedliningDataHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler handler = new RedliningDataHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // First API call - cache checking
    HttpURLConnection connection1 = tryRequest("redliningdata");
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
            adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second API call - cache checking
    HttpURLConnection connection2 = tryRequest("redliningdata");
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
            adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();
  }

  /**
   * Test for checking RedliningDataHandler without caching.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testRedliningDataHandlerNoCache() throws IOException {
    // Create a RedliningDataHandler instance with caching disabled
    RedliningDataHandler handler = new RedliningDataHandler(ACTUAL_PATH, null);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // API call
    HttpURLConnection connection = tryRequest("redliningdata");
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(0, handler.getCache().size());
    connection.disconnect();
  }

  /**
   * Test for checking RedliningDataHandler cache miss.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testRedliningDataHandlerCacheMiss() throws IOException {
    // Create a RedliningDataHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler handler = new RedliningDataHandler(ACTUAL_PATH, cacheBuilder);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // Clear cache
    handler.getCache().clear();

    // API call - cache miss
    HttpURLConnection connection = tryRequest("redliningdata");
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
            adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(1, handler.getCache().size());
    connection.disconnect();
  }

  //--------------------------------------------------------------------------------------------


  /**
   * Test for checking cache functionality in RedliningDataHandler for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockRedliningDataHandlerCache() throws IOException {
    // Create a RedliningDataHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler handler = new RedliningDataHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // First API call - cache checking
    HttpURLConnection connection1 = tryRequest("redliningdata");
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertNotEquals("{}", body1.get("collection"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // Second API call - cache checking
    HttpURLConnection connection2 = tryRequest("redliningdata");
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertNotEquals("{}", body2.get("collection"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();
  }

  /**
   * Test for checking RedliningDataHandler without caching for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockRedliningDataHandlerNoCache() throws IOException {
    // Create a RedliningDataHandler instance with caching disabled
    RedliningDataHandler handler = new RedliningDataHandler(MOCK_PATH, null);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // API call
    HttpURLConnection connection = tryRequest("redliningdata");
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertNotEquals("{}", body.get("collection"));
    assertEquals(0, handler.getCache().size());
    connection.disconnect();
  }

  /**
   * Test for checking RedliningDataHandler cache miss for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockRedliningDataHandlerCacheMiss() throws IOException {
    // Create a RedliningDataHandler instance with caching enabled
    CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler handler = new RedliningDataHandler(MOCK_PATH, cacheBuilder);
    Spark.get("/redliningdata", handler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, handler.getCache().size());

    // Clear cache
    handler.getCache().clear();

    // API call - cache miss
    HttpURLConnection connection = tryRequest("redliningdata");
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertNotEquals("{}", body.get("collection"));
    assertEquals(1, handler.getCache().size());
    connection.disconnect();
  }

  //--------------------------------------------------------------------------------------------

  /**
   * Helper method to show details in case of an error.
   *
   * @param body the response body.
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}
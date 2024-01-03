package edu.brown.cs.student.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.BoundaryBoxHandler;
import edu.brown.cs.student.main.server.RedliningDataHandler;
import edu.brown.cs.student.main.server.SearchAreasHandler;
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

/**
 * This class contains JUnit tests for all map handlers class. This suite tests requests
 * made to each handler after one another.
 *
 * The class contains JUnit tests for the map handler called in sequence class.
 */
public class TestMultipleMapHandlers {
  // Type definition for mapping JSON to a Map<String, Object>
  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;


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
    Spark.unmap("/redlingingdata");
    Spark.unmap("/searchareas");
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
   * Test for checking cache functionality in fo map handlers used in tandem.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMultipleHandlersCache() throws IOException {
    // Create instance all handlers with caching enabled
    CacheBuilder cacheBuilder1 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler redliningDataHandler = new RedliningDataHandler(ACTUAL_PATH,
        cacheBuilder1);
    Spark.get("/redliningdata", redliningDataHandler);
    CacheBuilder cacheBuilder2 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler boundaryBoxHandler = new BoundaryBoxHandler(ACTUAL_PATH, cacheBuilder2);
    Spark.get("/boundarybox", boundaryBoxHandler);
    CacheBuilder cacheBuilder3 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler searchAreasHandler = new SearchAreasHandler(ACTUAL_PATH, cacheBuilder3);
    Spark.get("/searchareas", searchAreasHandler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, redliningDataHandler.getCache().size());
    assertEquals(0, boundaryBoxHandler.getCache().size());
    assertEquals(0, searchAreasHandler.getCache().size());

    // First API call to redliningdata - cache checking
    HttpURLConnection connection1 = tryRequest("redliningdata");
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(1, redliningDataHandler.getCache().size());
    connection1.disconnect();

    // Second API call to boundarybox - cache checking
    double minLat = 35.0;
    double maxLat = 36.0;
    double minLon = -80.0;
    double maxLon = -79.0;

    HttpURLConnection connection2 = tryRequest(
        "boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
            "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(1, boundaryBoxHandler.getCache().size());
    connection2.disconnect();

    // Third API call with to searchareas - cache checking
    String newKeyword = "club";
    HttpURLConnection connection3 = tryRequest("searchareas?keyword=" + newKeyword);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertEquals(1, searchAreasHandler.getCache().size());
    connection3.disconnect();
  }

//----------------------------------------------------------------------------------------------

  /**
   * Test for checking cache functionality in fo map handlers used in tandem for mocked data.
   *
   * @throws IOException if an I/O error occurs during the test.
   */
  @Test
  public void testMockMultipleHandlersCache() throws IOException {
    // Create instance all handlers with caching enabled
    CacheBuilder cacheBuilder1 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    RedliningDataHandler redliningDataHandler = new RedliningDataHandler(MOCK_PATH,
        cacheBuilder1);
    Spark.get("/redliningdata", redliningDataHandler);
    CacheBuilder cacheBuilder2 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    BoundaryBoxHandler boundaryBoxHandler = new BoundaryBoxHandler(MOCK_PATH, cacheBuilder2);
    Spark.get("/boundarybox", boundaryBoxHandler);
    CacheBuilder cacheBuilder3 = CacheBuilder.newBuilder().maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES);
    SearchAreasHandler searchAreasHandler = new SearchAreasHandler(MOCK_PATH, cacheBuilder3);
    Spark.get("/searchareas", searchAreasHandler);
    Spark.awaitInitialization();

    // Ensure the cache is initially empty
    assertEquals(0, redliningDataHandler.getCache().size());
    assertEquals(0, boundaryBoxHandler.getCache().size());
    assertEquals(0, searchAreasHandler.getCache().size());

    // First API call to redliningdata - cache checking
    HttpURLConnection connection1 = tryRequest("redliningdata");
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(1, redliningDataHandler.getCache().size());
    connection1.disconnect();

    // Second API call to boundarybox - cache checking
    double minLat = 35.0;
    double maxLat = 36.0;
    double minLon = -80.0;
    double maxLon = -79.0;

    HttpURLConnection connection2 = tryRequest(
        "boundarybox?minLat=" + minLat + "&maxLat=" + maxLat +
            "&minLng=" + minLon + "&maxLng=" + maxLon);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(1, boundaryBoxHandler.getCache().size());
    connection2.disconnect();

    // Third API call with to searchareas - cache checking
    String newKeyword = "club";
    HttpURLConnection connection3 = tryRequest("searchareas?keyword=" + newKeyword);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertEquals(1, searchAreasHandler.getCache().size());
    connection3.disconnect();
  }

//-----------------------------------------------------------------------------------------------

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

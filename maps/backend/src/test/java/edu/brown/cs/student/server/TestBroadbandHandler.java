package edu.brown.cs.student.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.BroadbandHandler;
import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.CensusSource;
import edu.brown.cs.student.main.sources.mocks.EchoMockCensusSource;
import edu.brown.cs.student.main.sources.mocks.StaleMockCensusSource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Integration tests: send real web requests to our server as it is running. Note that for these, we
 * prefer to avoid sending many real API requests to the NWS, and use "mocking" to avoid it. (There
 * are many other reasons to use mock data here. What are they?)
 *
 * <p>In short, there are two new techniques demonstrated here: integration testing; and testing
 * with mock data / mock objects.
 * 
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class TestBroadbandHandler {

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;
  private JsonAdapter<CensusData> broadbandDataAdapter;

  @BeforeEach
  public void setup() {
    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
    broadbandDataAdapter = moshi.adapter(CensusData.class);
  }

  @AfterEach
  public void tearDown() {
    Spark.unmap("/broadband");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (Note: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
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
   * Test case to verify a successful /broadband request with an echo mock CensusSource. Checks the
   * accuracy of the returned broadband access percentage (set - 80.0%) and the time of data
   * retrieval.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testEchoBroadbandRequestSuccess() throws IOException {
    CensusSource mockedSource = new EchoMockCensusSource(new CensusData(80.0));
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carolina&county=Durham";
    HttpURLConnection loadConnection = tryRequest("broadband?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(80.0, body.get("broadband_access_percent"));

    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    assertEquals(dateTimeFormatted, body.get("date_time"));
    loadConnection.disconnect();
  }

  /**
   * Test case to verify a successful /broadband request using a stale mock CensusSource. Checks
   * whether the response body contains the expected broadband access percentage (values differ
   * among states) and appropriate time of data retrieval.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testStaleBroadbandRequestSuccess() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carolina&county=Orange";
    HttpURLConnection loadConnection = tryRequest("broadband?" + params);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(89.1, body.get("broadband_access_percent"));

    Date today = new Date();
    Long now = today.getTime();
    String dateTimeFormatted = new SimpleDateFormat("MM/dd/yyyy HH:mm").format(now);
    assertEquals(dateTimeFormatted, body.get("date_time"));
    loadConnection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with a missing "state" parameter using
   * an echo mock CensusSource. Checks whether the response body contains an error result and
   * message indicating the missing "state" parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testEchoBroadbandMissingState() throws IOException {
    CensusSource mockedSource = new EchoMockCensusSource(new CensusData(80.0));
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();
    String params = "county=Durham";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Required parameter missing: state", body.get("error_message"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with a missing "county" parameter
   * using a stale mock CensusSource. Checks whether the response body contains an error result and
   * message indicating the missing "state" parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testStaleBroadbandMissingState() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();
    String params = "county=Iredell";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Required parameter missing: state", body.get("error_message"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with a missing "state" parameter using
   * an echo mock CensusSource. Checks whether the response body contains an error result and
   * message indicating the missing "county" parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testEchoBroadbandMissingCounty() throws IOException {
    CensusSource mockedSource = new EchoMockCensusSource(new CensusData(80.0));
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carolina";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Required parameter missing: county", body.get("error_message"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with a missing "state" parameter using
   * a stale mock CensusSource. Checks whether the response body contains an error result and
   * message indicating the missing "county" parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testStaleBroadbandMissingCounty() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carolina";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Required parameter missing: county", body.get("error_message"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with an invalid "state" parameter.
   * Checks whether the response body contains an error result indicating the invalid "state"
   * parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testBroadbandInvalidState() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carloina&county=Durham";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with an invalid "county" parameter.
   * Checks whether the response body contains an error result indicating the invalid "county"
   * parameter.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testBroadbandInvalidCounty() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=North%20Carolina&county=Ornage";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test case to verify the handling of a /broadband request with empty "state" and "county"
   * parameters. Checks whether the response body contains an error result indicating the empty
   * parameters.
   *
   * @throws IOException if an I/O error occurs while making the HTTP request
   */
  @Test
  public void testBroadbandEmptyStateEmptyCounty() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    Spark.get("/broadband", new BroadbandHandler(mockedSource, null));
    Spark.awaitInitialization();

    String params = "state=&county=";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test case to verify the caching behavior of the BroadbandHandler when a CacheBuilder is
   * provided. Ensures that the cache is of the same size after sending requests with equal
   * parameters, and increases with a requests for keys not yet cached.
   *
   * @throws IOException if an I/O error occurs while making the HTTP requests
   */
  @Test
  public void testBroadbandCache() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    BroadbandHandler handler =
        new BroadbandHandler(
            mockedSource,
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/broadband", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());
    String params = "state=North%20Carolina&county=Durham";
    HttpURLConnection connection1 = tryRequest("broadband?" + params);
    assertEquals(200, connection1.getResponseCode());
    Map<String, Object> body1 =
        adapter.fromJson(new Buffer().readFrom(connection1.getInputStream()));
    showDetailsIfError(body1);
    assertEquals("success", body1.get("result"));
    assertEquals(90.0, body1.get("broadband_access_percent"));
    assertEquals(1, handler.getCache().size());
    connection1.disconnect();

    // second call - cache checking
    HttpURLConnection connection2 = tryRequest("broadband?" + params);
    assertEquals(200, connection2.getResponseCode());
    Map<String, Object> body2 =
        adapter.fromJson(new Buffer().readFrom(connection2.getInputStream()));
    showDetailsIfError(body2);
    assertEquals("success", body2.get("result"));
    assertEquals(90.0, body2.get("broadband_access_percent"));
    assertEquals(1, handler.getCache().size());
    connection2.disconnect();

    String params2 = "state=North%20Carolina&county=Orange";
    HttpURLConnection connection3 = tryRequest("broadband?" + params2);
    assertEquals(200, connection3.getResponseCode());
    Map<String, Object> body3 =
        adapter.fromJson(new Buffer().readFrom(connection3.getInputStream()));
    showDetailsIfError(body3);
    assertEquals("success", body3.get("result"));
    assertEquals(89.1, body3.get("broadband_access_percent"));
    assertEquals(2, handler.getCache().size());
    connection3.disconnect();
  }

  @Test
  public void testBroadbandCacheInvalidRequest() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    BroadbandHandler handler =
        new BroadbandHandler(
            mockedSource,
            CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.MINUTES));
    Spark.get("/broadband", handler);
    Spark.awaitInitialization();

    assertEquals(0, handler.getCache().size());
    String params = "state=Invalid%20Carolina&county=Durham";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(0, handler.getCache().size());

    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    connection.disconnect();
  }

  /**
   * Test case to verify the behavior of the BroadbandHandler when no CacheBuilder is provided,
   * ensuring that caching is disabled by checkign the size.
   *
   * @throws IOException if an I/O error occurs while making the HTTP requests
   */
  @Test
  public void testBroadbandNoCache() throws IOException {
    CensusSource mockedSource = new StaleMockCensusSource();
    BroadbandHandler handler = new BroadbandHandler(mockedSource, null);
    Spark.get("/broadband", handler);
    Spark.awaitInitialization();

    String params = "state=North%20Carolina&county=Pitt";
    HttpURLConnection connection = tryRequest("broadband?" + params);
    assertEquals(200, connection.getResponseCode());
    Map<String, Object> body = adapter.fromJson(new Buffer().readFrom(connection.getInputStream()));
    showDetailsIfError(body);

    assertEquals("success", body.get("result"));
    assertEquals(75.5, body.get("broadband_access_percent"));
    assertEquals(0, handler.getCache().size());
    connection.disconnect();
  }

  // add to check if cache is still empty after invalid  request
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("type") && "error".equals(body.get("type"))) {
      System.out.println(body);
    }
  }
}

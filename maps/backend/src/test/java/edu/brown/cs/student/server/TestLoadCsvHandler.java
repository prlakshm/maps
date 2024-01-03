package edu.brown.cs.student.server;

import static org.testng.AssertJUnit.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.CsvDataWrapper;
import edu.brown.cs.student.main.server.LoadCsvHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * INTEGRATION TEST: sending real web requests to our server as it is running, and seeing how the
 * pieces of the project, from the handlers to the sources, work together.
 *
 * <p>This is a testing class that tests the LoadCsvHandler class. LoadCsvHandler handles /loadcsv
 * requests to our Server. This class tests many cases for how this /loadcsv can occur, with
 * different kinds of files loaded, missing parameters, and more.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestLoadCsvHandler {

  /** Sets up the Spark port at any open port, once for the entire testing suite. */
  @BeforeAll
  public static void setupOnce() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * This method reinitializes the /loadcsv endpoint, and well as the handler, csvData object, and
   * adapter for every test.
   */
  @BeforeEach
  public void setup() {
    CsvDataWrapper csvData = new CsvDataWrapper(new ArrayList<>(), false);
    LoadCsvHandler handler = new LoadCsvHandler(csvData);
    Spark.get("/loadcsv", handler);
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  /**
   * This method gracefully stops the endpoint after every test, and does not proceed until the
   * server has stopped.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/loadcsv");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint with query parameters.
   *
   * @param apiCall the call string, including endpoint
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
   * This test verifies that the /loadcsv endpoint can successfully load a CSV file that exists in
   * the data directory.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testRequestSuccess() throws IOException {
    String validFilepath = "data/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + validFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(validFilepath, body.get("filepath"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint can successfully load a CSV file that exists in
   * the data directory, but with an extra ./ at the beginning of the file path.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testRequestSuccessDotSlash() throws IOException {
    String validFilepath = "./data/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + validFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(validFilepath, body.get("filepath"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint can successfully load an empty CSV file that
   * exists in the data directory.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadEmptyFile() throws IOException {
    String emptyFilepath = "./data/custom/empty.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("success", body.get("result"));
    assertEquals(emptyFilepath, body.get("filepath"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint will return an object representing a failure
   * response if the given filepath, although in the data directory, does not exist.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testNonexistentFile() throws IOException {
    String emptyFilepath = "./data/custom/fake_file.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_datasource", body.get("result"));
    assertEquals("File not found", body.get("error_message"));
    assertEquals(emptyFilepath, body.get("filepath"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint will return an object representing a failure
   * response if the request has no filepath parameter.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testMissingParameter() throws IOException {
    HttpURLConnection loadConnection = tryRequest("loadcsv?");
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Missing required parameter: filepath", body.get("error_message"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint will return a failure response if the request has
   * a misspelled filepath parameter (given filePath, but expecting filepath).
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testMisspelledParameter() throws IOException {
    String validFilepath = "./data/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filePath" + validFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_bad_request", body.get("result"));
    assertEquals("Missing required parameter: filepath", body.get("error_message"));
    loadConnection.disconnect();
  }

  /**
   * This test verifies that the /loadcsv endpoint will return a failure response if the request
   * tries to access a file in a directory other than data.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testInvalidFilepath() throws IOException {
    String invalidFilepath = "./src/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + invalidFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> body =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    showDetailsIfError(body);
    assertEquals("error_datasource", body.get("result"));
    assertEquals("File not found", body.get("error_message"));
    assertEquals("./src/census/dol_ri_earnings_disparity.csv", body.get("filepath"));
    loadConnection.disconnect();
  }

  /**
   * This helper method prints the details of an error message if the response body contains the
   * String "error" in the result field.
   *
   * @param body a map representing a json response object
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("result") && "error".equals(body.get("result"))) {
      System.out.println(body.toString());
    }
  }
}

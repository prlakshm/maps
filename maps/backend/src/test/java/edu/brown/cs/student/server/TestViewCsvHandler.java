package edu.brown.cs.student.server;

import static org.testng.AssertJUnit.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.CsvDataWrapper;
import edu.brown.cs.student.main.server.LoadCsvHandler;
import edu.brown.cs.student.main.server.ViewCsvHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * INTEGRATION TEST: sending real web requests to our server as it is running, and seeing how the
 * pieces of the project, from the handlers to the sources, work together.
 *
 * <p>This is a testing class that tests the ViewCsvHandler class. ViewCsvHandler handles /viewcsv
 * requests to our Server. This class tests many cases for how combiantions of /loadcsv and /viewcsv
 * can occur, as well as different kinds of files loaded, missing parameters, and more.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestViewCsvHandler {
  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * This method reinitializes the /loadcsv and /viewcsv endpoints, and well as the handler, csvData
   * object, and adapter for every test.
   */
  @BeforeEach
  public void setup() {
    CsvDataWrapper csvData = new CsvDataWrapper(new ArrayList<>(), false);
    Spark.get("/loadcsv", new LoadCsvHandler(csvData));
    Spark.get("/viewcsv", new ViewCsvHandler(csvData));
    Spark.awaitInitialization();

    Moshi moshi = new Moshi.Builder().build();
    adapter = moshi.adapter(mapStringObject);
  }

  /**
   * This method gracefully stops the endpoints after every test, and does not proceed until the
   * server has stopped.
   */
  @AfterEach
  public void tearDown() {
    Spark.unmap("/loadcsv");
    Spark.unmap("/viewcsv");
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
   * This test verifies that if /viewcsv is called without any call to /loadcsv occuring, that the
   * handler returns a failure response object with a proper error code and message.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadedNoFile() throws IOException {
    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> viewBody =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    showDetailsIfError(viewBody);
    assertEquals("error_not_loaded", viewBody.get("result"));
    assertEquals("CSV file not loaded", viewBody.get("error_message"));
    viewConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called, but fails, and then /viewcsv is called, that the
   * view handler returns a failure response object with a proper error code and message.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadFailsBeforeView() throws IOException {
    String emptyFilepath = "src/main/empty.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error_datasource", loadBody.get("result"));
    assertEquals("File not found", loadBody.get("error_message"));

    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> viewBody =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    showDetailsIfError(viewBody);
    assertEquals("error_not_loaded", viewBody.get("result"));
    assertEquals("CSV file not loaded", viewBody.get("error_message"));
    viewConnection.disconnect();
  }

  /**
   * This test verifies that if one CSV file is loaded with /loadcsv, and then a different file is
   * loaded with /loadcsv, that when /viewcsv is called, the second file loaded is viewed.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadFirstLoadSecondView() throws IOException {
    String filepath = "data/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    filepath = "data/stars/ten-star.csv";
    HttpURLConnection loadSecondConnection = tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadSecondBody =
        adapter.fromJson(new Buffer().readFrom(loadSecondConnection.getInputStream()));
    assertEquals("success", loadSecondBody.get("result"));

    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> viewBody =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    showDetailsIfError(viewBody);
    List<List<String>> expectedData =
        List.of(
            List.of("StarID", "ProperName", "X", "Y", "Z"),
            List.of("0", "Sol", "0", "0", "0"),
            List.of("1", "", "282.43485", "0.00449", "5.36884"),
            List.of("2", "", "43.04329", "0.00285", "-15.24144"),
            List.of("3", "", "277.11358", "0.02422", "223.27753"),
            List.of("3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"),
            List.of("70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"),
            List.of("71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"),
            List.of("71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"),
            List.of("87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"),
            List.of("118721", "", "-2.28262", "0.64697", "0.29354"));
    assertEquals(expectedData, viewBody.get("data"));
    viewConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called to load an empty CSV file, and then /viewcsv is
   * called, that the view handler returns a success object with a data field containing an empty
   * list of rows.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadedEmptyFile() throws IOException {
    String emptyFilepath = "data/custom/empty.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> viewBody =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    showDetailsIfError(viewBody);
    assertEquals("success", viewBody.get("result"));
    assertEquals(List.of(), viewBody.get("data"));
    viewConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called to load a full CSV file, and then /viewcsv is
   * called, that the view handler returns a success object with a data field containing all the
   * rows of loaded CSV data.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadedFilledFile() throws IOException {
    String validFilepath = "data/census/dol_ri_earnings_disparity.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + validFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection viewConnection = tryRequest("viewcsv");
    assertEquals(200, viewConnection.getResponseCode());
    Map<String, Object> viewBody =
        adapter.fromJson(new Buffer().readFrom(viewConnection.getInputStream()));
    showDetailsIfError(viewBody);
    List<List<String>> expectedData =
        List.of(
            List.of(
                "State",
                "Data Type",
                "Average Weekly Earnings",
                "Number of Workers",
                "Earnings Disparity",
                "Employed Percent"),
            List.of("RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"),
            List.of("RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"),
            List.of(
                "RI",
                "Native American/American Indian",
                " $471.07 ",
                "2315.505646",
                " $0.45 ",
                "0%"),
            List.of(
                "RI", "Asian-Pacific Islander", "\" $1,080.09 \"", "18956.71657", " $1.02 ", "4%"),
            List.of("RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"),
            List.of("RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"));
    assertEquals("success", viewBody.get("result"));
    assertEquals(expectedData, viewBody.get("data"));
    viewConnection.disconnect();
  }

  /**
   * This helper method prints the details of an error message if the response body contains the
   * String "error" in the result field.
   *
   * @param body a map representing a json response object
   */
  private void showDetailsIfError(Map<String, Object> body) {
    if (body.containsKey("result") && "error".equals(body.get("result"))) {
      System.out.println(body);
    }
  }
}

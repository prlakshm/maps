package edu.brown.cs.student.server;

import static org.testng.AssertJUnit.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.CsvDataWrapper;
import edu.brown.cs.student.main.server.LoadCsvHandler;
import edu.brown.cs.student.main.server.SearchCsvHandler;
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
 * <p>This is a testing class that tests the SearchCsvHandler class. SearchCsvHandler handles
 * /searchcsv requests to our Server. This class tests many cases for how this /searchcsv can occur,
 * with different combinations of /loadcsv and /viewcsv calls preceding, and different search
 * criteria.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestSearchCsvHandler {
  private final Type mapStringObject =
      Types.newParameterizedType(Map.class, String.class, Object.class);
  private JsonAdapter<Map<String, Object>> adapter;

  /**
   * This method reinitializes the /loadcsv, /viewcsv, and /searchcsv endpoints, and well as the
   * handler, csvData object, and adapter for every test.
   */
  @BeforeEach
  public void setup() {
    CsvDataWrapper csvData = new CsvDataWrapper(new ArrayList<>(), false);
    Spark.get("/loadcsv", new LoadCsvHandler(csvData));
    Spark.get("/viewcsv", new ViewCsvHandler(csvData));
    Spark.get("/searchcsv", new SearchCsvHandler(csvData));
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
    Spark.unmap("/searchcsv");
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
   * This test verifies that if /searchcsv is called (with no parameters) before any CSV file has
   * been loaded by a successful call to /loadcsv, the handler returns an error response object.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedNoFileNoParameters() throws IOException {
    HttpURLConnection searchConnection = tryRequest("searchcsv");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_not_loaded", searchBody.get("result"));
    assertEquals("CSV file not loaded", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /searchcsv is called (with parameters) before any CSV file has been
   * loaded by a successful call to /loadcsv, the handler returns an error response object.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedNoFileWithParameters() throws IOException {
    HttpURLConnection searchConnection = tryRequest("searchcsv?value=White&headers=true");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_not_loaded", searchBody.get("result"));
    assertEquals("CSV file not loaded", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called, but fails, and then /searchcsv is called (with
   * parameters), that the search handler returns a failure response object with a proper error code
   * and message.
   *
   * @throws IOException
   */
  @Test
  public void testLoadFailsBeforeSearch() throws IOException {
    String emptyFilepath = "src/main/empty.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error_datasource", loadBody.get("result"));
    assertEquals("File not found", loadBody.get("error_message"));

    HttpURLConnection searchConnection = tryRequest("searchcsv?value=White&headers=true");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_not_loaded", searchBody.get("result"));
    assertEquals("CSV file not loaded", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that specify that there are no headers, but includes no search parameters, that the
   * search handler returns a failure response object with a descriptive error code and message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedMissingAllParameters() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection = tryRequest("searchcsv?");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals(
        "Required parameters missing: value and hasHeaders", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that specify that there are no headers, but includes no value parameter, that the
   * search handler returns a failure response object with a descriptive error code and message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedMissingHasHeadersParameter() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection = tryRequest("searchcsv?value=White");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals("Required parameter missing: headers", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that specify that there are no headers, but includes no headers parameter, that the
   * search handler returns a failure response object with a descriptive error code and message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedMissingValueParameter() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection = tryRequest("searchcsv?headers=true");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals("Required parameter missing: value", searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that specify that there are no headers, but asks to search a non-numeric column,
   * that the search handler returns a failure response object with a descriptive error code and
   * message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedFileHeadersFalseError() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Bristol&headers=false&colid=City/Town");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals(
        "Cannot search non-numeric column City/Town if no headers. "
            + "Can search numeric column identifiers between 0 and 3 inclusive.",
        searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that asks to search a non-numeric column header that does not exist, that the search
   * handler returns a failure response object with a descriptive error code and message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedFileNonexistentHeader() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Bristol&headers=true&colid=CityTown");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals(
        "Column identifier CityTown not found. Valid column identifiers include "
            + "[City/Town, Median Household Income , Median Family Income, Per Capita Income] "
            + "and numbers between 0 and 3 inclusive.",
        searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and it fails, but then /loadcsv is
   * called again, and succeeds, and then /searchcsv is called, that /searchcsv is successful and
   * finds a matching row.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedFailLoadSucceedsSearch() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_202.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("error_datasource", loadBody.get("result"));
    assertEquals("File not found", loadBody.get("error_message"));
    assertEquals(
        "data/census/ri_city_town_income_us_census_2017_202.csv", loadBody.get("filepath"));

    String filepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection secondLoadConnection = tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> secondLoadBody =
        adapter.fromJson(new Buffer().readFrom(secondLoadConnection.getInputStream()));
    assertEquals("success", secondLoadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Bristol&headers=true&colid=City/Town");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    List<List<String>> expectedData =
        List.of(List.of("Bristol", "\"80,727.00\"", "\"115,740.00\"", "\"42,658.00\""));
    assertEquals("success", searchBody.get("result"));
    assertEquals(expectedData, searchBody.get("data"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if one CSV file is loaded with /loadcsv, and then a different file is
   * loaded with /loadcsv, that when /searchcsv is called, the second file loaded is searched. Then,
   * a new file is loaded, and this test verifies that this third file is searched when another call
   * to /searchcsv is made.
   *
   * @throws IOException if the CSV parsing done by the handler's call to CsvParser fails
   */
  @Test
  public void testLoadFirstLoadSecondSearch() throws IOException {
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

    HttpURLConnection searchConnection = tryRequest("searchcsv?value=Sol&headers=false");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    List<List<String>> expectedData = List.of(List.of("0", "Sol", "0", "0", "0"));
    assertEquals(expectedData, searchBody.get("data"));
    searchConnection.disconnect();

    filepath = "data/stars/stardata.csv";
    HttpURLConnection loadThirdConnection = tryRequest("loadcsv?filepath=" + filepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadThirdBody =
        adapter.fromJson(new Buffer().readFrom(loadThirdConnection.getInputStream()));
    assertEquals("success", loadThirdBody.get("result"));

    HttpURLConnection secondSearchConnection = tryRequest("searchcsv?value=Andreas&headers=false");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> secondSearchBody =
        adapter.fromJson(new Buffer().readFrom(secondSearchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    expectedData = List.of(List.of("1", "Andreas", "282.43485", "0.00449", "5.36884"));
    assertEquals(expectedData, secondSearchBody.get("data"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that asks to search a non-numeric column header that is out of range, that the
   * search handler returns a failure response object with a descriptive error code and message.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedFileOutOfRangeId() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Bristol&headers=true&colid=10");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("error_bad_request", searchBody.get("result"));
    assertEquals(
        "Column index 10 not found. Can search numeric column identifiers "
            + "between 0 and 3 inclusive.",
        searchBody.get("error_message"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on an empty CSV file, and then /searchcsv if
   * called with parameters, the handler returns a success response object with a data field holding
   * an empty list of found rows.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testSearchEmptyFile() throws IOException {
    String emptyFilepath = "data/custom/empty.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection = tryRequest("searchcsv?value=White&headers=true");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("success", searchBody.get("result"));
    assertEquals(List.of(), searchBody.get("data"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on a CSV file, and then /searchcsv if called with
   * parameters that specify the wrong column to search for a target value, that the handler returns
   * a success response object with a data field containing an empty list of found rows.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadedFileNothingFoundWrongIndex() throws IOException {
    String emptyFilepath = "data/census/ri_city_town_income_us_census_2017_2021.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=Bristol&headers=false&colid=1");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("success", searchBody.get("result"));
    assertEquals(List.of(), searchBody.get("data"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on an empty CSV file, and then /viewcsv is
   * called, that all rows are returned in the success object by the view handler, and that when
   * /searchcsv is called with parameter, the search handler returns a success response object
   * containing only the rows of CSV data that fit the search criteria (with a space in colid!!).
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadSearchFindRows() throws IOException {
    String emptyFilepath = "data/census/postsecondary_education.csv";
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=" + emptyFilepath);
    assertEquals(200, loadConnection.getResponseCode());
    Map<String, Object> loadBody =
        adapter.fromJson(new Buffer().readFrom(loadConnection.getInputStream()));
    assertEquals("success", loadBody.get("result"));

    HttpURLConnection searchConnection =
        tryRequest("searchcsv?value=White&headers=true&colid=IPEDS%20Race");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("success", searchBody.get("result"));
    assertEquals(
        List.of(
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "691",
                "brown-university",
                "0.223552248",
                "Men",
                "1"),
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "660",
                "brown-university",
                "0.213523132",
                "Women",
                "2")),
        searchBody.get("data"));
    searchConnection.disconnect();
  }

  /**
   * This test verifies that if /loadcsv is called on an empty CSV file, and then /viewcsv is
   * called, that all rows are returned in the success object by the view handler, and that when
   * /searchcsv is called with parameter, the search handler returns a success response object
   * containing only the rows of CSV data that fit the search criteria.
   *
   * @throws IOException if the CSV searching done by the handler's call to CsvSearcher fails
   */
  @Test
  public void testLoadViewSearchFindRows() throws IOException {
    String emptyFilepath = "data/census/postsecondary_education.csv";
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
    List<List<String>> expectedData =
        List.of(
            List.of(
                "IPEDS Race",
                "ID Year",
                "Year",
                "ID University",
                "University",
                "Completions",
                "Slug University",
                "share",
                "Sex",
                "ID Sex"),
            List.of(
                "Asian",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "214",
                "brown-university",
                "0.069233258",
                "Men",
                "1"),
            List.of(
                "Black or African American",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "77",
                "brown-university",
                "0.024911032",
                "Men",
                "1"),
            List.of(
                "Native Hawaiian or Other Pacific Islanders",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "3",
                "brown-university",
                "0.00097056",
                "Men",
                "1"),
            List.of(
                "Hispanic or Latino",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "143",
                "brown-university",
                "0.046263345",
                "Men",
                "1"),
            List.of(
                "Two or More Races",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "58",
                "brown-university",
                "0.018764154",
                "Men",
                "1"),
            List.of(
                "American Indian or Alaska Native",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "4",
                "brown-university",
                "0.00129408",
                "Men",
                "1"),
            List.of(
                "Non-resident Alien",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "327",
                "brown-university",
                "0.105791006",
                "Men",
                "1"),
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "691",
                "brown-university",
                "0.223552248",
                "Men",
                "1"),
            List.of(
                "Asian",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "235",
                "brown-university",
                "0.076027176",
                "Women",
                "2"),
            List.of(
                "Black or African American",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "95",
                "brown-university",
                "0.03073439",
                "Women",
                "2"),
            List.of(
                "Native Hawaiian or Other Pacific Islanders",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "4",
                "brown-university",
                "0.00129408",
                "Women",
                "2"),
            List.of(
                "Hispanic or Latino",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "207",
                "brown-university",
                "0.066968619",
                "Women",
                "2"),
            List.of(
                "Two or More Races",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "85",
                "brown-university",
                "0.027499191",
                "Women",
                "2"),
            List.of(
                "American Indian or Alaska Native",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "7",
                "brown-university",
                "0.002264639",
                "Women",
                "2"),
            List.of(
                "Non-resident Alien",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "281",
                "brown-university",
                "0.090909091",
                "Women",
                "2"),
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "660",
                "brown-university",
                "0.213523132",
                "Women",
                "2"));
    assertEquals("success", viewBody.get("result"));
    assertEquals(expectedData, viewBody.get("data"));
    viewConnection.disconnect();

    HttpURLConnection searchConnection = tryRequest("searchcsv?value=White&headers=false&colid=0");
    assertEquals(200, searchConnection.getResponseCode());
    Map<String, Object> searchBody =
        adapter.fromJson(new Buffer().readFrom(searchConnection.getInputStream()));
    showDetailsIfError(searchBody);
    assertEquals("success", searchBody.get("result"));
    assertEquals(
        List.of(
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "691",
                "brown-university",
                "0.223552248",
                "Men",
                "1"),
            List.of(
                "White",
                "2020",
                "2020",
                "217156",
                "Brown University",
                "660",
                "brown-university",
                "0.213523132",
                "Women",
                "2")),
        searchBody.get("data"));
    searchConnection.disconnect();
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

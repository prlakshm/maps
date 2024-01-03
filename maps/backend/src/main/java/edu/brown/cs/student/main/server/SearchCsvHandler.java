package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.search.CsvSearcher;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This SearchCsvHandler class handles a /searchcsv request to our server. It is constructed with a
 * CsvDataWrapper object, wrapping a List of List of String representing the CSV data. If there is
 * data loaded in the CsvDataWrapper object, then this request searches that CSV data with the given
 * query parameters and returns the rows matching the query.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class SearchCsvHandler implements Route {

  private final CsvDataWrapper csvData;

  /**
   * The constructor for the SearchCsvHandler class.
   *
   * @param csvData a CsvDataWrapper object wrapping a List of List of String of CSV data and a
   *     boolean representing whether a CSV file has been loaded
   */
  public SearchCsvHandler(CsvDataWrapper csvData) {
    this.csvData = csvData;
  }

  /**
   * Method that handles a /searchcsv request to our Server. Given a request and response, returns a
   * failure or success response object. If successful, the response object contains the rows of CSV
   * data that were found to match the query parameters passed.
   *
   * @param request the Request object passed by client, should contain request parameters headers,
   *     value, and optionally colid
   * @param response the Response object that we do not use
   * @return response object depending on whether the search was successful, and data found
   */
  @Override
  public Object handle(Request request, Response response) {
    if (csvData.getData().isEmpty()) {
      if (!csvData.getLoaded()) {
        return new SearchFailureResponse("error_not_loaded", "CSV file not loaded").serialize();
      }
    }
    String hasHeaders = request.queryParams("headers");
    String value = request.queryParams("value");
    String origColId = request.queryParams("colid");
    String colId = origColId;
    if (value == null) {
      if (hasHeaders == null) {
        return new SearchFailureResponse(
                "error_bad_request", "Required parameters missing: value and hasHeaders")
            .serialize();
      }
      return new SearchFailureResponse("error_bad_request", "Required parameter missing: value")
          .serialize();
    }
    if (hasHeaders == null) {
      return new SearchFailureResponse("error_bad_request", "Required parameter missing: headers")
          .serialize();
    }
    if (origColId == null) {
      colId = "*";
    }
    try {
      CsvSearcher searcher = new CsvSearcher(csvData.getData());
      List<List<String>> rows = searcher.search(Boolean.parseBoolean(hasHeaders), value, colId);
      return new SearchSuccessResponse(value, hasHeaders, origColId, rows).serialize();
    } catch (Exception e) {
      return new SearchFailureResponse("error_bad_request", e.getMessage()).serialize();
    }
  }

  /**
   * A record representing a failed call to the /searchcsv handler, containing a result with an
   * error code and an error message with more information.
   *
   * @param result the String containing an error code
   * @param error_message the String containing a more specific error message
   */
  public record SearchFailureResponse(String result, String error_message) {
    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /searchcsv handler, containing a result of
   * success, as well as the value, headers, and colid parameters, and the found CSV data.
   *
   * @param result the String containing "success"
   * @param value the String containing the query parameter value to be matched in the CSV data
   * @param headers the String containing the query parameter headers, a boolean representing
   *     whether the CSV data to be searched has headers
   * @param colid the String containing the numeric or non-numeric column identifier to search, or *
   *     to search every column
   * @param data the List of List of String containing the matched CSV rows found
   */
  public record SearchSuccessResponse(
      String result, String value, String headers, String colid, List<List<String>> data) {

    /**
     * The constructor for the SearchSuccessResponse class.
     *
     * @param value the String containing the query parameter value to be matched in the CSV data
     * @param headers the String containing the query parameter headers, a boolean representing
     *     whether the CSV data to be searched has headers
     * @param colid the String containing the numeric or non-numeric column identifier to search, or
     *     to search every column
     * @param data the List of List of String containing the matched CSV rows found
     */
    public SearchSuccessResponse(
        String value, String headers, String colid, List<List<String>> data) {
      this("success", value, headers, colid, data);
    }

    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SearchSuccessResponse.class).toJson(this);
    }
  }
}

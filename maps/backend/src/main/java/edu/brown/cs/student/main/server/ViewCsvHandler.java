package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This ViewCsvHandler class handles a /viewcsv request to our server. It is constructed with a
 * CsvDataWrapper object, wrapping a List of List of String representing the CSV data. If there is
 * data loaded in the CsvDataWrapper object, then this request returns an object with a field
 * containing all the rows of that CSV data.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class ViewCsvHandler implements Route {
  private final CsvDataWrapper csvData;

  /**
   * The constructor for the ViewCsvHandler class.
   *
   * @param csvData a CsvDataWrapper object wrapping a List of List of String of CSV data and a
   *     boolean representing whether a CSV file has been loaded
   */
  public ViewCsvHandler(CsvDataWrapper csvData) {
    this.csvData = csvData;
  }

  /**
   * Method that handles a /viewcsv request to our Server. Given a request and response, returns a
   * failure or success response object. If successful, the response object contains all the rows of
   * CSV data in the CSV file loaded.
   *
   * @param request the Request object passed by client, which needs no query parameters
   * @param response the Response object that we do not use
   * @return response object depending on whether the search was successful, and all CSV data
   */
  @Override
  public Object handle(Request request, Response response) {
    if (csvData.getData().isEmpty()) {
      if (!csvData.getLoaded()) {
        return new ViewFailureResponse("error_not_loaded", "CSV file not loaded").serialize();
      }
    }
    return new ViewSuccessResponse(csvData.getData()).serialize();
  }

  /**
   * A record representing a failed call to the /viewcsv handler, containing a result with an error
   * code and an error message with more information.
   *
   * @param result the String containing an error code
   * @param error_message the String containing a more specific error message
   */
  public record ViewFailureResponse(String result, String error_message) {

    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ViewFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /viewcsv handler, containing a result of
   * success, as well as all the loaded CSV data.
   *
   * @param result the String containing "success"
   * @param data the List of List of String containing all the CSV data loaded
   */
  public record ViewSuccessResponse(String result, List<List<String>> data) {

    /**
     * The constructor for the ViewSuccessResponse class.
     *
     * @param data the List of List of String containing all the CSV data loaded
     */
    public ViewSuccessResponse(List<List<String>> data) {
      this("success", data);
    }

    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ViewSuccessResponse.class).toJson(this);
    }
  }
}

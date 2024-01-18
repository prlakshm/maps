package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.creator.ListStringFromRow;
import edu.brown.cs.student.main.csv.parse.CsvParser;

import java.io.*;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This LoadCsvHandler class handles a /loadcsv request to our server. It is constructed with a
 * CsvDataWrapper object, wrapping a List of List of String representing the CSV data. If the
 * filepath passed as a query parameter is found, the handle method parses the CSV and stores its
 * rows in the CsvDataWrapper object and sets the wrapper's loadedInPast field to true.
 *
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class LoadCsvHandler implements Route {
  private final CsvDataWrapper csvData;

  /**
   * The constructor for the LoadCsvHandler class.
   *
   * @param csvData a CsvDataWrapper object, with fields csvData, representing rows of loaded CSV
   *     data, and loadedInPast, a boolean representing whether a CSV has been loaded
   */
  public LoadCsvHandler(CsvDataWrapper csvData) {
    this.csvData = csvData;
  }

  /**
   * Method that handles a /loadcsv request to our Server. Given a request and response, returns a
   * failure or success response object. If successful, the CSV data in the filepath is loaded in
   * CsvDataWrapper and its loadedInPast field is set to true. A load is only successful if the file
   * to be loaded is located in the data directory.
   *
   * @param request the Request object passed by client, should contain request parameter filepath
   * @param response the Response object that we do not use
   * @return response object depending on whether the file loading and parsing was successful
   */
  @Override
  public Object handle(Request request, Response response) {
    String csvFilePath = request.queryParams("filepath");
    if (csvFilePath == null) {
      return new LoadFailureResponse(
              "error_bad_request", "Missing required parameter: filepath", "")
          .serialize();
    }
    try {
      // String[] paths = csvFilePath.split("/");
      // if (!(paths[0].equals("repl") && paths[1].equals("src") && paths[2].equals("backend") && paths[4].equals("data"))) {
      //   return new LoadFailureResponse(
      //           "error_datasource", "Filepath located in an inaccessible directory", csvFilePath)
      //       .serialize();
      // }
      ClassLoader classLoader = getClass().getClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream(csvFilePath);

      //Check file in resource folder
      if (inputStream == null) {
        return new LoadFailureResponse(
                "error_datasource", "File not found \"" + csvFilePath + "\"", csvFilePath)
                .serialize();
      }

      InputStreamReader reader = new InputStreamReader(inputStream);
      CsvParser<List<String>> parser = new CsvParser<>(reader, new ListStringFromRow());
      parser.parseCsv();
      List<List<String>> rows = parser.getStoreRows();
      csvData.setData(rows);
      csvData.setLoaded(true);
      return new LoadSuccessResponse(csvFilePath).serialize();
    } catch (FileNotFoundException fnfe) {
      return new LoadFailureResponse("error_datasource", "File not found", csvFilePath).serialize();
    } catch (Exception e) {
      return new LoadFailureResponse("error_bad_request", e.getMessage(), csvFilePath).serialize();
    }
  }

  /**
   * A record representing a failed call to the /loadcsv handler, containing a result with an error
   * code, an error message with more information, and the given filepath.
   *
   * @param result a String containing an error code
   * @param error_message a String containing a more informative error message
   * @param filepath a String containing the filepath given by client
   */
  public record LoadFailureResponse(String result, String error_message, String filepath) {
    /**
     * This method serializes a failure response object.
     *
     * @return this failure response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadFailureResponse.class).toJson(this);
    }
  }

  /**
   * A record representing a successful call to the /loadcsv handler, containing a result of
   * success, as well as the given filepath parameter.
   *
   * @param result the String containing "success"
   * @param filepath the String containing the filepath to the loaded file
   */
  public record LoadSuccessResponse(String result, String filepath) {
    /**
     * The constructor for the LoadSuccessResponse record.
     *
     * @param filepath the String filepath parameter given
     */
    public LoadSuccessResponse(String filepath) {
      this("success", filepath);
    }
    /**
     * This method serializes a success response object.
     *
     * @return this success response object, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(LoadSuccessResponse.class).toJson(this);
    }
  }
}

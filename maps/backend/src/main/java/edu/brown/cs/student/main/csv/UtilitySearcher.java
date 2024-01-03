package edu.brown.cs.student.main.csv;

import static java.lang.System.exit;

import edu.brown.cs.student.main.creator.ListStringFromRow;
import edu.brown.cs.student.main.csv.parse.CsvParser;
import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import edu.brown.cs.student.main.csv.search.CsvSearcher;
import edu.brown.cs.student.main.csv.search.SearchException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * The UtilitySearcher class of our project. This is where execution begins for the command line
 * tool.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public final class UtilitySearcher {
  private final FileReader dataFileReader;
  private final String searchValue;
  private final boolean hasHeaders;
  private final String colId;

  /**
   * Constructor of the UtilitySearcher class.
   *
   * @param dataFileReader FileReader representing the file to read
   * @param searchValue String representing target item
   * @param hasHeaders boolean representing whether the file has headers
   * @param colId String representing the column(s) to search in
   */
  private UtilitySearcher(
      FileReader dataFileReader, String searchValue, boolean hasHeaders, String colId) {
    this.dataFileReader = dataFileReader;
    this.searchValue = searchValue;
    this.hasHeaders = hasHeaders;
    this.colId = colId;
  }

  /**
   * The initial method called when execution of UtilityRunner begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(
          "ERROR: Please provide all required arguments:\n"
              + "The CSV filepath, the value to search for, (optional) whether the CSV "
              + "contains headers, (optional) the column identifier");
      exit(1);
    }
    String filePath = args[0];
    try {
      File csvFile = new File(filePath);
      FileReader fileReader = new FileReader(csvFile);
      String searchValue = args[1];
      boolean hasHeaders = args.length >= 3 && Boolean.parseBoolean(args[2]);
      String colId = args.length >= 4 ? args[3] : "*";
      new UtilitySearcher(fileReader, searchValue, hasHeaders, colId).run();
    } catch (FileNotFoundException e) {
      System.err.println("ERROR: No file found at path " + filePath);
      exit(1);
    }
  }

  /**
   * A method called by the main method, which creates a CSVParser and CSVSearcher and prints the
   * results, whether they be error messages or rows found, to the console.
   */
  private void run() {
    try {
      CsvParser<List<String>> csvParser = new CsvParser<>(dataFileReader, new ListStringFromRow());
      csvParser.parseCsv();
      List<List<String>> dataRows = csvParser.getStoreRows();
      CsvSearcher csvSearcher = new CsvSearcher(dataRows);
      List<List<String>> parsedResults = csvSearcher.search(hasHeaders, searchValue, colId);
      if (parsedResults.isEmpty()) {
        String colRep = colId.equals("*") ? "any column" : "column " + colId;
        System.err.println("ERROR: No rows found with value " + searchValue + " in " + colRep);
      }
      for (List<String> row : parsedResults) {
        System.out.println(row);
      }
      exit(0);
    } catch (FactoryFailureException | IOException | SearchException e) {
      System.err.println(e.getMessage());
      exit(1);
    }
  }
}

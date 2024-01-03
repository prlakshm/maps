package edu.brown.cs.student.main.csv.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is the CsvSearcher class, which is called by the main run function. Constructed with a
 * CsvParser object, an object of the CsvSearcher class can search the csvRows from the CsvParser's
 * methods parseCsv and getStoreRows to find rows with the given search criteria.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class CsvSearcher {

  private final List<List<String>> csvRows;

  /**
   * Constructor for the CsvSearcher class.
   *
   * @param csvData List of List of String, representing the CSV data
   */
  public CsvSearcher(List<List<String>> csvData) {
    this.csvRows = csvData;
  }

  /**
   * A method that uses the String colID to find the numeric index of the target column, only called
   * if there is a specific column to be searching. CsvRows is guaranteed to not be empty because we
   * caught that case in search before this function can be called.
   *
   * @param colIdIsNum boolean representing whether the colID is a number
   * @param colId String representing the name or index of column to be searched
   * @return integer representing the index of column to be searched
   * @throws IndexOutOfBoundsException if the target column is not found or is out of range
   */
  private int findColIndex(boolean colIdIsNum, String colId) throws IndexOutOfBoundsException {
    int colIndex;
    if (!colIdIsNum) {
      // If the column ID is not numeric, treat it as a column name and find its index
      List<String> headers = csvRows.get(0);
      colIndex = headers.indexOf(colId);
      if (colIndex == -1) {
        throw new IndexOutOfBoundsException(
            "Column identifier "
                + colId
                + " not found. Valid column identifiers include "
                + headers
                + " and numbers between 0 and "
                + (headers.size() - 1)
                + " inclusive.");
      }
    } else {
      // Otherwise, use convert the column ID into a number
      colIndex = Integer.parseInt(colId);
      int firstRowLength = csvRows.get(0).size();
      if (colIndex >= firstRowLength) {
        throw new IndexOutOfBoundsException(
            "Column index "
                + colIndex
                + " not found. Can search numeric column identifiers between 0 and "
                + (firstRowLength - 1)
                + " inclusive.");
      }
    }
    return colIndex;
  }

  /**
   * A method that searches all columns of each row of the CSV for the desired searchValue. NOTE:
   * the searchValue much match an item in the row exactly (case matters).
   *
   * @param searchValue String representing the value to find in any column of a row
   * @return List of String containing the rows where the searchValue is found exactly
   */
  private List<List<String>> searchAllCols(boolean hasHeaders, String searchValue) {
    int start = hasHeaders ? 1 : 0;
    List<List<String>> resultsData = new ArrayList<>();
    for (List<String> row : csvRows.subList(start, csvRows.size())) {
      for (String item : row) {
        if (item.equals(searchValue)) {
          resultsData.add(row);
        }
      }
    }
    return resultsData;
  }

  /**
   * A method that searches only one column, with index colIndex, and returns all rows where
   * searchValue is found in the column with that index.
   *
   * @param searchValue String representing the value to be found in its exact form
   * @param colIdIsNum boolean representing whether column ID is numeric
   * @param colId String which the method includes in an error message if exception is thrown
   * @return List of String of rows where searchValue in its exact form is found in the column with
   *     index colIndex, or a List only containing an error String if fails
   */
  private List<List<String>> searchOneCol(
      boolean hasHeaders, String searchValue, boolean colIdIsNum, String colId)
      throws SearchException {
    int start = hasHeaders ? 1 : 0;
    List<List<String>> resultsData = new ArrayList<>();
    try {
      int colIndex = findColIndex(colIdIsNum, colId);
      // For each row, get the item at the colIndex and check if it matches searchValue
      for (List<String> row : csvRows.subList(start, csvRows.size())) {
        String item = row.get(colIndex);
        if (item.equals(searchValue)) {
          resultsData.add(row);
        }
      }
      return resultsData;
    } catch (IndexOutOfBoundsException ibe) {
      throw new SearchException(ibe.getMessage());
    }
  }

  /**
   * A public method, which is called by the Main runner, which searches for searchValue in the
   * column(s) specified by colID, and returns a list of matching rows or a list containing an error
   * message describing what went wrong.
   *
   * @param hasHeaders boolean representing whether the CSV has a row of headers
   * @param searchValue String representing the value to be matched exactly in the CSV data
   * @param colId String representing the column(s) to search for the exact appearance of
   *     searchValue
   * @return List of String containing the matching rows or only containing an error String
   * @throws SearchException if there is an issue with the search criteria given
   */
  public List<List<String>> search(boolean hasHeaders, String searchValue, String colId)
      throws SearchException {
    boolean colIdIsNum = colId.matches("^-?\\d+$");
    if (csvRows.isEmpty()) {
      // If there is no data, return an empty list
      return List.of();
    } else if (colId.equals("*")) {
      // Search all columns for the search value
      List<List<String>> rowsFound = searchAllCols(hasHeaders, searchValue);
      return Collections.unmodifiableList(rowsFound);
    } else if (colIdIsNum || hasHeaders) {
      // Search the specified column for the search value
      List<List<String>> rowsFound = searchOneCol(hasHeaders, searchValue, colIdIsNum, colId);
      return Collections.unmodifiableList(rowsFound);
    } else {
      // Return list with error message to be passed along to main
      int maxIndex = csvRows.get(0).size() - 1;
      throw new SearchException(
          "Cannot search non-numeric column "
              + colId
              + " if no headers. Can search numeric column identifiers between 0 and "
              + maxIndex
              + " inclusive.");
    }
  }
}

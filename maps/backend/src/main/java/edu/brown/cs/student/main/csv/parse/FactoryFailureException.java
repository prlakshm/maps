package edu.brown.cs.student.main.csv.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an error provided to catch any error that may occur when you create an object from a row.
 * Feel free to expand or supplement or use it for other purposes.
 *
 * @author cs0320 staff
 * @version 1.0
 */
public class FactoryFailureException extends Exception {
  final List<String> row;

  /**
   * Constructor for the FactoryFailureException, thrown when failed to create object from CSV row.
   *
   * @param message the error message supplied when create fails
   * @param row the row that create failed to turn into an object
   */
  public FactoryFailureException(String message, List<String> row) {
    super(message);
    this.row = new ArrayList<>(row);
  }
}

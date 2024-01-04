package edu.brown.cs.student.main.sources;

/**
 * This class represents an exception that is thrown if there is an error fetching broadband access
 * data from the source.
 *
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class DataSourceException extends Exception {

  /**
   * This is a one-parameter constructor for the DataSourceException class.
   *
   * @param message the error message that the caller of the exception passes
   */
  public DataSourceException(String message) {
    super(message);
  }
}

package edu.brown.cs.student.main.creator;

import java.util.List;

/**
 * This class implements CreatorFromRow, where the generic T is a list of Strings. This class
 * overrides the create method that allows the CSV parser to convert each row into an object of type
 * list of Strings.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class ListStringFromRow implements CreatorFromRow<List<String>> {
  /**
   * Converts one row into an object of type list of Strings.
   *
   * @param row a list of String, to be kept as a list of String
   * @return the resulting list of String representing the row
   */
  @Override
  public List<String> create(List<String> row) {
    return row;
  }
}

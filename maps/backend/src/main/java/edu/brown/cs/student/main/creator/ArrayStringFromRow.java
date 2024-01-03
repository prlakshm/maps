package edu.brown.cs.student.main.creator;

import java.util.List;

/**
 * This class implements CreatorFromRow, where the generic T is an array of Strings. This class
 * overrides the create method that allows the CSV parser to convert each row into an object of type
 * array of Strings.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class ArrayStringFromRow implements CreatorFromRow<String[]> {
  /**
   * Converts one row into an object of type array of Strings.
   *
   * @param row a list of String, to be turned into an array of String
   * @return the resulting array of String representing the row
   */
  @Override
  public String[] create(List<String> row) {
    return row.toArray(new String[0]);
  }
}

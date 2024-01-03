package edu.brown.cs.student.main.creator;

import java.util.LinkedList;
import java.util.List;

/**
 * This class implements CreatorFromRow, where the generic T is a linked list of Strings. This class
 * overrides the create method that allows the CSV parser to convert each row into an object of type
 * linked list of String.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class LinkedListStringFromRow implements CreatorFromRow<LinkedList<String>> {
  /**
   * Converts one row into an object of type linked list of String.
   *
   * @param row a list of String, to be turned into a linked list of String
   * @return the resulting linked list of String representing the row
   */
  @Override
  public LinkedList<String> create(List<String> row) {
    return new LinkedList<>(row);
  }
}

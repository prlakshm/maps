package edu.brown.cs.student.creator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.creator.ListStringFromRow;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the ListStringFromRow class, which implements CreatorFromRow.
 * A ListStringFromRow instance keeps a list of string row as a list of string row.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestListStringFromRow {

  /** Create an empty List<String> from an empty row */
  @Test
  public void createFromEmptyList() {
    List<String> emptyList = List.of();
    ListStringFromRow listStringCreator = new ListStringFromRow();
    assertEquals(emptyList, listStringCreator.create(emptyList));
  }

  /** Create a singleton List<String> from a singleton row */
  @Test
  public void createFromSingletonList() {
    List<String> singletonList = List.of("CSCI 0320");
    ListStringFromRow listStringCreator = new ListStringFromRow();
    assertEquals(singletonList, listStringCreator.create(singletonList));
  }

  /** Create a long List<String> from a long row */
  @Test
  public void createFromListString() {
    List<String> stringList = List.of("i", "am", "a", "pisces", "what", "is", "ur", "sign");
    ListStringFromRow listStringCreator = new ListStringFromRow();
    assertEquals(stringList, listStringCreator.create(stringList));
  }
}

package edu.brown.cs.student.creator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import edu.brown.cs.student.main.creator.ArrayStringFromRow;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the ArrayStringFromRow class, which implements CreatorFromRow.
 * An ArrayStringFromRow instance turns a list of string row into an array of string row.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestArrayStringFromRow {
  /** Create an empty String[] from an empty row */
  @Test
  public void createFromEmptyList() {
    List<String> emptyList = List.of();
    String[] emptyArray = new String[] {};
    ArrayStringFromRow arrayStringCreator = new ArrayStringFromRow();
    assertArrayEquals(emptyArray, arrayStringCreator.create(emptyList));
  }

  /** Create a singleton String[] from a singleton row */
  @Test
  public void createFromSingletonList() {
    List<String> singletonList = List.of("CSCI 0320");
    String[] singletonArray = singletonList.toArray(new String[0]);
    ArrayStringFromRow arrayStringCreator = new ArrayStringFromRow();
    assertArrayEquals(singletonArray, arrayStringCreator.create(singletonList));
  }

  /** Create a long String[] from a long row */
  @Test
  public void createFromListString() {
    List<String> stringList = List.of("i", "am", "a", "pisces", "what", "is", "ur", "sign");
    String[] stringArray = stringList.toArray(new String[0]);
    ArrayStringFromRow arrayStringCreator = new ArrayStringFromRow();
    assertArrayEquals(stringArray, arrayStringCreator.create(stringList));
  }
}

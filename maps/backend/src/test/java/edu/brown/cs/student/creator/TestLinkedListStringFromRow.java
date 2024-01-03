package edu.brown.cs.student.creator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.creator.LinkedListStringFromRow;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the LinkedListStringFromRow class, which implements
 * CreatorFromRow. A LinkedListStringFromRow instance turns a list of string row into a linked list
 * of string row.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestLinkedListStringFromRow {

  /** Create an empty LinkedList<String> from an empty row */
  @Test
  public void createFromEmptyList() {
    List<String> emptyList = List.of();
    LinkedList<String> emptyLinkedList = new LinkedList<>();
    LinkedListStringFromRow linkedListStringCreator = new LinkedListStringFromRow();
    assertEquals(emptyLinkedList, linkedListStringCreator.create(emptyList));
  }

  /** Create a singleton LinkedList<String> from a singleton row */
  @Test
  public void createFromSingletonList() {
    List<String> singletonList = List.of("CSCI 0320");
    LinkedList<String> singletonLinkedList = new LinkedList<>(singletonList);
    LinkedListStringFromRow linkedListStringCreator = new LinkedListStringFromRow();
    assertEquals(singletonLinkedList, linkedListStringCreator.create(singletonList));
  }

  /** Create a long LinkedList<String> from a long row */
  @Test
  public void createFromListString() {
    List<String> stringList = List.of("i", "am", "a", "pisces", "what", "is", "ur", "sign");
    LinkedList<String> stringLinkedList = new LinkedList<>(stringList);
    LinkedListStringFromRow linkedListStringCreator = new LinkedListStringFromRow();
    assertEquals(stringLinkedList, linkedListStringCreator.create(stringList));
  }
}

package edu.brown.cs.student.creator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.brown.cs.student.main.creator.PairStringListFromRow;
import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import java.util.List;
import kotlin.Pair;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the PairStringListFromRow class, which implements
 * CreatorFromRow. A PairStringListFromRow instance turns a list of string row into a pair,
 * containing a key string (the first element in a row) and a list of strings (the remaining
 * elements in the row).
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestPairStringListFromRow {

  /** Create an empty List of String from an empty row */
  @Test
  public void createFromEmptyList() {
    List<String> emptyList = List.of();
    PairStringListFromRow pairStringListCreator = new PairStringListFromRow();
    FactoryFailureException thrown =
        assertThrows(FactoryFailureException.class, () -> pairStringListCreator.create(emptyList));
    assertEquals("Cannot parse an empty row into a pair", thrown.getMessage());
  }

  /**
   * Create a Pair with a String and an empty List of String from a singleton row
   *
   * @throws FactoryFailureException if row creation fails
   */
  @Test
  public void createFromSingletonList() throws FactoryFailureException {
    List<String> singletonList = List.of("CSCI 0320");
    Pair<String, List<String>> pairEmptyList = new Pair<>("CSCI 0320", List.of());
    PairStringListFromRow pairStringListCreator = new PairStringListFromRow();
    assertEquals(pairEmptyList, pairStringListCreator.create(singletonList));
  }

  /**
   * Create a Pair with a String and a long List of String from a long row
   *
   * @throws FactoryFailureException if row creation fails
   */
  @Test
  public void createFromListString() throws FactoryFailureException {
    List<String> stringList = List.of("i", "am", "a", "pisces", "what", "is", "ur", "sign");
    Pair<String, List<String>> pairEmptyList =
        new Pair<>("i", stringList.subList(1, stringList.size()));
    PairStringListFromRow pairStringListCreator = new PairStringListFromRow();
    assertEquals(pairEmptyList, pairStringListCreator.create(stringList));
  }
}

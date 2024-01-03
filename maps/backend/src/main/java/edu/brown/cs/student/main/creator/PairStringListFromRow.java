package edu.brown.cs.student.main.creator;

import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import java.util.ArrayList;
import java.util.List;
import kotlin.Pair;

/**
 * This class implements CreatorFromRow, where the generic T is a pair of String and List of String.
 * This class overrides the create method that allows the CSV parser to convert each row into an
 * object of type pair of String and List of String.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class PairStringListFromRow implements CreatorFromRow<Pair<String, List<String>>> {
  /**
   * Converts one row into an object of type pair of String and List of String.
   *
   * @param row a list of String, to be turned into a pair of String and List of String
   * @return the resulting pair of String and List of String representing the row
   * @throws FactoryFailureException if the row is empty and cannot be made into a pair
   */
  @Override
  public Pair<String, List<String>> create(List<String> row) throws FactoryFailureException {
    if (row.isEmpty() || (row.size() == 1 && row.get(0).equals(""))) {
      throw new FactoryFailureException("Cannot parse an empty row into a pair", row);
    }
    String key = row.get(0);
    List<String> itemsList = new ArrayList<>(row.subList(1, row.size()));
    return new Pair<>(key, itemsList);
  }
}

package edu.brown.cs.student.main.creator;

import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import java.util.List;

/**
 * This interface defines a method that allows your CSV parser to convert each row into an object of
 * some arbitrary passed type.
 *
 * <p>Your parser class constructor should take a second parameter of this generic interface type.
 *
 * @author cs0320 staff
 * @version 1.0
 */
public interface CreatorFromRow<T> {
  /**
   * Converts one row into an object of type T.
   *
   * @param row a list of String, to be turned into an object of type T
   * @return the resulting object of type T representing the row
   * @throws FactoryFailureException if creation of object from row fails
   */
  T create(List<String> row) throws FactoryFailureException;
}

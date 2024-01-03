package edu.brown.cs.student.main.csv.parse;

import edu.brown.cs.student.main.creator.CreatorFromRow;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class is the CSVParser class, which depends on a generic type T. Its fields include a
 * BufferedReader object, a CreatorFromRow of T object, a Pattern object, and a List of T. This
 * class has methods parseCSV, which parses the CSV from these fields and getStoreRows, which allows
 * a user to get the parsed CSV information.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class CsvParser<T> {
  private final BufferedReader buffReader;
  private final CreatorFromRow<T> creatorFromRow;
  private final Pattern regexSplitCsvRow;
  private final List<T> storeRows;

  /**
   * Constructor for the CSVParser class.
   *
   * @param givenReader a Reader object to be wrapped in a BufferedReader to read the CSV
   * @param creatorFromRow an object that defines how the CSVParser converts each row into an object
   */
  public CsvParser(Reader givenReader, CreatorFromRow<T> creatorFromRow) {
    this.buffReader = new BufferedReader(givenReader);
    this.creatorFromRow = creatorFromRow;
    this.regexSplitCsvRow = Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
    this.storeRows = new ArrayList<>();
  }

  /**
   * A method with no arguments the returns nothing, uses the field data to parse the CSV data, turn
   * each row into an object of type T, and store these rows in storeRows field as a list of T.
   *
   * @throws FactoryFailureException when the create method of the CreatorFromRow of T fails
   * @throws IOException when there is an issue reading the CSV from the given Reader object
   */
  public void parseCsv() throws FactoryFailureException, IOException {
    List<T> csvData = new ArrayList<>();
    try {
      String line = buffReader.readLine();
      while (line != null) {
        String[] result = regexSplitCsvRow.split(line);
        T dataCreated = creatorFromRow.create(List.of(result));
        csvData.add(dataCreated);
        line = buffReader.readLine();
      }
      storeRows.clear();
      storeRows.addAll(csvData);
    } catch (FactoryFailureException ffe) {
      throw new FactoryFailureException(ffe.getMessage(), ffe.row);
    } catch (IOException ioe) {
      throw new IOException("Issue while reading the given Reader");
    }
  }

  /**
   * A getter method with no arguments that allows access to the storeRows field, which stores the
   * rows of the CSV as a List of type T.
   *
   * @return a list of type T representing the rows read from the CSV
   */
  public List<T> getStoreRows() {
    return Collections.unmodifiableList(storeRows);
  }
}

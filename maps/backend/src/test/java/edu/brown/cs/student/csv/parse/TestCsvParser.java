package edu.brown.cs.student.csv.parse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.brown.cs.student.main.creator.ArrayStringFromRow;
import edu.brown.cs.student.main.creator.CreatorFromRow;
import edu.brown.cs.student.main.creator.LinkedListStringFromRow;
import edu.brown.cs.student.main.creator.ListStringFromRow;
import edu.brown.cs.student.main.creator.PairStringListFromRow;
import edu.brown.cs.student.main.csv.parse.CsvParser;
import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import kotlin.Pair;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the CsvParser class. A CsvParser object has fields of types
 * BufferedReader, CreatorFromRow of T, Pattern, and StoreRows. It has methods parseCSV(), which
 * parses CSV data into the StoreRows field, and getStoreRows(), which allows access to that data.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestCsvParser {
  /**
   * Given a FileReader and a PairStringListFromRow, parses all rows of data, which are of different
   * lengths, including a blank, row which raises an exception due to the blank row
   *
   * @throws IOException if file reading were to fail
   */
  @Test
  public void pairParseFromFileEmptyRow() throws IOException {
    File pairData = new File("data/custom/people_rows_different_lengths.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(pairData));
    CreatorFromRow<Pair<String, List<String>>> creatorFromRow = new PairStringListFromRow();
    CsvParser<Pair<String, List<String>>> pairParser = new CsvParser<>(buffRead, creatorFromRow);
    FactoryFailureException thrown =
        assertThrows(FactoryFailureException.class, () -> pairParser.parseCsv());
    assertEquals("Cannot parse an empty row into a pair", thrown.getMessage());
  }

  /**
   * Given a StringReader and a LinkedListStringFromRow, fails to parse the data due to closing the
   * buffered reader, which throws an exception
   *
   * @throws IOException if file reading were to fail
   */
  @Test
  public void failParseFromClosedReader() throws IOException {
    String dataString = "this,is,a,row\nthis,is,another,row\nhow,many,rows,will\nthere,be,in,total";
    BufferedReader buffRead = new BufferedReader(new StringReader(dataString));
    CreatorFromRow<LinkedList<String>> creatorFromRow = new LinkedListStringFromRow();
    CsvParser<LinkedList<String>> stringParser = new CsvParser<>(buffRead, creatorFromRow);
    buffRead.close();
    IOException thrown = assertThrows(IOException.class, stringParser::parseCsv);
    assertEquals("Issue while reading the given Reader", thrown.getMessage());
  }

  /**
   * Given a FileReader and a ArrayStringListFromRow, parses an empty file into an empty list
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseEmptyFileReader() throws IOException, FactoryFailureException {
    File emptyFile = new File("data/custom/empty.csv");
    FileReader fileRead = new FileReader(emptyFile);
    CreatorFromRow<String[]> creatorFromRow = new ArrayStringFromRow();
    CsvParser<String[]> peopleParser = new CsvParser<>(fileRead, creatorFromRow);
    peopleParser.parseCsv();
    assertEquals(List.of(), peopleParser.getStoreRows());
  }

  /**
   * Given a StringReader and a ListStringListFromRow, parses an empty string into an empty list
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseEmptyStringReader() throws IOException, FactoryFailureException {
    String emptyFile = "";
    StringReader stringRead = new StringReader(emptyFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> peopleParser = new CsvParser<>(stringRead, creatorFromRow);
    peopleParser.parseCsv();
    assertEquals(List.of(), peopleParser.getStoreRows());
  }

  /**
   * Given a FileReader and a ListStringListFromRow, correctly parses rows with items with quotation
   * marks, and returns a list of list of correctly parsed strings
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseFileReaderRowsWithQuotes() throws IOException, FactoryFailureException {
    File peopleWithQuotes = new File("data/custom/people_rows_have_quotes.csv");
    FileReader fileRead = new FileReader(peopleWithQuotes);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> peopleParser = new CsvParser<>(fileRead, creatorFromRow);
    peopleParser.parseCsv();
    List<List<String>> listPeople =
        List.of(
            List.of("ID", "Name", "Favorite Food", "State", "Country"),
            List.of("1", "Sarah", "\"Vanilla, Strawberry\"", "NC", "USA"),
            List.of("2", "Sara", "\"Vanilla, \"Strawberry\"\"", "NM", "USA"),
            List.of("3", "Sera", "\"\"Vanilla\", \"Strawberry\"\"", "ND", "USA"),
            List.of("4", "Amy", "Chocolate", "MN", "USA", "extra!"));
    assertEquals(listPeople, peopleParser.getStoreRows());
  }

  /**
   * Given a StringReader and a LinkedListStringFromRow, parses the data even though rows are
   * different lengths
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseLinkedListFromDifferentLengthRows() throws IOException, FactoryFailureException {
    String dataString = "this,is,a,row\nhow,many,rows,will,there\nbe,in,total?";
    BufferedReader buffRead = new BufferedReader(new StringReader(dataString));
    CreatorFromRow<LinkedList<String>> creatorFromRow = new LinkedListStringFromRow();
    CsvParser<LinkedList<String>> stringParser = new CsvParser<>(buffRead, creatorFromRow);
    stringParser.parseCsv();
    List<LinkedList<String>> listLinkedList =
        List.of(
            new LinkedList<>(List.of("this", "is", "a", "row")),
            new LinkedList<>(List.of("how", "many", "rows", "will", "there")),
            new LinkedList<>(List.of("be", "in", "total?")));
    assertEquals(listLinkedList, stringParser.getStoreRows());
  }

  /**
   * Given a FileReader and a ListStringFromRow, parses the ten-star csv file and stores it as a
   * list of list of strings in the StoreRow field.
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseStarDataListCreator() throws IOException, FactoryFailureException {
    File starFile = new File("data/stars/ten-star.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> starParser = new CsvParser<>(buffRead, creatorFromRow);
    starParser.parseCsv();
    List<List<String>> expectedStarData =
        List.of(
            List.of("StarID", "ProperName", "X", "Y", "Z"),
            List.of("0", "Sol", "0", "0", "0"),
            List.of("1", "", "282.43485", "0.00449", "5.36884"),
            List.of("2", "", "43.04329", "0.00285", "-15.24144"),
            List.of("3", "", "277.11358", "0.02422", "223.27753"),
            List.of("3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"),
            List.of("70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"),
            List.of("71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"),
            List.of("71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"),
            List.of("87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"),
            List.of("118721", "", "-2.28262", "0.64697", "0.29354"));
    assertEquals(expectedStarData, starParser.getStoreRows());
  }

  /**
   * Given a FileReader and an ArrayStringFromRow, parses the ten-star csv file and stores it as a
   * list of array of strings in the StoreRow field.
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseStarDataArrayCreator() throws IOException, FactoryFailureException {
    File starFile = new File("data/stars/ten-star.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starFile));
    CreatorFromRow<String[]> creatorFromRow = new ArrayStringFromRow();
    CsvParser<String[]> starParser = new CsvParser<>(buffRead, creatorFromRow);
    starParser.parseCsv();
    List<String[]> expectedStarData =
        List.of(
            new String[] {"StarID", "ProperName", "X", "Y", "Z"},
            new String[] {"0", "Sol", "0", "0", "0"},
            new String[] {"1", "", "282.43485", "0.00449", "5.36884"},
            new String[] {"2", "", "43.04329", "0.00285", "-15.24144"},
            new String[] {"3", "", "277.11358", "0.02422", "223.27753"},
            new String[] {"3759", "96 G. Psc", "7.26388", "1.55643", "0.68697"},
            new String[] {"70667", "Proxima Centauri", "-0.47175", "-0.36132", "-1.15037"},
            new String[] {"71454", "Rigel Kentaurus B", "-0.50359", "-0.42128", "-1.1767"},
            new String[] {"71457", "Rigel Kentaurus A", "-0.50362", "-0.42139", "-1.17665"},
            new String[] {"87666", "Barnard's Star", "-0.01729", "-1.81533", "0.14824"},
            new String[] {"118721", "", "-2.28262", "0.64697", "0.29354"});
    List<String[]> starDataRows = starParser.getStoreRows();
    for (int ind = 0; ind < expectedStarData.size(); ind++) {
      assertArrayEquals(expectedStarData.get(ind), starDataRows.get(ind));
    }
  }

  /**
   * Given a FileReader and a ListStringFromRow, parses the earnings csv file and stores it as a
   * list of list of strings in the StoreRow field.
   *
   * @throws IOException if file reading were to fail
   * @throws FactoryFailureException if row creation were to fail
   */
  @Test
  public void parseEarningsDataListCreator() throws IOException, FactoryFailureException {
    File earningsFile = new File("data/census/dol_ri_earnings_disparity.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(earningsFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> starParser = new CsvParser<>(buffRead, creatorFromRow);
    starParser.parseCsv();
    List<List<String>> expectedStarData =
        List.of(
            List.of(
                "State",
                "Data Type",
                "Average Weekly Earnings",
                "Number of Workers",
                "Earnings Disparity",
                "Employed Percent"),
            List.of("RI", "White", "\" $1,058.47 \"", "395773.6521", " $1.00 ", "75%"),
            List.of("RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"),
            List.of(
                "RI",
                "Native American/American Indian",
                " $471.07 ",
                "2315.505646",
                " $0.45 ",
                "0%"),
            List.of(
                "RI", "Asian-Pacific Islander", "\" $1,080.09 \"", "18956.71657", " $1.02 ", "4%"),
            List.of("RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"),
            List.of("RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"));
    assertEquals(expectedStarData, starParser.getStoreRows());
  }

  /**
   * Given a StringReader and a PairStringListFromRow, parses multiple rows of proper Pair data
   *
   * @throws IOException if StringReader reading were to fail
   * @throws FactoryFailureException if Star creation were to fail
   */
  @Test
  public void pairParseFromStringReaderMultiLine() throws FactoryFailureException, IOException {
    String starData = "Amy,4,1,2,3\nSarah,100,-2,5,10.5\nJohn,4,5,6,1";
    BufferedReader buffRead = new BufferedReader(new StringReader(starData));
    CreatorFromRow<Pair<String, List<String>>> creatorFromRow = new PairStringListFromRow();
    CsvParser<Pair<String, List<String>>> pairParser = new CsvParser<>(buffRead, creatorFromRow);
    List<Pair<String, List<String>>> expectedPairData =
        List.of(
            new Pair<>("Amy", Arrays.asList("4", "1", "2", "3")),
            new Pair<>("Sarah", Arrays.asList("100", "-2", "5", "10.5")),
            new Pair<>("John", Arrays.asList("4", "5", "6", "1")));
    pairParser.parseCsv();
    assertEquals(expectedPairData, pairParser.getStoreRows());
  }

  /**
   * Given a FileReader and a PairStringListFromRow, parses multiple rows of proper Pair data
   *
   * @throws IOException if StringReader reading were to fail
   * @throws FactoryFailureException if Star creation were to fail
   */
  @Test
  public void pairParseFromFileReaderMultiLine() throws FactoryFailureException, IOException {
    File sameHeaders = new File("data/custom/all_header_same_name.csv");
    FileReader fileRead = new FileReader(sameHeaders);
    CreatorFromRow<Pair<String, List<String>>> creatorFromRow = new PairStringListFromRow();
    CsvParser<Pair<String, List<String>>> pairParser = new CsvParser<>(fileRead, creatorFromRow);
    List<Pair<String, List<String>>> expectedPairData =
        List.of(
            new Pair<>("Letter", Arrays.asList("Letter", "Letter", "Letter", "Letter")),
            new Pair<>("A", Arrays.asList("B", "C", "D", "E")),
            new Pair<>("F", Arrays.asList("G", "H", "I", "J")),
            new Pair<>("A", Arrays.asList("G", "H", "I", "E")));
    pairParser.parseCsv();
    assertEquals(expectedPairData, pairParser.getStoreRows());
  }
}

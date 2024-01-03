package edu.brown.cs.student.csv.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.brown.cs.student.main.creator.CreatorFromRow;
import edu.brown.cs.student.main.creator.ListStringFromRow;
import edu.brown.cs.student.main.csv.parse.CsvParser;
import edu.brown.cs.student.main.csv.parse.FactoryFailureException;
import edu.brown.cs.student.main.csv.search.CsvSearcher;
import edu.brown.cs.student.main.csv.search.SearchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the CsvSearcher class. A CsvSearcher object has fields of
 * types CsvParser and List of List of String, representing the CSV data. It has method search(...),
 * which searches the rows, the List of List of String, for the given search criteria, and returns a
 * list with the rows found or throws an exception.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestCsvSearcher {
  /**
   * Given a FileReader and ListStringFromRow Creator, where the file has NO headers, throws an
   * exception because the given numeric col ID to search for is out of range.
   *
   * @throws IOException if file reading fails
   */
  @Test
  public void mismatchColIDNumNoHeaders() throws IOException, FactoryFailureException {
    File starDataFile = new File("data/stars/stardata.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starDataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    SearchException thrown =
        assertThrows(SearchException.class, () -> searchStarData.search(false, "Rory", "5"));
    assertEquals(
        "Column index 5 not found. "
            + "Can search numeric column identifiers between 0 and 4 inclusive.",
        thrown.getMessage());
  }

  /**
   * Given a StringReader and ListStringFromRow Creator, where the file has headers, throws an
   * exception because the given numeric col ID to search for is out of range.
   */
  @Test
  public void mismatchColIDNumWithHeaders() throws IOException, FactoryFailureException {
    String starData = "this,is,a,header,row\nthis,is,not,a,header\nhere,is,some,more,data";
    BufferedReader buffRead = new BufferedReader(new StringReader(starData));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    SearchException thrown =
        assertThrows(SearchException.class, () -> searchStarData.search(true, "Rory", "10"));
    assertEquals(
        "Column index 10 not found. "
            + "Can search numeric column identifiers between 0 and 4 inclusive.",
        thrown.getMessage());
  }

  /**
   * Given a FileReader and ListStringFromRow Creator, and a String colID, throws an exception
   * because the given String col ID to search for is not found in the headers.
   *
   * @throws IOException if file reading fails
   */
  @Test
  public void mismatchColIDWithHeaders() throws IOException, FactoryFailureException {
    File starDataFile = new File("data/stars/stardata.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starDataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    SearchException thrown =
        assertThrows(SearchException.class, () -> searchStarData.search(true, "White", "Race"));
    assertEquals(
        "Column identifier Race not found. Valid column identifiers include "
            + "[StarID, ProperName, X, Y, Z] and numbers between 0 and 4 inclusive.",
        thrown.getMessage());
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments false (representing that
   * there are no headers), and col ID StarID. The search results in an error because you cannot
   * search a non-numeric column ID if there are no headers.
   *
   * @throws IOException if file reading fails
   */
  @Test
  public void noRowsFoundBadColID() throws IOException, FactoryFailureException {
    File dataFile = new File("data/stars/stardata.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(dataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    SearchException thrown =
        assertThrows(SearchException.class, () -> searchStarData.search(false, "119617", "StarID"));
    assertEquals(
        "Cannot search non-numeric column StarID if no headers. "
            + "Can search numeric column identifiers between 0 and 4 inclusive.",
        thrown.getMessage());
  }

  /**
   * Given a FileReader of an empty file and a ListStringFromRow creator, return an empty list
   * because no matching rows could be found.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if row searching fails
   */
  @Test
  public void emptyFileReader() throws IOException, FactoryFailureException, SearchException {
    File emptyFile = new File("data/custom/empty.csv");
    FileReader fileRead = new FileReader(emptyFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(fileRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(List.of(), searchStarData.search(true, "White", "Race"));
  }

  /**
   * Given a StringReader of an empty String and a ListStringFromRow creator, return an empty list
   * because no matching rows could be found.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if row searching fails
   */
  @Test
  public void emptyInputStringReader()
      throws IOException, FactoryFailureException, SearchException {
    String input = "";
    BufferedReader buffRead = new BufferedReader(new StringReader(input));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(List.of(), searchStarData.search(true, "White", "Race"));
  }

  /**
   * Given a StringReader of an empty String and a ListStringFromRow creator, return an empty list
   * because no matching rows could be found.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if row searching fails
   */
  @Test
  public void emptyInputStringReaderAllCols()
      throws IOException, FactoryFailureException, SearchException {
    String input = "";
    BufferedReader buffRead = new BufferedReader(new StringReader(input));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(List.of(), searchStarData.search(true, "White", "*"));
  }

  /**
   * Given a FileReader and ListStringFromRow creator, returns an empty list because no rows are
   * found with the search criteria due to case mismatch ("women" vs "Women").
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void noMatchingDataCase() throws IOException, FactoryFailureException, SearchException {
    File starDataFile = new File("data/census/postsecondary_education.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starDataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    List<String> emptyList = new ArrayList<>();
    assertEquals(emptyList, searchStarData.search(true, "women", "*"));
  }

  /**
   * Given a FileReader and ListStringFromRow creator, returns an empty list because no rows are
   * found with the search criteria.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void noMatchingDataWithColID()
      throws IOException, FactoryFailureException, SearchException {
    File starDataFile = new File("data/stars/stardata.csv");
    FileReader fileRead = new FileReader(starDataFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(fileRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(List.of(), searchStarData.search(true, "SarahKate", "ProperName"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID *. The search finds no rows that match.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void noColIdSpecifiedRowsFound()
      throws SearchException, IOException, FactoryFailureException {
    File starDataFile = new File("data/stars/ten-star.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(starDataFile));
    List<String> emptyList = new ArrayList<>();
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(emptyList, searchStarData.search(true, "sol", "*"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID *. The search value matches an item in the header, but the
   * searcher finds no rows that match because we passed 'true' for hasHeaders.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void noColIDSpecifiedHeaderMatchesTarget()
      throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/census/income_by_race_edited.csv");
    FileReader buffRead = new FileReader(dataFile);
    List<String> emptyList = new ArrayList<>();
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(emptyList, searchStarData.search(true, "Race", "*"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID StarID. The search finds one row that matches.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void colIDSpecifiedNoRowsFound()
      throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/stars/stardata.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(dataFile));
    List<String> emptyList = new ArrayList<>();
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(emptyList, searchStar.search(true, "119617", "StarID"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID *, specifying all. The search finds one row that matches.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void noColIDOneRowFound() throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/census/dol_ri_earnings_disparity.csv");
    FileReader buffRead = new FileReader(dataFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    List<List<String>> rowFound =
        List.of(List.of("RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"));
    assertEquals(rowFound, searchStarData.search(true, "Multiracial", "*"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID Data Type. The search finds one rows that match.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void colIDSpecifiedOneRowFound()
      throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/census/dol_ri_earnings_disparity.csv");
    FileReader buffRead = new FileReader(dataFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    List<List<String>> rowFound =
        List.of(List.of("RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"));
    assertEquals(rowFound, searchStar.search(true, "Multiracial", "Data Type"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID 1. The search finds one rows that match.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void colIDSpecifiedNumericOneRowFound()
      throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/census/dol_ri_earnings_disparity.csv");
    FileReader buffRead = new FileReader(dataFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    List<List<String>> rowFound =
        List.of(List.of("RI", "Multiracial", " $971.89 ", "8883.049171", " $0.92 ", "2%"));
    assertEquals(rowFound, searchStar.search(true, "Multiracial", "1"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID 1. The search finds no rows that match because the target values
   * exist in a different column.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void differentColIDSpecifiedNumericOneRowFound()
      throws IOException, FactoryFailureException, SearchException {
    File dataFile = new File("data/census/dol_ri_earnings_disparity.csv");
    FileReader buffRead = new FileReader(dataFile);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(List.of(), searchStar.search(true, "Multiracial", "0"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments false (representing that
   * there are no headers), and col ID *, specifying all. The search finds four rows that match.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void colIDSpecifiedMultiRowsFound()
      throws IOException, FactoryFailureException, SearchException {
    List<List<String>> rowsFound =
        List.of(
            List.of("1", "", "282.43485", "0.00449", "5.36884"),
            List.of("2", "", "43.04329", "0.00285", "-15.24144"),
            List.of("3", "", "277.11358", "0.02422", "223.27753"),
            List.of("118721", "", "-2.28262", "0.64697", "0.29354"));
    File dataFile = new File("data/stars/ten-star.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(dataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(rowsFound, searchStar.search(true, "", "ProperName"));
  }

  /**
   * Given FileReader and ListStringFromRow creator, and search arguments false (representing that
   * there are no headers), and col ID *, specifying all. The search finds one rows that matches,
   * which ends up being the header.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void colIDSpecifiedAllHeaderFound()
      throws IOException, FactoryFailureException, SearchException {
    List<List<String>> rowsFound = List.of(List.of("StarID", "ProperName", "X", "Y", "Z"));
    File dataFile = new File("data/stars/ten-star.csv");
    BufferedReader buffRead = new BufferedReader(new FileReader(dataFile));
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStarData = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(rowsFound, searchStarData.search(false, "StarID", "*"));
  }

  /**
   * Given StringReader and ListStringFromRow creator, and search arguments true (representing that
   * there are headers), and col ID Favorite Number. The search finds all rows which all match.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if CSV row searching fails
   */
  @Test
  public void stringReaderAllRowsFound()
      throws IOException, FactoryFailureException, SearchException {
    List<List<String>> rowsFound =
        List.of(
            List.of("Sarah", "Sewing", "5"),
            List.of("Gabby", "Volleyball", "5"),
            List.of("Stacey", "Crocheting", "5"));
    String data =
        "Name,Hobby,Favorite Number\nSarah,Sewing,5\nGabby,Volleyball,5\nStacey,Crocheting,5";
    StringReader buffRead = new StringReader(data);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseStarData = new CsvParser<>(buffRead, creatorFromRow);
    parseStarData.parseCsv();
    CsvSearcher searchStar = new CsvSearcher(parseStarData.getStoreRows());
    assertEquals(rowsFound, searchStar.search(true, "5", "Favorite Number"));
  }

  /**
   * Given FileReader with all headers the same and ListStringFromRow creator, and search arguments
   * true (representing that there are headers), and col ID *, specifying all. The search finds
   * several rows.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if row searching fails
   */
  @Test
  public void stringReaderBlankStringAllCols()
      throws IOException, FactoryFailureException, SearchException {
    File allHeadersSame = new File("data/custom/all_header_same_name.csv");
    FileReader fileRead = new FileReader(allHeadersSame);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseData = new CsvParser<>(fileRead, creatorFromRow);
    parseData.parseCsv();
    CsvSearcher searchData = new CsvSearcher(parseData.getStoreRows());
    List<List<String>> rowsFound =
        List.of(List.of("A", "B", "C", "D", "E"), List.of("A", "G", "H", "I", "E"));
    assertEquals(rowsFound, searchData.search(true, "A", "*"));
  }

  /**
   * Given FileReader with all headers the same and ListStringFromRow creator, and search arguments
   * true (representing that there are headers), and col ID Letter, which is the name of every
   * column. The search finds several rows.
   *
   * @throws IOException if file reading fails
   * @throws FactoryFailureException if row creation fails
   * @throws SearchException if row searching fails
   */
  @Test
  public void stringReaderBlankStringColSpecified()
      throws IOException, FactoryFailureException, SearchException {
    File allHeadersSame = new File("data/custom/all_header_same_name.csv");
    FileReader fileRead = new FileReader(allHeadersSame);
    CreatorFromRow<List<String>> creatorFromRow = new ListStringFromRow();
    CsvParser<List<String>> parseData = new CsvParser<>(fileRead, creatorFromRow);
    parseData.parseCsv();
    CsvSearcher searchData = new CsvSearcher(parseData.getStoreRows());
    List<List<String>> rowsFound =
        List.of(List.of("A", "B", "C", "D", "E"), List.of("A", "G", "H", "I", "E"));
    assertEquals(rowsFound, searchData.search(true, "A", "Letter"));
  }
}

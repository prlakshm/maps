package edu.brown.cs.student.sources.mocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.DataSourceException;
import edu.brown.cs.student.main.sources.mocks.StaleMockCensusSource;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the StaleMockCensusSource class. StaleMockCensusSource extends
 * the CensusSource class, and overrides its getBroadbandAccess method. This method takes a state
 * name and a county name as parameters, and returns a CensusData object with a Double representing
 * broadband access in that county (in the state). This mocked source returns the actual broadband
 * access in North Carolina and Rhode Island counties as of 9/25/23, or throws a DataSourceException
 * if the given county / state combination does not exist in North Carolina or Rhode Island.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestStaleMockCensusSource {

  /**
   * Given an empty string for state name and county name, the mock source throws a
   * DataSourceException.
   */
  @Test
  public void testEmptyStateCountyName() {
    StaleMockCensusSource mockSource = new StaleMockCensusSource();
    DataSourceException thrown =
        assertThrows(DataSourceException.class, () -> mockSource.getBroadbandAccess("", ""));
    assertEquals("County  in state  was not found.", thrown.getMessage());
  }

  /**
   * Given an empty string for county name and North Carolina as the state, the mock source throws a
   * DataSourceException.
   */
  @Test
  public void testEmptyCountyName() {
    StaleMockCensusSource mockSource = new StaleMockCensusSource();
    DataSourceException thrown =
        assertThrows(
            DataSourceException.class, () -> mockSource.getBroadbandAccess("North Carolina", ""));
    assertEquals("County  in state North Carolina was not found.", thrown.getMessage());
  }

  /**
   * Given a county that does not exist in North Carolina and North Carolina as the state, the mock
   * source throws a Data Source exception.
   */
  @Test
  public void testNotRealCountyName() {
    StaleMockCensusSource mockSource = new StaleMockCensusSource();
    DataSourceException thrown =
        assertThrows(
            DataSourceException.class,
            () -> mockSource.getBroadbandAccess("North Carolina", "Providence"));
    assertEquals("County Providence in state North Carolina was not found.", thrown.getMessage());
  }

  /**
   * Given a county that does exist in North Carolina and North Carolina as the state, the mock
   * source returns the actual broadband access level as of 9/25/23.
   *
   * @throws DataSourceException if county and state combination were not found in stale data
   */
  @Test
  public void testRealCountyNameNC() throws DataSourceException {
    StaleMockCensusSource mockSource = new StaleMockCensusSource();
    CensusData result = mockSource.getBroadbandAccess("North Carolina", "Durham");
    Double broadbandAccess = result.data();
    assertEquals(90, broadbandAccess);
  }

  /**
   * Given a county that does exist in Rhode Island and Rhode Island as the state, the mock source
   * returns the actual broadband access level as of 9/25/23.
   *
   * @throws DataSourceException if county and state combination were not found in stale data
   */
  @Test
  public void testRealCountyNameRI() throws DataSourceException {
    StaleMockCensusSource mockSource = new StaleMockCensusSource();
    CensusData result = mockSource.getBroadbandAccess("Rhode Island", "Providence");
    Double broadbandAccess = result.data();
    assertEquals(85.4, broadbandAccess);
  }
}

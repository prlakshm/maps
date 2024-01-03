package edu.brown.cs.student.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.brown.cs.student.main.sources.AcsCensusSource;
import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.CensusSource;
import edu.brown.cs.student.main.sources.DataSourceException;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the AcsCensusSource class. Because the AcsCensusSource class
 * sends real API requests to the actual census API, there are only three tests in this class to
 * avoid spamming. One test succeeds and two tests throw an exceptions. There are two mocked source
 * classes, EchoMockCensusSource and StaleMockCensusSource, which are largely used for testing the
 * broadband handler.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestAcsCensusSource {
  /**
   * This method tests the _real_ ACS API datasource. We only have three _real_ tests, one that
   * succeeds and two that fail. This call succeeds as the state and county combination is a valid
   * combination whose broadband access level can be found.
   *
   * @throws DataSourceException if the state and county combination is not valid
   */
  @Test
  public void testDurhamNorthCarolinaCanLoad_REAL() throws DataSourceException {
    CensusSource source = new AcsCensusSource();
    CensusData dataResult = source.getBroadbandAccess("North Carolina", "Durham");

    assertNotNull(dataResult);
    System.out.println(dataResult.data());
    Boolean greaterThanOrEqualZero = 0 <= dataResult.data();
    Boolean lessThanOrEqualOneHundred = 100 >= dataResult.data();
    assertTrue(greaterThanOrEqualZero);
    assertTrue(lessThanOrEqualOneHundred);
  }

  /**
   * This method tests the _real_ ACS API datasource. We only have three _real_ tests, one that
   * succeeds and two that fail. This call fails because North is not a valid state name.
   */
  @Test
  public void testBadStateFailedLoad_REAL() {
    CensusSource source = new AcsCensusSource();
    DataSourceException thrown =
        assertThrows(DataSourceException.class, () -> source.getBroadbandAccess("North", "Durham"));
    assertEquals("State North is not a valid state name.", thrown.getMessage());
  }

  /**
   * This method tests the _real_ ACS API datasource. We only have three _real_ tests, one that
   * succeeds and two that fail. This call fails because Dur is not a valid county name in North
   * Carolina.
   */
  @Test
  public void testBadCountyFailedLoad_REAL() {
    CensusSource source = new AcsCensusSource();
    DataSourceException thrown =
        assertThrows(
            DataSourceException.class, () -> source.getBroadbandAccess("North Carolina", "Dur"));
    assertEquals("County Dur not found in state North Carolina.", thrown.getMessage());
  }
}

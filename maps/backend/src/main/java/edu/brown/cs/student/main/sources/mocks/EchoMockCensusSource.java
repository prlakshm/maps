package edu.brown.cs.student.main.sources.mocks;

import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.CensusSource;

/**
 * The EchoMockCensusSource class implements the CensusSource interface and overrides its
 * getBroadbandAccess method. An object of this class is constructed with the CensusData object
 * containing a Double, and when the getBroadbandAccess method is called, an object of this class
 * returns the exact CensusData object it was constructed with. Thus, this is called our echo mock
 * source because it just echos what the user constructs it with.
 *
 * <p>This mock class enables us to test our /broadband handler without making many repeated calls
 * to an object of the AcsCensusSource class, which makes real requests to the census API.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class EchoMockCensusSource implements CensusSource {

  private final CensusData constantData;

  /**
   * The constructor for the EchoMockCensusSource class.
   *
   * @param constantData a CensusData object containing a Double representing broadband access
   */
  public EchoMockCensusSource(CensusData constantData) {
    this.constantData = constantData;
  }

  /**
   * A method that returns the exact CensusData object that the EchoMockCensusSource object was
   * constructed with.
   *
   * @param state the String representing the state to find broadband data for, although unused
   * @param county the String representing the county to find broadband data for, also unused
   * @return the CensusData object that the EchoMockCensusSource object was constructed with
   */
  @Override
  public CensusData getBroadbandAccess(String state, String county) {
    return constantData;
  }
}

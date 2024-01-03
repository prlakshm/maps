package edu.brown.cs.student.main.sources;

/**
 * This is an interface that represents a census source object, which can be implemented by a mock
 * data source or a real data source that queries the census API.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public interface CensusSource {

  /**
   * This method returns the broadband access in the given state and county Strings.
   *
   * @param state the String representing the state to get broadband access data about
   * @param county the String representing the county to get broadband access about
   * @return a CensusData object that contains a Double representing broadband access in the state /
   *     county combination passed
   * @throws DataSourceException if the census source fails to find the broadband access data
   */
  CensusData getBroadbandAccess(String state, String county) throws DataSourceException;
}

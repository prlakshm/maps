package edu.brown.cs.student.sources.mocks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.mocks.EchoMockCensusSource;
import org.junit.jupiter.api.Test;

/**
 * This is a testing class that tests the EchoMockCensusSource class. EchoMockCensusSource extends
 * the CensusSource class, and it is constructed with a CensusData object, and overrides the
 * getBroadbandAccess method. This method takes a state name and a county name as parameters, and
 * returns the same CensusData object that it was constructed with.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class TestEchoMockCensusSource {
  /**
   * Given an empty string for state name and county name, and a CensusData object containing the
   * Double 0, the mock source returns a CensusData object containing the Double 0.
   */
  @Test
  public void testCensusDataZeroEmptyBoth() {
    CensusData zero = new CensusData(Double.valueOf(0));
    EchoMockCensusSource mockSource = new EchoMockCensusSource(zero);
    assertEquals(new CensusData(Double.valueOf(0)), mockSource.getBroadbandAccess("", ""));
  }

  /**
   * Given an empty string for county name and North Carolina for state name, and a CensusData
   * object containing the Double 50, the mock source returns a CensusData object containing the
   * Double 50.
   */
  @Test
  public void testCensusDataEmptyCounty() {
    CensusData fifty = new CensusData(Double.valueOf(50));
    EchoMockCensusSource mockSource = new EchoMockCensusSource(fifty);
    assertEquals(
        new CensusData(Double.valueOf(50)), mockSource.getBroadbandAccess("North Carolina", ""));
  }

  /**
   * Given Durham for county name and North Carolina for state name, and a CensusData object
   * containing the Double 99.9999, the mock source returns a CensusData object containing the
   * Double 99.9999.
   */
  @Test
  public void testCensusDataLongDecimal() {
    CensusData longDouble = new CensusData(Double.valueOf(99.9999));
    EchoMockCensusSource mockSource = new EchoMockCensusSource(longDouble);
    assertEquals(
        new CensusData(Double.valueOf(99.9999)),
        mockSource.getBroadbandAccess("North Carolina", "Durham"));
  }

  /**
   * Given Providence for county name and Rhode Island for state name, and a CensusData object
   * containing the Double -0.01, the mock source returns a CensusData object containing the Double
   * -0.01.
   */
  @Test
  public void testCensusDataNegativeDouble() {
    CensusData negativeDouble = new CensusData(Double.valueOf(-0.01));
    EchoMockCensusSource mockSource = new EchoMockCensusSource(negativeDouble);
    assertEquals(
        new CensusData(Double.valueOf(-0.01)),
        mockSource.getBroadbandAccess("Rhode Island", "Providence"));
  }

  /**
   * Given Durham for county name and North Carolina for state name, and a CensusData object
   * containing the Double 0.00001, the mock source returns a CensusData object containing the
   * Double 0.00001.
   */
  @Test
  public void testCensusDataSameObject() {
    CensusData smallDouble = new CensusData(Double.valueOf(0.00001));
    EchoMockCensusSource mockSource = new EchoMockCensusSource(smallDouble);
    assertEquals(smallDouble, mockSource.getBroadbandAccess("North Carolina", "Durham"));
  }
}

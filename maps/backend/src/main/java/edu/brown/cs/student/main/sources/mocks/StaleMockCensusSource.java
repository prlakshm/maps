package edu.brown.cs.student.main.sources.mocks;

import edu.brown.cs.student.main.sources.CensusData;
import edu.brown.cs.student.main.sources.CensusSource;
import edu.brown.cs.student.main.sources.DataSourceException;
import java.util.HashMap;
import java.util.List;

/**
 * The StaleMockCensusSource class implements the CensusSource interface and overrides its
 * getBroadbandAccess method. An object of this class is constructed with nothing, but in the
 * constructor, the field broadbandAccess is filled with stale ACS broadband data from 9/25/23 in
 * North Carolina and Rhode Island. When the getBroadbandAccess method is called, an object of this
 * class returns the stale broadband access data if the given state and county combination exists in
 * North Carolina or Rhode Island. Otherwise, this source returns a DataSourceException.
 *
 * <p>This mock class enables us to test our /broadband handler without making many repeated calls
 * to an object of the AcsCensusSource class, which makes real requests to the census API.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class StaleMockCensusSource implements CensusSource {

  private final HashMap<String, String> broadbandAccess;

  /**
   * The constructor for the StateMockCensusSource class, which fills the broadbandAccess field with
   * stale data from 9/25/23 for North Carolina and Rhode Island counties.
   */
  public StaleMockCensusSource() {
    List<List<String>> stateData =
        List.of(
            List.of("NAME", "S2802_C03_022E", "state", "county"),
            List.of("Alamance County, North Carolina", "83.8", "37", "001"),
            List.of("Burke County, North Carolina", "80.5", "37", "023"),
            List.of("Catawba County, North Carolina", "81.4", "37", "035"),
            List.of("Lincoln County, North Carolina", "88.6", "37", "109"),
            List.of("Nash County, North Carolina", "80.7", "37", "127"),
            List.of("Onslow County, North Carolina", "87.0", "37", "133"),
            List.of("Randolph County, North Carolina", "84.5", "37", "151"),
            List.of("Robeson County, North Carolina", "69.2", "37", "155"),
            List.of("Union County, North Carolina", "92.1", "37", "179"),
            List.of("Wayne County, North Carolina", "85.8", "37", "191"),
            List.of("Brunswick County, North Carolina", "94.4", "37", "019"),
            List.of("Buncombe County, North Carolina", "83.6", "37", "021"),
            List.of("Cabarrus County, North Carolina", "88.0", "37", "025"),
            List.of("Caldwell County, North Carolina", "76.4", "37", "027"),
            List.of("Carteret County, North Carolina", "90.4", "37", "031"),
            List.of("Chatham County, North Carolina", "88.4", "37", "037"),
            List.of("Cleveland County, North Carolina", "72.0", "37", "045"),
            List.of("Craven County, North Carolina", "79.1", "37", "049"),
            List.of("Cumberland County, North Carolina", "87.6", "37", "051"),
            List.of("Davidson County, North Carolina", "85.2", "37", "057"),
            List.of("Durham County, North Carolina", "90.0", "37", "063"),
            List.of("Forsyth County, North Carolina", "85.7", "37", "067"),
            List.of("Franklin County, North Carolina", "85.8", "37", "069"),
            List.of("Gaston County, North Carolina", "78.4", "37", "071"),
            List.of("Guilford County, North Carolina", "89.2", "37", "081"),
            List.of("Harnett County, North Carolina", "76.5", "37", "085"),
            List.of("Henderson County, North Carolina", "86.3", "37", "089"),
            List.of("Iredell County, North Carolina", "89.8", "37", "097"),
            List.of("Johnston County, North Carolina", "83.1", "37", "101"),
            List.of("Mecklenburg County, North Carolina", "89.6", "37", "119"),
            List.of("Moore County, North Carolina", "81.3", "37", "125"),
            List.of("New Hanover County, North Carolina", "89.2", "37", "129"),
            List.of("Orange County, North Carolina", "89.1", "37", "135"),
            List.of("Pitt County, North Carolina", "75.5", "37", "147"),
            List.of("Rockingham County, North Carolina", "74.7", "37", "157"),
            List.of("Rowan County, North Carolina", "85.8", "37", "159"),
            List.of("Surry County, North Carolina", "80.6", "37", "171"),
            List.of("Wake County, North Carolina", "92.0", "37", "183"),
            List.of("Wilkes County, North Carolina", "77.6", "37", "193"),
            List.of("Wilson County, North Carolina", "71.9", "37", "195"),
            List.of("Kent County, Rhode Island", "84.1", "44", "003"),
            List.of("Providence County, Rhode Island", "85.4", "44", "007"),
            List.of("Newport County, Rhode Island", "90.1", "44", "005"),
            List.of("Washington County, Rhode Island", "92.8", "44", "009"));
    broadbandAccess = new HashMap<>();
    for (List<String> row : stateData) {
      broadbandAccess.put(row.get(0), row.get(1));
    }
  }

  /**
   * A method that returns a CensusData object representing the stale broadband access level from
   * 9/25/23 for a state / county combination in North Carolina or Rhode Island. Otherwise, throws a
   * DataSourceException.
   *
   * @param state the String representing the state query parameter from the handler
   * @param county the String representing the county query parameter from the handler
   * @return CensusData object containing a Double representing the stale broadband access level
   * @throws DataSourceException if the state / county combination does not exist in NC or RI
   */
  @Override
  public CensusData getBroadbandAccess(String state, String county) throws DataSourceException {
    String fullCountyName = county + " County, " + state;
    String response = broadbandAccess.get(fullCountyName);
    if (response == null) {
      throw new DataSourceException("County " + county + " in state " + state + " was not found.");
    }
    return new CensusData(Double.valueOf(response));
  }
}

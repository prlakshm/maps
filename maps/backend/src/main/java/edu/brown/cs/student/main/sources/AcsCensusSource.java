package edu.brown.cs.student.main.sources;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import okio.Buffer;

/**
 * The AcsCensusSource class implements the CensusSource interface and overrides its
 * getBroadbandAccess method. An object of this class is constructed with no arguments, and when the
 * getBroadbandAccess method is called, an object of this class makes an API call to the census API
 * to find the broadband access level of the given state and county, and returns this level wrapped
 * in a CensusData object. If the broadband access level cannot be found for some reason, this
 * method (or a method that it calls) throws a DataSourceException.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class AcsCensusSource implements CensusSource {
  private final HashMap<String, String> stateIds;

  /** The constructor for the AcsCensusSource class, sets the stateIds field to an empty HashMap. */
  public AcsCensusSource() {
    stateIds = new HashMap<>();
  }

  /**
   * This method creates a http connection for the caller methods to connect to the ACS API.
   *
   * @param requestURL the URL to make an API request to
   * @return the Http connection url
   * @throws DataSourceException if the connection fails
   * @throws IOException if an I/O exception occurs
   */
  private static HttpURLConnection connect(URL requestURL) throws DataSourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if (!(urlConnection instanceof HttpURLConnection)) {
      throw new DataSourceException("unexpected: result of connection wasn't HTTP");
    }
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect();
    if (clientConnection.getResponseCode() != 200) {
      throw new DataSourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    }
    return clientConnection;
  }

  /**
   * This method returns the state code associated with the given String state. The first time this
   * method is called while the server is running, this method stores all state codes in the
   * stateIds field, and references that HashMap on future calls to this method.
   *
   * @param state the String representing the state to find the ID for
   * @return the String representing the ID of the given state
   * @throws DataSourceException if the state is not found in the ACS data
   */
  public String getStateCode(String state) throws DataSourceException {
    try {
      if (stateIds.isEmpty()) {
        URL requestURL =
            new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
        HttpURLConnection clientConnection = connect(requestURL);
        Moshi moshi = new Moshi.Builder().build();
        Type listOfListOfString = Types.newParameterizedType(List.class, List.class, String.class);
        JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfListOfString);
        List<List<String>> data =
            adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        for (List<String> row : data) {
          stateIds.put(row.get(0), row.get(1));
        }
      }
      String stateCode = stateIds.get(state);
      if (stateCode == null) {
        throw new DataSourceException("State " + state + " is not a valid state name.");
      }
      return stateCode;
    } catch (MalformedURLException e) {
      throw new DataSourceException("Malformed URL: Connection failed.");
    } catch (IOException e) {
      throw new DataSourceException(e.getMessage());
    }
  }

  /**
   * This method returns the county code of the given county in the given state by calling the
   * census API.
   *
   * @param state the String representing the state where the county is in
   * @param stateCode the String representing the state code of the state
   * @param county the String representing the county name
   * @return a String representing the county code of the given county in the given state
   * @throws DataSourceException if the given county is not found in the state
   */
  public String getCountyCode(String state, String stateCode, String county)
      throws DataSourceException {
    try {
      String fullCounty = county + " County, " + state;
      URL requestURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode);
      HttpURLConnection clientConnection = connect(requestURL);
      Moshi moshi = new Moshi.Builder().build();
      Type listOfListOfString = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfListOfString);
      List<List<String>> data =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      for (List<String> row : data) {
        if (row.get(0).equals(fullCounty)) {
          return row.get(2);
        }
      }
      throw new DataSourceException("County " + county + " not found in state " + state + ".");
    } catch (IOException e) {
      throw new DataSourceException(e.getMessage());
    }
  }

  /**
   * This method finds the broadband access level of the given state and county names. The
   * intermediate steps include finding the state's code and the county's code to use in a final
   * query to the census API. This broadband access level is returned in a CensusData object as the
   * data field. If the state / county combination is not found, throws a DataSourceException.
   *
   * @param state the String representing the state of the county for broadband level to be found
   * @param county the String representing the county for broadband level to be found
   * @return a CensusData object wrapping a Double representing broadband access level in the given
   *     state / county combination
   * @throws DataSourceException if the state / county combination is not found in census data
   */
  @Override
  public CensusData getBroadbandAccess(String state, String county) throws DataSourceException {
    String stateCode = getStateCode(state);
    String countyCode = getCountyCode(state, stateCode, county);
    try {
      URL requestURL =
          new URL(
              "https",
              "api.census.gov",
              "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                  + countyCode
                  + "&in=state:"
                  + stateCode);
      HttpURLConnection clientConnection = connect(requestURL);
      Moshi moshi = new Moshi.Builder().build();
      Type listOfListOfString = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listOfListOfString);
      List<List<String>> data =
          adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      String percent = data.get(1).get(1);
      Double numPercent = Double.parseDouble(percent);
      return new CensusData(numPercent);
    } catch (IOException e) {
      throw new DataSourceException(e.getMessage());
    }
  }
}

package edu.brown.cs.student.main.server;

import java.util.Collections;
import java.util.List;

/**
 * This CsvDataWrapper class wraps a List of List of String representing the CSV data parsed by
 * loadcsv, which can be viewed or searched with viewcsv and searchcsv endpoints respectively. This
 * class also has the field loadedInPast, representing whether any CSV data has been loaded.
 *
 * @author sarahridley juliazdzilowska
 * @version 1.0
 */
public class CsvDataWrapper {
  private final List<List<String>> csvData;
  private boolean loadedInPast;

  /**
   * The constructor for the CsvDataWrapper class.
   *
   * @param csvData the List of List of String representing the parsed CSV data
   * @param loadedInPast the boolean representing whether any CSV data has been loaded
   */
  public CsvDataWrapper(List<List<String>> csvData, boolean loadedInPast) {
    this.csvData = csvData;
    this.loadedInPast = loadedInPast;
  }

  /**
   * A setter method allowing the caller to reset the value of csvData (cleared and then set).
   *
   * @param data the List of List of String to replace the contents of csvData with
   */
  public void setData(List<List<String>> data) {
    this.csvData.clear();
    this.csvData.addAll(data);
  }

  /**
   * A getter method allowing the caller (viewcsv or searchcsv) to have an unmodifiable version of
   * the csvData field.
   *
   * @return an unmodiiable List of List of String, allowing the caller to view csvData
   */
  public List<List<String>> getData() {
    return Collections.unmodifiableList(this.csvData);
  }

  /**
   * A setter method allowing the caller to set the value of the boolean loadedInPast field.
   *
   * @param loaded the new boolean value for the loadedInPast field
   */
  public void setLoaded(boolean loaded) {
    this.loadedInPast = loaded;
  }

  /**
   * A getter method allowing the caller to check whether a CSV has been loaded by a call to this
   * server's /loadcsv endpoint.
   *
   * @return a boolean representing whether CSV data has been loaded
   */
  public boolean getLoaded() {
    return this.loadedInPast;
  }
}

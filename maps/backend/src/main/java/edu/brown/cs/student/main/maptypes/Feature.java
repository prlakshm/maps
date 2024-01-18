package edu.brown.cs.student.main.maptypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 * This Java file defines a record class named "Feature" that represents a feature in a map.
 * Each feature consists of a type, geometry information, and properties.
 * The Geometry class defines the geometry of the feature, including its type and coordinates.
 * The Properties class stores additional properties of the feature, such as state, city, name, and various identifiers.
 * This class is designed to be used for working with map-related data and provides accessors for the feature's attributes.
 * It also includes methods to retrieve immutable lists and maps to ensure data integrity.
 */
public class Feature {
  private String type;
  private Geometry geometry;
  private Properties properties;

  // Getter methods for fields
  public String getType(String type) {
    return this.type;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public Properties getProperties() {
    return this.properties;
  }
  public static class Geometry {
    private String type;
    private List<List<List<List<Double>>>> coordinates;

    // Accessor method for retrieving the type of geometry.
    public String getType() {
      return type;
    }

    // Accessor method for retrieving the immutable list of coordinates.
    public List<List<List<List<Double>>>> getCoordinates() {
      return Collections.unmodifiableList(coordinates);
    }
  }

  // Accessor methods for retrieving various properties.
  public static class Properties {
    private String state;
    private String city;
    private String name;
    private String holc_id;
    private String holc_grade;
    private int neighborhood_id;
    private Map<String,String> area_description_data;

    public String getState() {
      return state;
    }

    public String getCity() {
      return city;
    }

    public String getName() {
      return name;
    }

    public String getHolc_id() {
      return holc_id;
    }

    public String getHolc_grade() {
      return holc_grade;
    }

    public int getNeighborhood_id() {
      return neighborhood_id;
    }

    // Accessor method for retrieving the immutable map of area description data.
    public Map<String, String> getArea_description_data() {
      return area_description_data;
    }
  }

}


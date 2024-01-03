/*
 * This Java file defines a class named "FeatureCollection" that represents a collection of map features.
 * It contains a list of features and provides an accessor method for retrieving these features.
 * The class ensures that the list of features is immutable to maintain data integrity.
 */

package edu.brown.cs.student.main.maptypes;

import java.util.Collections;
import java.util.List;

public class FeatureCollection {
    // Instance variable to store a list of map features.
    private List<Feature> features;

    // Accessor method for retrieving an immutable list of features.
    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }
}
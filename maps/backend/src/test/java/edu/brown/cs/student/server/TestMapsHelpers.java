// TestMapsHelpers.java

// This class contains JUnit tests for the MapsHelpers class.
package edu.brown.cs.student.server;

// Import necessary libraries and modules
import com.google.common.cache.CacheBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.maptypes.Feature;
import edu.brown.cs.student.main.maptypes.FeatureCollection;
import edu.brown.cs.student.main.server.BoundaryBoxHandler;
import edu.brown.cs.student.main.server.SearchAreasHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class contains JUnit tests for the MapsHelpers class.
 */
public class TestMapsHelpers {

    // Constants for test configuration
    final static int TRIAL_LENGTH = 100;
    final static int INDEX_UPPER_BOUND = 8878; // Size of geolength features in JSON

    //filepaths to geojsons
    final static String ACTUAL_PATH = "C:\\Users\\prana\\Documents\\GitHub\\maps-prlakshm-tbonas\\maps\\backend\\data\\geojson\\fullDownload.geojson";

    // Variables for test setup
    private static FeatureCollection featureCollection;
    private static BoundaryBoxHandler boundaryBoxHandler;
    private static SearchAreasHandler searchAreasHandler;

    /**
     * Setup method executed once before all tests.
     *
     * @throws IOException if an I/O error occurs while reading the GEOJSON content.
     */
    @BeforeAll
    public static void setupOnce() throws IOException {
        // Read the GEOJSON content from the file
        String jsonContent = new String(Files.readAllBytes(Paths.get(ACTUAL_PATH)));

        // Build Moshi instance
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FeatureCollection> adapter = moshi.adapter(FeatureCollection.class);

        // Deserialize the GEOJSON content into a FeatureCollection
        featureCollection = adapter.fromJson(jsonContent);
        boundaryBoxHandler = new BoundaryBoxHandler(ACTUAL_PATH,
                CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES));
        searchAreasHandler = new SearchAreasHandler(ACTUAL_PATH,
                CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES));
    }

    /**
     * Test for checking if a feature is within a valid boundary.
     */
    @Test
    public void testFeatureWithinBoundaryValid() {
        // Create a test feature with known coordinates within the bounding box
        List<List<List<List<Double>>>> testCoordinates = List.of(
                List.of(
                        List.of(List.of(-86.756777, 33.497543),
                                List.of(-86.75692, 33.495789),
                                List.of(-86.762018, 33.491924),
                                List.of(-86.756777, 33.497543))
                )
        );

        // Check if the test feature is within the bounding box
        boolean ifIncluded = boundaryBoxHandler.isFeatureWithinBoundingBox(testCoordinates, 33.472256, 33.501794, -86.768353, -86.725257);

        // Assert that the test feature is indeed within the bounding box
        assertTrue(ifIncluded);
        System.out.print(featureCollection.getFeatures().size());
    }

    /**
     * Test for checking if a feature is within an empty boundary.
     */
    @Test
    public void testFeatureWithinBoundaryEmpty() {
        // Create a test feature with known coordinates within the bounding box
        List<List<List<List<Double>>>> testCoordinates = List.of();

        // Check if the test feature is within the bounding box
        boolean ifIncluded = boundaryBoxHandler.isFeatureWithinBoundingBox(testCoordinates, 33.472256, 33.501794, -86.768353, -86.725257);

        // Assert that the test feature is indeed within the bounding box
        assertTrue(ifIncluded);
    }

    /**
     * Test for checking if a feature is within an invalid boundary.
     */
    @Test
    public void testFeatureWithinBoundaryInvalid() {
        // Create a test feature with known coordinates within the bounding box
        List<List<List<List<Double>>>> testCoordinates = List.of(
                List.of(
                        List.of(List.of(-88.3, 33.497543),
                                List.of(-86.75692, 33.495789),
                                List.of(-86.762018, 33.491924),
                                List.of(-86.756777, 33.497543))
                )
        );

        // Check if the test feature is within the bounding box
        boolean ifIncluded = boundaryBoxHandler.isFeatureWithinBoundingBox(testCoordinates, 33.472256, 33.501794, -86.768353, -86.725257);

        // Assert that the test feature is indeed within the bounding box
        assertFalse(ifIncluded);
    }

    /**
     * Test for fuzz testing the getCoordinates method.
     */
    @Test
    public void getCoordinatesFuzzTest() {
        // Initialize the FeatureCollection with data (you can load your data here)
        // featureCollection = ...;

        for (int i = 0; i < TRIAL_LENGTH; i++) {
            int randomIndex = (int) (Math.random() * INDEX_UPPER_BOUND);

            // Extract a sublist of featureCollection based on the random index
            List<Feature> sublist = featureCollection.getFeatures().subList(0, randomIndex);

            // Perform some checks or operations on the sublist
            // For example, you can call the getCoordinates method
            List<List<Double>> coordinates = searchAreasHandler.getCoordinates(sublist);

            // Add your assertions here based on the expected behavior
            // For example, check if the coordinates are not empty
            assertTrue(!coordinates.isEmpty());
        }
    }

    /**
     * Test for the getCoordinates method with an empty feature list.
     */
    @Test
    public void emptyGetCoordinatesTest(){
        // Extract a sublist of featureCollection based on the random index
        List<Feature> sublist = new ArrayList<>();

        // Perform some checks or operations on the sublist
        // For example, you can call the getCoordinates method
        List<List<Double>> coordinates = searchAreasHandler.getCoordinates(sublist);

        // Add your assertions here based on the expected behavior
        // For example, check if the coordinates are empty
        assertTrue(coordinates.isEmpty());
    }

    /**
     * Generates random input strings.
     *
     * @param max_length Maximum length of the string.
     * @return A string of random ASCII characters of random length.
     */
    public String getRandomInt(int max_length) {
        final ThreadLocalRandom r = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();


        // Generates a random string length
        int length = r.nextInt(max_length + 1);
        for (int iCount = 0; iCount < length; iCount++) {
            // Generates random characters
            int code = r.nextInt();
            sb.append((char) code);
        }

        return sb.toString();
    }
}







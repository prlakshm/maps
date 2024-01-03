package edu.brown.cs.student.main.server;

import com.squareup.moshi.Moshi;

/**
 * A generic class for reading and deserializing JSON strings into Java objects using Moshi library.
 *
 * @param <T> The type of the Java object to deserialize the JSON into.
 */
public class JsonReader<T> {

  // Store the JSON string and the class type for deserialization
  String json;
  Class<T> objectType;

  /**
   * Constructs a JsonReader with the specified JSON string and class type.
   *
   * @param jsonString The JSON string to be deserialized.
   * @param classT     The class type to deserialize the JSON into.
   */
  public JsonReader(String jsonString, Class<T> classT) {
    this.json = jsonString;
    this.objectType = classT;
  }

  /**
   * Deserializes the stored JSON string into the provided class type.
   *
   * @return The Java object deserialized from the JSON string.
   * @throws InstantiationError If there is an error converting the JSON to an object.
   */
  public T fromJson() throws InstantiationError {
    try {
      Moshi moshi = new Moshi.Builder().build();

      // Parse JSON into the provided class type
      T object = moshi.adapter(this.objectType).fromJson(this.json);

      return object;

    } catch (Exception e) {
      throw new InstantiationError("Error in converting JSON to object!");
    }
  }
}
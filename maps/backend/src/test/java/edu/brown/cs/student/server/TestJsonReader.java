package edu.brown.cs.student.server;

import edu.brown.cs.student.server.jsonTypes.Person;

import edu.brown.cs.student.main.server.JsonReader;
import org.junit.jupiter.api.Test;
import static org.testng.AssertJUnit.assertEquals;

/**
 * This class tests json reader
 * 
 * @author sarahridley juliazdzilowska prlakshm
 * @version 3.0
 */
public class TestJsonReader {

  /**
   * test converts jsonString to int
   * @throws InstantiationError if error parsing json
   */
  @Test
  public void testJsonInteger() throws InstantiationError {
    String jsonString = "31";
    JsonReader<Integer> reader = new JsonReader<>(jsonString, Integer.class);
    Integer num = reader.fromJson();

    assertEquals(num, Integer.valueOf(31));
  }

  /**
   * test converts jsonString to person
   * @throws InstantiationError if error parsing json
   */
  @Test
  public void testJsonPerson() throws InstantiationError {
    String jsonString = "{\"name\":\"John\",\"age\":30}";
    JsonReader<Person> reader = new JsonReader<>(jsonString, Person.class);
    Person person = reader.fromJson();

    assertEquals("John", person.getName());
    assertEquals(30, person.getAge());
  }



}

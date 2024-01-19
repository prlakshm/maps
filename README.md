# maps-prlakshm-tbonas

**Project Name:** Maps

**Team Members and Contributions:** 
<br>Thalia Bonas (frontend) 
<br>Pranavi Lakshminarayanan (frontend, backend, and deployment) 
<br>Julia Zdzilowska (frontend and backend)
<br>Sarah Ridley (backend)

<br>**Link to Deployed Webpage:** https://prlakshm.github.io/maps/

<br>**Estimated Completion Time:** 45 hours

### 1. Functionality/Design

Our program is divided into two separate packages, our frontend and our backend. For our backend implementation, we used, and built upon our code from the Server Project, the breakdown of which is explained below. The backend server is run using an AWS ubuntu ec2 instance and nginx. The server can be accessed directly at https://cs32customserver.com/maps/. 

For our front end, we built off of our the gearup for this sprint, adding functionality for user stories 1 and 2. The core components of our front end are outlined below.

Our Front end consists of the following files, their purpose and relationships are outlines briefly below:

App.tsx:
This file is the main entry point for the application. It renders the App component, which is the root of the application. This component contains two main sections, a Mapbox component and a REPL component, allowing users to interact with maps and a command-line interface.

ControlledInput.tsx:
This file defines a controlled input component for the user's command box input and allows for the input value to be managed externally. It accepts props for value, setValue, ariaLabel, and onKeyDown, making it flexible and customizable. ControlledInput includes an onchange handler to update the input's value when it changes. The file also supports an optional onkeyDown prop for handling keydown events.

GeoJSONContext.tsx:
This file defines a React context for managing GeoJSON data. The GeoJSONContext defines an interface for managing GeoJSON data and provides a context to share this data between components. It uses the React context API to share GeoJSON data throughout the application.

index.tsx:
This file is the entry point for rendering the application.
It renders the App component within the root element in the HTML file.

Mapbox.tsx:
This file defines the main Mapbox component for displaying maps and geospatial data. The Mapbox component manages the map view, geocoding, and interaction with geospatial data. It contains features like setting the map view state, handling user inputs, and interacting with overlays. It also includes the feature for viewing county and state upon clicking on a spot on the map.

overlay.ts:
This file provides overlay data and styling for map layers. It contains the overlayData function that converts raw JSON data into a GeoJSON FeatureCollection and defines styles for map layers.The overlayData function ensures that the data is converted to a GeoJSON FeatureCollection.
We also used the isFeatureCollection type guard function for type safety.

REPL.tsx:
This part defines a React component called REPL that provides a Read-Eval-Print Loop (REPL) interface for users to input commands, display command history, and view the results of each command. It manages the history, display mode, and command results. This component renders two subcomponents: REPLHistory and REPLInput.

REPLHistory.tsx:
This component displays the command history and corresponding results. It has options to display in either "brief" or "verbose" mode. It receives a list of history items, a display mode, a map of command results, and an ARIA label as props. There is also an option to view all the possible commands upon clicking on the "Command History " header. You can still scroll from the command instructions to see the history elements.

REPLInput.tsx:
This component handles user input and executes commands. It allows users to enter commands and has options to change the display mode. It also registers various commands and processes user input accordingly.

The use of state management via React's useState hooks allows us to maintain the state of various variables, such as command history, user input, and the
current mode (verbose or brief). This design enables real-time updates to the UI based on user input and interactions. More specific examples of our use of
useState hooks includes:

In App.tsx, where we used the useState hook to manage the coordinates state, which represents the geographic coordinates selected by the user. In ControlledInput.tsx, a useState hook is employed to bind the input value to the provided value prop.

In Mapbox.tsx, we used a useState hook to manage the map view state, overlay data, latitude and longitude inputs, and highlighted coordinates. You can also view the state and county you clicked from a request to the FCC Area API.

We also included aria labels throughout our code, such as in ControlledInput and REPLHistory to enhance user accessibility. We included keyboard shortcuts, such as submitting a command by pressing "Enter" and navigating to the command input by clicking "Ctrl+b". You can navigate to the latitude input by pressing "Ctrl+q", press "Tab" to navigate to the longitude input, then press "Enter" to submit the coordinates.

Runtime and space optimizations are evident in our construction of components
such as ControlledInput, which were designed independently to be reused across
the application, promoting clean and maintainable code. Efficiently updating the
command history and UI in response to user actions, minimized unnecessary
re-renders and data processing.

An explanation of our design choices for our backend package is written below. Our functionality and design is adopting from our server project. Thus, the following explanantions are just for the classes we added for this sprint to our server package. Overall, the classes we added handle HTTPS requests, deserializing GEOJSON data, optionally caching results, and responding with success or error messages. The code uses Guava's CacheBuilder for caching and Moshi for JSON deserialization. It is designed to efficiently handle GEOJSON data and search queries.

BoundaryBoxHandler.java
This class handles HTTPS requests related to bounding box queries on GEOJSON data. It extracts features within a specified bounding box and can optionally cache the results. The class uses lists and optional caching with Guava's CacheBuilder. Caching is also implemented to optimize runtime performance by avoiding redundant calculations when the same bounding box is queried multiple times.

JsonReader.java
This class provides a generic way to read and deserialize JSON strings into Java objects using the Moshi library. This class uses the Moshi library to parse JSON strings into Java objects. It acts as a generic class to support deserialization of various Java object types from JSON.

RedliningDataHandler.java:
This class handles HTTPS requests related to loading and caching GEOJSON data for redlining analysis. It loads GEOJSON data and optionally caches it. This class uses Guava's CacheBuilder for optional caching. Deserializes GEOJSON data using Moshi. Implements a caching mechanism to store and retrieve GEOJSON data for redlining analysis.Caching is implemented throughout the class to optimize runtime performance by avoiding redundant data loading when the same data is requested multiple times.

SearchAreasHandler.java:
This class handles HTTPS requests related to searching areas in GEOJSON data based on a provided keyword. It extracts features that match the keyword and can optionally cache the results. The class uses Guava's CacheBuilder for optional caching, deserializes GEOJSON data using Moshi and implements a caching mechanism to store and retrieve search results based on the keyword.

We also created a maps package which contained the following classes for the maps portion of our code. Overall, these classes are designed to represent map features and collections of map features while enforcing data immutability to maintain data integrity. The use of Java's Collections.unmodifiableList and Collections.unmodifiableMap makes the data structures immutable, which is a good practice to prevent unintended changes to the data.

FeatureCollection.java
This class represents a collection of map features and ensures that the list of features is immutable to maintain data integrity. It provides an accessor method for retrieving an immutable list of features. Essentially, this class uses an instance variable to store a list of map features and provides a getFeatures method that returns an immutable list of features using Collections.unmodifiableList. It ensures that the list of features is immutable, preventing unintentional modifications.

Feature.java:
This class represents a feature in a map and includes information about its type, geometry, and properties. It provides accessors for various attributes of the feature, ensuring that lists and maps are immutable. This class uses a record class named Feature that includes nested classes for Geometry and Properties. Each class has accessor methods for retrieving attributes and utilizes Collections.unmodifiableList to make the geometry coordinates list and the area description data map immutable.

### 2. Errors/Bugs:

Though we extensivly tested out code, there are some bugs that were beyond our expertise to fix. The backend server can be easily overloaded. Because of this, we recommend typing simple commands into the input box. 

**IMPORTANT: The API handling county and state geolocation requests can be overloaded if there are too many frequent clicks on the map. Because of this, the tooltip might not always appear or take a while to appear if there are too many consecutive clicks. A message prints to cosole when a location is clicked without a county associated (ex. water mass clicked).**


### 3. Testing:

We created separate testing files using both Jest for unit testing and playwright for integration testing.

The tests we added to our front end from our previous sprint were designed to help ensure the correctness and reliability of the application by covering various aspects, including input handling, command execution, error handling, and the functionality of external components like Mapbox and React components. Here's an explanation of each test we added and how it ensures that a part of the program works:

Basic Functionality and Element Visibility Tests:
on page load, i see an input bar: This test ensures that the input bar is visible when the page loads.
after I type into the input box, its text changes: It verifies that the text in the input box changes when text is typed into it.
input field for commands is functional: This test checks if the input field for commands is visible and whether it correctly reflects the input.
input field for commands is functional before entering a command: It ensures that the input field is visible and correctly represents an empty input.
Button Functionality Tests:
on page load, i see a button: This test confirms the visibility of a button on page load.
after I click the button, my command gets pushed: It tests whether clicking the button increases the command count as expected.

Invalid Command Tests:
submitting an invalid command adds it to the history: This test checks if submitting an invalid command adds it to the command history as expected.
submitting an empty command doesn't add it to history: It verifies that an empty command does not get added to the history. submitting various commands adds the appropriate ones to history: This test validates whether various commands result in the expected history entries. submitting a large number of commands updates count: It tests whether submitting a large number of commands correctly updates the count on the button.

Register Command Tests:

Mapbox Component Tests (Separate Test Suite):
Mapbox loads and displays correctly: This test verifies if the Mapbox component loads and displays Providence initially. Geocoding coordinates updates the map view: It tests whether entering coordinates and clicking the "Geocode" button updates the map view as expected and map moves. Zoom in and Zoom out: This tests that the zoom in and zoom out buttons change the zoom level of the map.

Serachareas Component Tests (Separate Test Suite):
Valid Search Query: Tests that a valid searchareas command moves the map to see all highlighted dots. Invalid Search Query: Tests that map doesn't move when searchareas command is invalid. Test Multiple Searches: Tests to see map moves multiple times with multiple valid
searchareas entries. Test Same Searches: Tests that the map doesn't move when the same searchareas keyword is searched. Broadband In Tandem: Tests that seachareas still moves the map when used after broadband command.

Broadband Component Tests (Separate Test Suite):
Valid Broadband Queries: Tests to see correct broadband percent is outputted after multiple valid uses of broadband command. Invalid Broadband Query: Tests that appropriate error messages are shown when broadband commands are invalid.

For our back end, we added the following test files to ensure our added functionality described in our design portion works correctly. Overall, these tests cover various scenarios, including valid inputs, invalid inputs, caching behavior, and fuzz testing, to ensure the correctness and robustness of the RedlingingDataHandler, BoundaryBoxHandler, and SearchAreasHandler classes.

TestBoundaryBoxHandler
testBoundaryBoxCache: This test checks if the caching mechanism in the BoundaryBoxHandler is functioning correctly. It sends two requests with the same parameters, and the test ensures that the second request retrieves the result from the cache instead of making a redundant request.
testBoundaryBoxCacheInvalidRequest: This test checks how the handler handles an invalid request, specifically when the request contains invalid latitude values. It ensures that the cache is not used when the request is invalid.
testBoundaryBoxNoCache: This test checks the behavior of the BoundaryBoxHandler when caching is disabled. It sends a request with valid parameters and ensures that the result is not cached.
testBoundaryBoxMissingParameters: This test checks how the handler handles requests with missing parameters, specifically minLat, maxLat, minLng, and maxLng. It ensures that the handler responds with an error when parameters are missing.
testBoundaryBoxFuzz: This is a fuzz testing scenario where random coordinate values are generated and used as parameters for multiple requests to the BoundaryBoxHandler. This test helps ensure the robustness of the handler when receiving various input values.
All tests are also tested with mocked geojson data.

TestMapsHelpers
testFeatureWithinBoundaryValid: This test checks if the BoundaryBoxHandler can correctly determine if a given feature's coordinates are within a valid boundary. It tests the handler's spatial filtering capabilities.
testFeatureWithinBoundaryEmpty: This test checks how the handler handles a feature with no coordinates. It ensures that an empty feature is considered within the boundary.
testFeatureWithinBoundaryInvalid: This test checks how the handler handles a feature with coordinates that are outside the valid boundary. It ensures that the handler correctly identifies the feature as not within the boundary.
getCoordinatesFuzzTest: This test is a fuzz test that randomly selects features from a FeatureCollection and extracts their coordinates using the getCoordinates method of the SearchAreasHandler. The test asserts that the coordinates obtained are not empty.
emptyGetCoordinatesTest: This test checks the behavior of the getCoordinates method when an empty feature list is provided. It ensures that the method returns an empty list as expected.
getRandomInt: This method generates random input strings, but it is not directly used in the test cases.

TestSearchAreasHandler
testSearchAreasCache: This test checks the caching functionality in the SearchAreasHandler. It sends requests with keywords and checks if the results are cached for subsequent requests with the same keyword.
testSearchAreasCacheInvalidRequest: This test checks how the handler handles invalid requests, specifically requests without the keyword parameter or with an empty keyword. It ensures that the handler responds with an error when the requests are invalid and that the cache is not used.
testSearchAreasNoCache: This test checks the behavior of the SearchAreasHandler when caching is disabled. It sends a request with a keyword and ensures that the result is not cached.
testSearchAreasFuzz: This is a fuzz testing scenario where random keywords are generated and used as parameters for multiple requests to the SearchAreasHandler. This test helps ensure the robustness of the handler when receiving various input values.
getRandomString: This method generates random strings, but it is not directly used in the test cases.
showDetailsIfError: This method is a helper function used to display details if an error occurs in the response body, but it is not directly used in the test cases.
All tests are also tested with mocked geojson data.

TestRedliningDataHandler
testRedliningDataHandlerCache: This test checks the caching functionality of the RedliningDataHandler class.
testRedliningDataHandlerNoCache: This test checks the behavior of the RedliningDataHandler class when caching is disabled.
testRedliningDataHandlerCacheMiss: This test checks the scenario of a cache miss in the RedliningDataHandler class.
These tests ensure that the caching functionality in the RedliningDataHandler class is working correctly, and they cover scenarios with caching enabled, disabled, and cache misses. The tests also make use of a JSON adapter to parse response bodies and include error handling to print details in case of errors. The setup and tearDown methods are used to set up and clean up the environment before and after each test.
All tests are also tested with mocked geojson data.

TestMultipleMapHandlers
testMultipleHandlersCache: This tests that all map endpoints called in sequence to one another still return the appropriate result.
This also tests that the respective cache for each handler behaves correctly and acts as its own cache entity.
This test is also tested with mocked data.

We chose to create a mocked.geojson to use for mocking. We did this to test if our handler can parse FeatureCollection and Features
appropriately with Moshi. It also helps us get cacheing results. The benefit of mocking using a smaller geojson file is that if we didn't have the full file, we could still test that the handlers appropriately search area description and coordinates. We can also
check the outputs because it is controlled data.

**IMPORTANT: All tests run on their own. If they fail when running it with the whole test suite, that is because the server is overrun. Rerun the test seperately and it will work.**

### 4. Build and Run:

Upon inputting the command 'npm run dev' into the terminal, the user can utilize the Mock Application. The backend is run at https://cs32customserver.com/maps/. To run the server on localhost, run the backend server class and navigate to localhost:4000. 

The user will be prompted to input a command of their choice into the command line. 
The user can input the command 'mode brief' or 'mode verbose' to change the mode of the 
application. The user can input the command 'load' to load a csv file into the 
application. The user can input the command 'view' to view the loaded csv file. The 
user can input the command 'search' to search for a term in the loaded csv file. 
They can clear the command history using the "clear" command. The user can also 
register new commands using the 'register' command.
The user can also input 'broadband' to find the broadband percent from the state
and county. The output will also showed the time and date of the cached result. 
This time is in UTC (universal time), not the local timezone. The cache will last
for 10 minutes to optimize backend server heap space. Lastly, the user can 'searchareas'
by a keyword to search for areas with that keyword in the area description. From this, 
the map will move to show the highlighted areas. If there are no areas with the keyword, 
the map will zoom out to the globe to show the user that there are no highlighted dots 
(no areas match).

A user can view all possible commands by clicking on the "Command History" header. They
can close the command instructions by reclicking the header. 

The user can also search for longitude and latitude directly. There are keyboard
shortcuts added to the website. The user can press "Ctrl+b" to navigate to the input
command box. Then, they can sumbit the command by pressing "Enter" on the keyboard.
The user can press "Ctrl+q" to navigate to the latitude input box. Then, they can press 
enter from any input box to submit the respective input. For example, the user can 
press "Ctrl+q" to navigate to the latitude input box, press "Tab" to access the 
longitude input, then press "Enter" to submit the geocode. The map will move accordingly.

To use the backend directly, use the endpoints /redliningdata to access the entire
geojson file. To search areas, use the endpoint /searchareas?keyword={keyword}.
This will return all the coordinates that match this keyword. To search for all
the features in a boundary box, use the endpoint /boundarybox?minLat={minLat}&maxLat={maxLat}&
minLng={minLng}&maxLng={maxLng}. All endpoint handlers are cached (except for the csv
handlers), so the timestamp from the first instance of the particular search query will
return. This prevents excessive calls to the server and to the API. When using /searchareas,
you can just input "/searchareas?keyword" to access the entire geojson file coordinates.

The handlers for RedlingingData, BoundaryBox, and SearchAreas all take in a filepath.
We created a mocked geojson file and send these into the handlers to test mocked data.
This allowes us to se that FeatureCollection data is appropriately being parsed and evaluated
to return success messages in the handler.

### 4. Reflection Question:

In this sprint, we employed many programming languages, development enviroments, software packages, hardware etc. When considering the stack, we can identify various components for the front-end, back-end, and other parts of the application. A list of some of these components used in our sprint and how, are as follows:

React: React is a JavaScript library used for building user interfaces. In this capstone demo, React is the foundation for creating the front-end user interface and is used in all of our front end files. It handles the rendering of components, the application's state, and the overall structure of the web application. React components such as Mapbox and REPL are created and recentered in the App component.

Mapbox: Mapbox is a mapping platform used for rendering interactive maps. In this demo, it is used for displaying and customizing maps, and geopsatial data is displayed using Mapbox tools.

Moshi: Moshi is a JSON library for Android and Java applications. In this code, Moshi is used in the BoundaryBoxHandler and GEOJsonHandler classes to parse and deserialize JSON data. It's employed to convert raw JSON content into Java objects, specifically FeatureCollection objects, which are used for further processing.

Spark: Spark is a micro web framework for building web applications in Java. In this capstone, it's used for the back-end server, handling incoming HTTPS requests and responses in the BoundaryBoxHandler class and the GEOJsonHandler class. Here, Spark is used to handle incoming HTTPS requests, read data files, and respond with data or error messages. It enables the creation of API endpoints for data retrieval and manipulation.

"mapbox-gl": This is a JavaScript library specifically for Mapbox that provides tools for rendering and interacting with maps on the client side. It allows for the customization and display of geospatial data, ie styles, layers and data sources for the map.

"geojson": The GeoJSON library is used for handling GeoJSON data, which is a common format for representing geospatial data. It helps parse and work with the geographical features and properties.It's utilized in the BoundaryBoxHandler and GEOJsonHandler classes to handle GeoJSON data.

"REPL" (Read-Eval-Print Loop): This custom component/module is used in the code. It's used for providing an interactive interface or tool for manipulating and testing the users input requests.

"index.css" and "App.css": These are CSS files used for styling the web application. They control the visual presentation and layout of the user interface.

"fullDownload.json": This file contains raw geospatial data in JSON format. The rl_data variable is initialized with data from "fullDownload.json." This file contains geospatial data in JSON format and is used to create a FeatureCollection representing geospatial features. It serves as the source of geographical information that can be displayed on the map.

"private/api_key.tsx": This is the location for a private API key, used for accessing certain services or resources from GeoJSON.

HTML elements: HTML elements are used to structure the web page, providing the basic framework for the user interface. They work in conjunction with React components for rendering content on our webpage .

"document.getElementById('root')" (in ReactDOM):This code is used in the ReactDOM.createRoot method to access the HTML element with the ID "root" in the document. It's a fundamental part of the React application setup, linking the React application to the HTML document.

HTML and JSX: HTML is the standard markup language used for structuring web pages. JSX (JavaScript XML) is a syntax extension for JavaScript that is often used in React applications to describe what the UI should look like. In our project, HTML and JSX are used to define the structure and content of the web page, including the components rendered by React.

"ReactDOM": ReactDOM is a package that is part of the React ecosystem. It is specifically used for rendering React components into the DOM (Document Object Model). Here, ReactDOM.createRoot is used to render the React application into the root element of the HTML document, ensuring that the user interface is displayed in the web browser.

"featureCollection" and "Feature" (custom classes): These classes represent the geojson data model for the application. "FeatureCollection" encapsulated a collection of geographical features, while "Feature" represents individual geographical features on the map. These classes are essential for organizing and working with geospatial data within the application.

Thus, here are just a few examples of external components that we integrated into our code to create a fully functional mapping application with back end and front end components.

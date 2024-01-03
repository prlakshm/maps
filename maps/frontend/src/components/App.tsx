import React, { useEffect, useState } from "react";
import "../styles/App.css";
import Mapbox from "./Mapbox";
import REPL from "./REPL";


// This is the main functional component named "App" that represents the root of the application.
function App() {
  const [coordinates, setCoordinates] = useState<number[][]>([]);

  return (
    <div className="App">
      <header className="App-header">Maps</header>
      <div className="app-style">
        <div className="leftColumnStyle">
          <Mapbox
            coordinates={coordinates}
            setCoordinates={setCoordinates}
          ></Mapbox>
        </div>
        <div className="rightColumnStyle">
          <REPL coordinates={coordinates} setCoordinates={setCoordinates} />
        </div>
      </div>
    </div>
  );
}

// Export the "App" component so it can be used in other parts of the application.
export default App;

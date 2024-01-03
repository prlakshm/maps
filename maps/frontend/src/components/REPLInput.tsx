import "../styles/main.css";
import { Dispatch, SetStateAction, useState } from "react";
import { ControlledInput } from "./ControlledInput";
import { HistoryItem } from "../types/HistoryItem";
import { REPLFunction } from "../types/REPLFunction";
import { useEffect } from "react";
/**
 * Map storing registered commands
 */
const commandRegistry = new Map<string, REPLFunction>();

/**
 * Helper function responsible for registering commands
 * @param {string} command - The name of the command to be registered
 * @param {REPLFunction} func - The function to be executed upon command call
 */
function registerCommand(command: string, func: REPLFunction) {
  commandRegistry.set(command, func);
}

/**
 * Props for the REPLInputProps component
 */
interface REPLInputProps {
  history: HistoryItem[];
  setHistory: Dispatch<SetStateAction<HistoryItem[]>>;
  mode: string;
  setMode: (newMode: string) => void;
  commandResultMap: Map<HistoryItem, [[]] | string>;
  updateCommandResult: (command: string, output: [[]] | string) => void;
  ariaLabel: string;
  coordinates: number[][];
  setCoordinates: Dispatch<SetStateAction<number[][]>>;
}

/**
 * React component responsible for handling user input and executing commands
 * @param {REPLInputProps} props - The properties required for rendering the component
 */
export function REPLInput(props: REPLInputProps) {
  const [commandString, setCommandString] = useState<string>("");
  const [count, setCount] = useState<number>(0);
  const {
    mode,
    setMode,
    history,
    setHistory,
    commandResultMap,
    updateCommandResult,
    ariaLabel,
    coordinates,
    setCoordinates,
  } = props;

  /**
   * REPLFunction handling the registration of a user-specified command
   * @param {string[]} args - Details of a to-be-registered function (commandName, function executed upon command)
   */
  const handleRegister: REPLFunction = async (
    args: string[]
  ): Promise<string> => {
    if (args.length !== 2) {
      return "Invalid usage of 'register' command. Usage: register <commandName> <functionToExecute>";
    }
    const commandName = args[0];
    const toExecute = args[1];
    if (commandRegistry.has(commandName)) {
      return "Command: " + commandName + " is already registered";
    } else {
      registerCommand(commandName, eval(toExecute));
      return "Command registered: " + commandName;
    }
  };

  //----------------------------------------------------------------------------------------------------
  /**
   * Function handling mode changes of the REPL interface
   * @param {string[]} args - The new mode to set
   */
  const handleMode: REPLFunction = async (args: string[]): Promise<string> => {
    const validModes = ["brief", "verbose"];
    if (args.length !== 1) {
      return "Invalid usage of 'mode' command. Usage: mode <newMode>";
    }
    if (validModes.includes(args[0])) {
      setMode(args[0]);
      return "Mode changed to " + args[0];
    } else {
      return "Invalid mode: " + args[0] + ". Use brief or verbose";
    }
  };

  /**
   * Function handling loading the file
   * @param {string[]} args - The file path of a file to be loaded (must be within the data directory)
   */
  const handleLoad: REPLFunction = async (args: string[]): Promise<string> => {
    if (args.length !== 1) {
      return "Invalid usage of 'load' command. Usage: load <URL>";
    }
    const filepath = args[0].trim();
    try {
      const response = await fetch(
        `http://localhost:4000/loadcsv?filepath=${filepath}`
      );
      if (response.ok) {
        const data = await response.json();
        const resultMessage =
          data.result === "success"
            ? "File " + filepath + " loaded successfully"
            : data.error_message;
        return resultMessage;
      } else return "Failed to fetch data from the backend";
    } catch (error) {
      // Implement the logic to load the file based on the provided 'url'.
      // Return an appropriate result message.
      return "An error ocurred while loading the file: " + error;
    }
  };

  /**
   * Function handling viewing the loaded dataset
   * @param {string[]} args - None (empty)
   */
  const handleView: REPLFunction = async (args: string[]): Promise<string> => {
    if (args.length !== 0) {
      return "Invalid usage of 'view' command. Usage: view";
    }
    try {
      const response = await fetch("http://localhost:4000/viewcsv");
      if (response.ok) {
        const data = await response.json();
        const resultMessage =
          data.result === "success" ? data.data : data.error_message;
        return resultMessage;
      } else return "Failed to fetch data from the backend";
    } catch (error) {
      return "An error occurred while viewing the file: " + error;
    }
  };

  /**
   * Function handling searching within the loaded dataset
   * @param {string[]} args - The search query parameters
   */
  const handleSearch: REPLFunction = async (
    args: string[]
  ): Promise<string> => {
    if (args.length !== 3) {
      return "Invalid search command. Usage: search <hasHeaders> <value> <columnId>";
    }
    const hasHeaders = args[0];
    const value = args[1].includes("%")
      ? args[1].replace("%", "%25").replace(/_/g, " ")
      : args[1].replace(/_/g, " ");
    const columnId = args[2].replace(/_/g, " ");

    try {
      const response = await fetch(
        `http://localhost:4000/searchcsv?headers=${hasHeaders}&value=${value}&colid=${columnId}`
      );
      if (response.ok) {
        const data = await response.json();
        const resultMessage =
          data.result === "success" ? data.data : data.error_message;
        return resultMessage;
      } else return "Failed to fetch data from the backend";
    } catch (error) {
      return "An error occurred while searching through the file: " + error;
    }
  };

  /**
   * Function handling retrieving broadband access percentage
   * @param {string[]} args - The broadband query parameters
   */
  const handleBroadband: REPLFunction = async (
    args: string[]
  ): Promise<string> => {
    if (args.length !== 2) {
      return "Invalid broadband retrieval command. Usage: broadband <state> <county>";
    }
    const state = args[0].replace(/_/g, " ");
    const county = args[1].replace(/_/g, " ");

    try {
      const response = await fetch(
        `http://localhost:4000/broadband?state=${state}&county=${county}`
      );
      if (response.ok) {
        const data = await response.json();
        const resultMessage =
          data.result === "success"
            ? `time of retrieval: ${data.date_time} broadband access percent: ${data.broadband_access_percent}`
            : data.error_message;
        return resultMessage;
      } else return "Failed to fetch data from the backend";
    } catch (error) {
      return "An error occurred while fetching broadband data: " + error;
    }
  };

  /**
   * Function handling search areas for highlighting map
   * @param {string[]} args - keyword to search areas
   */
  const handleAreas: REPLFunction = async (args: string[]): Promise<string> => {
    if (args.length !== 1) {
      return "Invalid usage of 'searchareas' command. Usage: searchareas <keyword>";
    }
    const keyword = args[0].trim();
    try {
      const response = await fetch(
        `http://localhost:4000/searchareas?keyword=${keyword}`
      );
      if (response.ok) {
        const data = await response.json();
        setCoordinates(data.coordinatesList);

        const resultMessage =
          data.result === "success"
            ? "Areas descriptions with " + keyword + " highlighted successfully"
            : data.error_message;
        return resultMessage;
      } else return "Failed to fetch areas from the backend";
    } catch (error) {
      // Return an appropriate result message.
      return "An error ocurred while searching for areas: " + error;
    }
  };
  // ---------------------------------------------------------------------------------------------------------------------------------------------
  /**
   * Functions registered upon component mount
   */
  useEffect(() => {
    registerCommand("register", handleRegister);
    registerCommand("mode", handleMode);
    registerCommand("load", handleLoad);
    registerCommand("view", handleView);
    registerCommand("search", handleSearch);
    registerCommand("broadband", handleBroadband);
    registerCommand("searchareas", handleAreas);
  }, []);

  /**
   * Helper function for executing the command upon input submission
   * @param {string} commandName - Name of the command
   * @param {string[]} args - Arguments for the to-be-executed function assigned to the command
   */
  async function executeCommand(
    commandName: string,
    args: string[]
  ): Promise<string> {
    const func = commandRegistry.get(commandName);

    if (func) {
      try {
        // Execute the registered function and pass the arguments, excluding the command itself
        const result = await func(args);
        return result;
      } catch (error) {
        return `Error executing command. ${error}`;
      }
    } else {
      return `Command not found: ${commandName}. Input "register <commandName> <function>" to register new command`;
    }
  }

  /**
   * Function triggered when the "Submit" button is clicked to process the user's command
   * @param {string} commandString - The whole user input
   */
  function handleSubmit(commandString: string) {
    const trimmedCommand = commandString.trim();
    if (trimmedCommand === "") {
      alert("Command cannot be empty");
      return;
    }

    // array of all words entered by user in the command input
    const args = trimmedCommand.split(/\s+/);
    // args[0] is the command name, args.slice(1) is an array of everything EXCEPT for the command name
    executeCommand(args[0], args.slice(1)).then((result) => {
      updateCommandResult(commandString, result);
      setCount(count + 1);
    });

    setCommandString("");
  }

  //All keyboard shortcuts here
  //------------------------------------------------------------------------------
  /**
   * Handles keyboard shortcut to submit by pressing Enter in command box
   * @param e keyboard event of pressing Enter key
   */
  function handleEnterPress(e: React.KeyboardEvent) {
    if (e.key === "Enter") {
      handleSubmit(commandString);
    }
  }

  /**
   * Webpage always listens out for Ctrl+b to navigate cursor to command box
   */
  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if (e.key === "b" && e.ctrlKey) {
        const inputElement = document.querySelector(".repl-command-box");
        if (inputElement && inputElement instanceof HTMLInputElement)
          inputElement.focus();
      }
    };

    document.addEventListener("keydown", handleKeyPress);

    return () => {
      document.removeEventListener("keydown", handleKeyPress);
    };
  }, []);

  //---------------------------------------------------------------------------------

  return (
    <div className="repl-input" aria-live="polite" aria-label={ariaLabel}>
      <fieldset>
        <legend style={{ marginBottom: '10px' }}>Enter a command:</legend>
        <ControlledInput
          value={commandString}
          setValue={setCommandString}
          ariaLabel={"Command Input Box to type in commands"}
          onKeyDown={handleEnterPress}
        />
      </fieldset>
      <button
        aria-label="repl-input-submit-button"
        onClick={() => handleSubmit(commandString)}
        style={{ marginTop: '8px' }}
      >
        Submit
      </button>
    </div>
  );
}

import { Dispatch, SetStateAction, useState } from "react";
import "../styles/main.css";
import { REPLHistory } from "./REPLHistory";
import { REPLInput } from "./REPLInput";
import { HistoryItem } from "../types/HistoryItem";

interface REPLProps {
  coordinates: number[][];
  setCoordinates: Dispatch<SetStateAction<number[][]>>;
}
/**
 * React component allowing users to input commands;
 * Displays corresponding command history, and shows the results of each command.
 * Depending on the mode selected, can display either just the output,
 * or both the user's command and the output.
 */
export default function REPL({ coordinates, setCoordinates }: REPLProps) {
  // State to manage the command history
  const [history, setHistory] = useState<HistoryItem[]>([]);

  // State to manage the display mode (brief or verbose)
  const [mode, setMode] = useState<string>("brief");

  // State to store command results using a Map
  const [commandResultMap, setCommandResultMap] = useState(new Map());

  /**
   * Update the command result and add it to the command history.
   * @param {string} command - The user's input command.
   * @param {[[]] | string } result - The result of the command execution.
   */
  function updateCommandResult(command: string, result: [[]] | string) {
    // Create a history item with the user's command and timestamp
    const historyItem: HistoryItem = {
      command: command,
      timestamp: new Date().getTime(),
    };

    // Add the result to the commandResultMap using the history item as the key
    commandResultMap.set(historyItem, result);

    // Update the command history with the new history item
    setHistory((prevHistory) => [...prevHistory, historyItem]);
  }

  return (
    <div className="repl">
      {/* Display the command history component */}
      <REPLHistory
        commandHistory={history}
        mode={mode}
        commandResultMap={commandResultMap}
        ariaLabel="History Log Display to show past commands inputted"
      />
      <hr></hr>
      {/* Display the input component for user commands */}
      <REPLInput
        history={history}
        setHistory={setHistory}
        mode={mode}
        setMode={setMode}
        commandResultMap={commandResultMap}
        updateCommandResult={updateCommandResult}
        ariaLabel="Input Command Component to take in and process command inputs"
        coordinates={coordinates}
        setCoordinates={setCoordinates}
      />
    </div>
  );
}

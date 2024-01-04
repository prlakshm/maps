import { useState } from "react";
import "../styles/main.css";
import { HistoryItem } from "../types/HistoryItem";

/**
 * Props for the REPLHistory component.
 */
interface REPLHistoryProps {
  commandHistory: HistoryItem[]; // Array of command history items
  mode: string; // Display mode, either "brief" or "verbose"
  commandResultMap: Map<HistoryItem, [[]] | string>; // Map of command results
  ariaLabel: string; // ARIA label for accessibility
}

/**
 * Component responsible for displaying the command history and corresponding results.
 * @param {REPLHistoryProps} props - The properties required for rendering the component.
 */
export function REPLHistory(props: REPLHistoryProps) {
  const { commandHistory, mode, commandResultMap, ariaLabel } = props;
  const [showInstructions, setShowInstructions] = useState(false);

  const toggleInstructions = () => {
    setShowInstructions(!showInstructions);
  };

  const closeInstructions = () => {
    setShowInstructions(false);
  };
  /**
   * Function for rendering different types of data in the command history.
   * @param {[[]] | string} data - The data to be rendered.
   * @returns {JSX.Element} - The rendered data as JSX.
   */
  const renderData = (data: [[]] | string) => {
    if (data.length === 0) {
      return "No data to display";
    }
    if (Array.isArray(data) && Array.isArray(data[0])) {
      // Render a table if the data is a 2D array
      return (
        <table className="center-table">
          <tbody>
            {data.map((row: string[], rowIndex: number) => (
              <tr key={rowIndex}>
                {row.map((cell: string, cellIndex: number) => (
                  <td key={cellIndex}>{cell}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      );
    } else if (typeof data === "string") {
      return data;
      // Uncomment the following lines to display JSON data
      // } else {
      //   return JSON.stringify(data);
    }
  };

  return (
    <div className="repl-history" aria-live="polite" aria-label={ariaLabel}>
      <h2 aria-live="polite" onClick={toggleInstructions}>
        Command History
      </h2>

      {showInstructions && (
        <div className="instructions-dropdown">
          <p>Command Instructions:</p>
          <ul>
            <li>
              "<span style={{ color: "mediumorchid" }}>load [filepath]</span>"{" "}
              to load csv file
            </li>
            <li>
              {" "}
              "<span style={{ color: "mediumorchid" }}>view</span>" to view csv
              file
            </li>
            <li>
              "
              <span style={{ color: "mediumorchid" }}>
                search [has_headers] [search_val] [col_name/col_index]
              </span>
              " to search a specific column where has_headers is "true" or
              "false"
            </li>
            <li>
              "
              <span style={{ color: "mediumorchid" }}>
                search [has_headers] [search_val] *
              </span>
              " to search all columns where has_headers is "true" or "false"
            </li>
            <li>
              "
              <span style={{ color: "mediumorchid" }}>
                broadband [state] [county]
              </span>
              " to get broadband access percent county in state
            </li>
            <li>
              "
              <span style={{ color: "mediumorchid" }}>
                searchareas [keyword]
              </span>
              " to search by area descriptions
            </li>
            <li>
              {" "}
              "
              <span style={{ color: "mediumorchid" }}>
                register [command] [function_to_execute]
              </span>
              " to register a new command
            </li>
            <li>
              "<span style={{ color: "mediumorchid" }}>mode brief</span>" to
              display only history output
            </li>
            <li>
              "<span style={{ color: "mediumorchid" }}>mode verbose</span>" to
              display history command and output
            </li>
            <li>
              "<span style={{ color: "mediumorchid" }}>clear</span>" to
              clear history
            </li>
          </ul>
        </div>
      )}

      <ul>
        {commandHistory.map((command, index) => (
          <div key={index} className="history-element">
            <li>
              {mode === "brief" ? (
                // Display in brief mode with only the output
                <div className="text-box" aria-live="polite">
                  <p>
                    Output:{" "}
                    {renderData(commandResultMap.get(command) ?? "No data")}
                  </p>
                </div>
              ) : (
                // Display in verbose mode with both command and output
                <div className="text-box" aria-live="polite">
                  <p>Command: {command.command}</p>
                  <p>
                    Output:{" "}
                    {renderData(commandResultMap.get(command) ?? "No data")}
                  </p>
                </div>
              )}
            </li>
          </div>
        ))}
      </ul>
    </div>
  );
}

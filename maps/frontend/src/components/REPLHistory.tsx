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
      <h2 aria-live="polite">Command History</h2>
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

import "../styles/main.css";
import { Dispatch, SetStateAction, useEffect } from "react";


/**
 * Props for the ControlledInput component.
 */
interface ControlledInputProps {
  value: string; // The current value of the input.
  setValue: Dispatch<SetStateAction<string>>; // A function to update the input's value.
  ariaLabel: string; // A text label for accessibility purposes.
  onKeyDown?: (e: React.KeyboardEvent) => void; //new optional prop to handle pressing submit from controlled input
}



/**
 * A controlled input component corresponding to user's input in the command box.
 * Allows for its value to be managed externally.
 * @param {ControlledInputProps} props - The props for the ControlledInput component.
 */
export function ControlledInput({
  value, // The current value of the input.
  setValue, // A function to update the input's value.
  onKeyDown, // An optional function to handle keydown events.
  ariaLabel, // A text label for accessibility purposes.
}: ControlledInputProps) {
  return (
    <input
      type="text"
      className="repl-command-box"
      value={value} // Bind the input's value to the provided 'value' prop.
      placeholder="Click &quot;Command History&quot; to see possible commands!"
      onChange={(ev) => setValue(ev.target.value)} // Update the input's value when it changes.
      onKeyDown={onKeyDown} // Handle keydown events if the 'onKeyDown' prop is provided.
      aria-label={ariaLabel} // Set the accessibility label for screen readers.
    ></input>
  );
}

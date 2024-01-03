/**
 * Interface representing an element in history.
 * @param {string} command - User's input
 * @param {number} timestamp - Current time used for differentiation between the
 * elements (necessary for mode selection)
 */
export interface HistoryItem {
  command: string;
  timestamp: number;
}

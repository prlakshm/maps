/**
 * A command-processor function for our REPL. The function returns a Promise
 * which resolves to a string, which is the value to print to history when
 * the command is done executing.
 *
 * The arguments passed in the input (which need not be named "args") should
 * *NOT*contain the command-name prefix.
 */
export interface REPLFunction {
  (args: Array<string>): Promise<string>;
}

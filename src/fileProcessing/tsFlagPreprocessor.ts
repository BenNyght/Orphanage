import * as vscode from 'vscode';

/**
 * Removes lines between #if FLAG and #endif if the FLAG is not in compileFlags.
 * Supports *nested* blocks. For example:
 *
 * #if FOO
 *   line1
 *   #if BAR
 *     line2
 *   #endif
 *   line3
 * #endif
 *
 * A block is only active if *all* containing blocks are active.
 * If the parent block is inactive, child blocks are implicitly inactive.
 */
export function removeBlocksWithoutFlags(
  fileContent: string,
  compileFlags: string[]
): string {
  const lines = fileContent.split(/\r?\n/);

  // A stack of booleans representing active/inactive for each nested block
  // The top (last) element indicates whether we are currently active
  // Start with [true] meaning "global" block is active by default
  const blockStack: boolean[] = [true];

  const outputLines: string[] = [];

  for (const line of lines) {
    const trimmed = line.trim();

    // Check if line is "#if <FLAG>"
    const ifMatch = trimmed.match(/^#if\s+(\w+)/);
    if (ifMatch) {
      const flagName = ifMatch[1];

      // The new block is active only if:
      //   1) The parent block is active
      //   2) compileFlags includes this flag
      const parentActive = blockStack[blockStack.length - 1];
      const currentBlockActive = parentActive && compileFlags.includes(flagName);

      // Push onto stack
      blockStack.push(currentBlockActive);

      // Only include preprocessor as comment
      outputLines.push("//" + line + " = " + currentBlockActive);
      continue;
    }

    // Check if line is "#endif"
    if (trimmed.match(/^#endif/)) {
      // Pop the current block of code
      // If stack has only 1 element, that's an error scenario we don't handle here
      // This could happen if someone has defined too many #endif 's
      if (blockStack.length > 1) {
        blockStack.pop();
      } else {
        vscode.window.showErrorMessage('Invalid preprocessor flag');
      }

      // Only include preprocessor as comment
      outputLines.push("//" + line);
      continue;
    }

    // If the top of the stack is true => we keep this line
    const currentActive = blockStack[blockStack.length - 1];
    if (currentActive) {
      outputLines.push(line);
    }
  }

  return outputLines.join('\r\n');
}

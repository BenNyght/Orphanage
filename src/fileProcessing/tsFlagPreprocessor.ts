import * as vscode from 'vscode';

/**
 * Removes lines between // #if FLAG and // #endif if the FLAG is not in compileFlags.
 * Supports nested blocks.
 */
/**
 * Removes lines between #if FLAG and #endif if the FLAG is not in compileFlags.
 * Supports *nested* blocks. For example:
 *
 * // #if FOO
 *   line1
 * // #if BAR
 *     line2
 * // #endif
 *   line3
 * // #endif
 *
 * A block is only active if *all* containing blocks are active.
 * If the parent block is inactive, child blocks are implicitly inactive.
 */
export function removeBlocksWithoutFlags(
  fileContent: string,
  compileFlags: string[]
): string {
  const lines = fileContent.split(/\r?\n/);

  const blockStack: boolean[] = [true];
  const outputLines: string[] = [];

  for (const line of lines) {
    const trimmed = line.trim();

    // Check for // #if <FLAG>
    const ifMatch = trimmed.match(/^\/\/\s*#if\s+(\w+)/);
    if (ifMatch) {
      const flagName = ifMatch[1];
      const parentActive = blockStack[blockStack.length - 1];
      const currentBlockActive = parentActive && compileFlags.includes(flagName);

      blockStack.push(currentBlockActive);
      outputLines.push(`// ${trimmed} = ${currentBlockActive}`);
      continue;
    }

    // Check for // #endif
    if (/^\/\/\s*#endif/.test(trimmed)) {
      if (blockStack.length > 1) {
        blockStack.pop();
      } else {
        vscode.window.showErrorMessage('Unmatched // #endif detected');
      }

      outputLines.push(`// ${trimmed}`);
      continue;
    }

    // Only include line if all surrounding blocks are active
    if (blockStack[blockStack.length - 1]) {
      outputLines.push(line);
    }
  }

  return outputLines.join('\r\n');
}

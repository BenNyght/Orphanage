import { ExtensionContext } from 'vscode';
import { registerFlattenCommand } from './flattenAllCommand';
import { setupPartialAutoRun } from './autoRunPartial';

export function activate(context: ExtensionContext) {
  // Register the manual "Flatten All" command
  registerFlattenCommand(context);

  // Setup auto-run watchers
  setupPartialAutoRun(context);
}

export function deactivate() {
  // Cleanup
}
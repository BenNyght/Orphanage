import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import { readOrCreateConfig } from './config';
import { OrphanageConfig } from './config';
import { processAndCloneFile, removeSingleFile } from './fileProcessing/fileProcessor';

/**
 * Setup to process files on file changes automatically. Cloning the files into the flattened directory
 */
export function setupPartialAutoRun(context: vscode.ExtensionContext) {
  const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
  if (!workspaceFolder) {
    vscode.window.showErrorMessage("No workspace folder is open.");
    return;
  }

  const config: OrphanageConfig | null = readOrCreateConfig(workspaceFolder);
  if (!config) {
    return;
  }

  // Build copy paths
  const sourceAbsolute = path.join(workspaceFolder.uri.fsPath, config.sourceFolder);
  const destAbsolute = path.join(workspaceFolder.uri.fsPath, config.destFolder);
  if (!fs.existsSync(destAbsolute)) {
    fs.mkdirSync(destAbsolute, { recursive: true });
  }

  // Create file watcher for all files in source folder
  const pattern = new vscode.RelativePattern(sourceAbsolute, '**/*');
  const watcher = vscode.workspace.createFileSystemWatcher(pattern);

  // Avoid flattening on every input
  let changeTimeout: NodeJS.Timeout | undefined;
  const schedule = (operation: () => void) => {
    if (changeTimeout) {
      clearTimeout(changeTimeout);
    }
    changeTimeout = setTimeout(operation, 750); // 0.75s debounce
  };

  // CREATE or CHANGE => Flatten only that one file
  watcher.onDidCreate(uri => {
    schedule(() => processAndCloneFile(uri.fsPath, destAbsolute, config));
  });
  watcher.onDidChange(uri => {
    schedule(() => processAndCloneFile(uri.fsPath, destAbsolute, config));
  });

  // DELETE => Remove that one file
  watcher.onDidDelete(uri => {
    schedule(() => removeSingleFile(uri.fsPath, destAbsolute));
  });

  // Cleanup on deactivate
  context.subscriptions.push(watcher);
}

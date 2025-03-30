import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';
import { getConfig } from './config/config';
import { processAndCloneFile, removeSingleFile } from './fileProcessing/fileProcessor';
import { getSelectedDestination, isAutoProcessEnabled, setAutoProcessEnabled } from './config/configState';
import { getRootWorkspaceFolder } from './util';

let currentWatcher: vscode.FileSystemWatcher | undefined;
let extensionContext: vscode.ExtensionContext;

/**
 * Update whether auto processing is running
 */
export function setAutoProcessRunning(enable: boolean) {
  setAutoProcessEnabled(enable);
  if (enable) {
    const selectedDest = getSelectedDestination();
    if (selectedDest) {
      setupPartialAutoRun(extensionContext);
    }
  } else {
    disposeWatcher();
  }
}

/**
 * Setup to process files on file changes automatically. Cloning the files into the flattened directory
 */
export function setupPartialAutoRun(context: vscode.ExtensionContext) {
  extensionContext = context;

  const selectedDestinationEntry = getSelectedDestination();
  if (!isAutoProcessEnabled() || !selectedDestinationEntry) {
    disposeWatcher();
    return;
  }

  const workspaceFolder = getRootWorkspaceFolder();
  const config = getConfig();
  if (!workspaceFolder || !config) {
    return;
  }

  // Build copy paths
  const sourceAbsolute = path.join(workspaceFolder.uri.fsPath, config.sourceFolder);
  const destAbsolute = path.join(workspaceFolder.uri.fsPath, selectedDestinationEntry.folderPath);
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

  currentWatcher = watcher;
}

/**
 * Dispose any existing watcher.
 */
export function disposeWatcher() {
  if (currentWatcher) {
    currentWatcher.dispose();
    currentWatcher = undefined;
  }
}
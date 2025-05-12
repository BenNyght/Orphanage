import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { isDebugEnabled } from './configState';

export interface OrphanageConfig {
  sourceFolder: string;
  destinations: DestinationEntry[];
  copyFromDestination: DestinationToSource[];
  compileFlags?: string[];
}

export interface DestinationEntry {
  displayName: string;
  folderPath: string;
  compileFlags?: string[];
}

export interface DestinationToSource {
  destinationPath: string;
  sourcePath: string;
}

// Default config if orphanage.json doesn't exist
const DEFAULT_CONFIG: OrphanageConfig = {
  sourceFolder: "src",
  destinations: [
    { displayName: "Destination 1", folderPath: "dist1", compileFlags: ["COMPILE_FLAG_1"] },
    { displayName: "Destination 2", folderPath: "dist2", compileFlags: ["COMPILE_FLAG_2"] }
  ],
  copyFromDestination: [
    { destinationPath: "types", sourcePath: "types" },
  ],
  compileFlags: [
    "COMPILE_FLAG_3"
  ]
};

export function getConfig(): OrphanageConfig | null {
  const configFilePath = getConfigPath();
  if (!configFilePath) {
    return null;
  }

  if (!fs.existsSync(configFilePath)) {
    vscode.window.showInformationMessage(
      "No orphanage.json found. A default config has been created. Please review and re-run the command."
    );
    return null;
  }

  const content = fs.readFileSync(configFilePath, 'utf-8');
  const config: OrphanageConfig = JSON.parse(content);

  if (!Array.isArray(config.destinations)) {
    config.destinations = [];
  }
  config.compileFlags = config.compileFlags || [];

  if (isDebugEnabled()) {
    config.compileFlags.push("DEBUG");
  }
  
  return config;
}

export function getConfigPath(): string | undefined {
  const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
  if (!workspaceFolder) {
    return undefined;
  }

  const configFilePath = path.join(workspaceFolder.uri.fsPath, 'orphanage.json');
  return configFilePath;
}

export function createConfigIfMissing() {
  const configFilePath = getConfigPath();
  if (!configFilePath) {
    return;
  }

  if (!fs.existsSync(configFilePath)) {
    fs.writeFileSync(configFilePath, JSON.stringify(DEFAULT_CONFIG, null, 2), 'utf-8');
    vscode.window.showInformationMessage(
      "Created config file. Relaunch VSCode to apply."
    );
  }
}
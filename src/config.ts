import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';

export interface OrphanageConfig {
  sourceFolder: string;
  destFolder: string;
  ignoreFlattenImports?: string[];
}

// Default config if orphanage.json doesn't exist
const DEFAULT_CONFIG: OrphanageConfig = {
  sourceFolder: 'src',
  destFolder: 'dist',
  ignoreFlattenImports: ["import_to_keep"]
};

/**
 * Read orphanage.json. If it's missing, create a default config and returns null.
 */
export function readOrCreateConfig(workspaceFolder: vscode.WorkspaceFolder): OrphanageConfig | null {
  const configFilePath = path.join(workspaceFolder.uri.fsPath, 'orphanage.json');

  if (!fs.existsSync(configFilePath)) {
    fs.writeFileSync(configFilePath, JSON.stringify(DEFAULT_CONFIG, null, 2), 'utf-8');
    vscode.window.showInformationMessage(
      "No orphanage.json found. A default config has been created. Please review and re-run the command."
    );
    return null;
  }

  const configContent = fs.readFileSync(configFilePath, 'utf-8');
  return JSON.parse(configContent) as OrphanageConfig;
}

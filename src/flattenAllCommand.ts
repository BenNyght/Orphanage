import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { OrphanageConfig } from './config';
import { readOrCreateConfig } from './config';
import { processAndCloneFile } from './fileProcessing/fileProcessor';
import { getAllFiles } from './fileProcessing/fileProcessor';

/**
 * Registers the 'Orphanage.flatten' command, which does a full re-flatten
 * of the source folder into the destination folder.
 */
export function registerFlattenCommand(context: vscode.ExtensionContext) {
  const disposable = vscode.commands.registerCommand('Orphanage.flatten', async () => {
	try {
	  const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
	  if (!workspaceFolder) {
		vscode.window.showErrorMessage("No workspace folder is open.");
		return;
	  }

	  const config: OrphanageConfig | null = readOrCreateConfig(workspaceFolder);
	  if (!config) {
		// We just created orphanage.json or error -> stop
		return;
	  }

	  const sourceAbsolute = path.join(workspaceFolder.uri.fsPath, config.sourceFolder);
	  const destAbsolute = path.join(workspaceFolder.uri.fsPath, config.destFolder);
	  if (!fs.existsSync(sourceAbsolute)) {
		vscode.window.showErrorMessage(`Source folder does not exist: ${sourceAbsolute}`);
		return;
	  }

	  // Clear destination folder
	  if (fs.existsSync(destAbsolute)) {
		fs.rmSync(destAbsolute, { recursive: true, force: true });
	  }
	  fs.mkdirSync(destAbsolute, { recursive: true });

	  // Process all files
	  const allSourceFiles = getAllFiles(sourceAbsolute);
	  for (const filePath of allSourceFiles) {
		const fileName = path.basename(filePath);
		const destFilePath = path.join(destAbsolute, fileName);

		processAndCloneFile(filePath, destFilePath, config);
	  }

	  vscode.window.showInformationMessage(`Flattened ${allSourceFiles.length} file(s) into "${config.destFolder}"!`);
	} catch (err: any) {
	  vscode.window.showErrorMessage(`Error flattening folders: ${err.message || err}`);
	}
  });

  context.subscriptions.push(disposable);
}

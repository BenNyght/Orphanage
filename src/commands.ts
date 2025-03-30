import * as vscode from 'vscode';
import { processAllFiles } from './fileProcessing/fileProcessor';
import { createConfigIfMissing } from './config/config';

/**
 * Registers the 'Orphanage.flatten' command, which does a full re-flatten
 * of the source folder into the destination folder.
 */
export function registerProcessCommand(context: vscode.ExtensionContext) {
  const disposable = vscode.commands.registerCommand('Orphanage.flatten', async () => {
	processAllFiles();
  });

  context.subscriptions.push(disposable);
}

/**
 * Registers the 'Orphanage.createConfig' command, which creates the define config file.
 */
export function registerCreateConfigCommand(context: vscode.ExtensionContext) {
	const disposable = vscode.commands.registerCommand('Orphanage.createConfig', async () => {
		createConfigIfMissing();
	});
  
	context.subscriptions.push(disposable);
  }
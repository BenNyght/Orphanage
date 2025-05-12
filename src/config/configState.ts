import * as vscode from 'vscode';
import { DestinationEntry, getConfig } from './config';
import { getRootWorkspaceFolder } from '../util';
import * as fs from 'fs';

let extensionContext: vscode.ExtensionContext | null = null;

// Settings keys
const SELECTED_DEST_KEY = 'orphanage.selectedDestination';
const AUTO_PROCESS_KEY = 'orphanage.autoProcess';
const DEBUG_MODE_KEY = 'orphanage.debugMode';
const DESTINATION_ROOT_KEY = "orphanage.destinationRoot";

/** Call this once in your activate() function, passing the context. */
export function initializeConfigState(context: vscode.ExtensionContext) {
	extensionContext = context;
}

/** Stores the given destination in globalState so it persists across sessions. */
export function setSelectedDestination(dest: DestinationEntry | null): void {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot store destination.');
		return;
	}

	extensionContext.globalState.update(SELECTED_DEST_KEY, dest);
}

/** Retrieves the currently selected destination from globalState. */
export function getSelectedDestination(): DestinationEntry | null {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot retrieve destination.');
		return null;
	}

	const entry: DestinationEntry | null = extensionContext.globalState.get<DestinationEntry>(SELECTED_DEST_KEY) ?? null;
	if (!entry) {
		vscode.window.showErrorMessage("No destination path selected.");
	}

	return extensionContext.globalState.get<DestinationEntry>(SELECTED_DEST_KEY) ?? null;
}

/** Set auto-process on/off (true/false) in globalState */
export function setAutoProcessEnabled(enabled: boolean): void {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot toggle auto-process state.');
		return;
	}

	extensionContext?.globalState.update(AUTO_PROCESS_KEY, enabled);
}

/** Retrieves whether or not file auto-processing is on */
export function isAutoProcessEnabled(): boolean {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot get auto-process state.');
		return false;
	}

	return extensionContext?.globalState.get<boolean>(AUTO_PROCESS_KEY) ?? false;
}

/** Set auto-process on/off (true/false) in globalState */
export function setDebugEnabled(enabled: boolean): void {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot toggle auto-process state.');
		return;
	}

	extensionContext?.globalState.update(DEBUG_MODE_KEY, enabled);
}

/** Retrieves whether or not file auto-processing is on */
export function isDebugEnabled(): boolean {
	if (!extensionContext) {
		return false;
	}

	return extensionContext?.globalState.get<boolean>(DEBUG_MODE_KEY) ?? false;
}

/** Set root destination folder. The folder the selected destination is relative too */
export function setRootDestinationFolder(rootDestinationFolder: string): void {
	if (!extensionContext) {
		console.warn('Extension context not initialized, cannot toggle auto-process state.');
		return;
	}

	extensionContext?.globalState.update(DESTINATION_ROOT_KEY, rootDestinationFolder);
}

/** Get the root destination folder */
export function getRootDestinationFolder(): string {
	if (!extensionContext) {
		return "";
	}

	const workspaceFolder = getRootWorkspaceFolder()?.uri.fsPath;
	const destinationRootKeyValue = extensionContext?.globalState.get<string>(DESTINATION_ROOT_KEY);
	return destinationRootKeyValue ?? workspaceFolder ?? "";
}

//** Get all compile flags, project and target */
export function getAllCompileFlags(): string[] {
  const selectedCompileFlags = getSelectedDestination()?.compileFlags ?? [];
  const projectCompileFlags = getConfig()?.compileFlags ?? [];
  return selectedCompileFlags.concat(projectCompileFlags);
}
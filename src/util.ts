import * as vscode from 'vscode';

export function getRootWorkspaceFolder(): vscode.WorkspaceFolder | undefined {
	const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
	if (!workspaceFolder) {
		vscode.window.showErrorMessage("No workspace folder is open.");
	}

	return workspaceFolder;
}
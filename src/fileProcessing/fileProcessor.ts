import * as path from 'path';
import * as fs from 'fs';
import * as vscode from 'vscode';
import { OrphanageConfig, getConfig } from '../config/config';
import { rewriteImportsInTSFile } from './tsImportFlattener';
import { getRootDestinationFolder, getSelectedDestination } from '../config/configState';
import { removeBlocksWithoutFlags } from './tsFlagPreprocessor';
import { getRootWorkspaceFolder } from '../util';

// Prefix on the front to know what can be deleted
// PF = Processed File
const filePrefix: string = "PF_";

/**
 * Process all files in selected destination
 */
export function processAllFiles() {
  try {
    const rootDestinationFolder = getRootDestinationFolder();
    const workspaceFolder = getRootWorkspaceFolder();
    const config = getConfig();
    if (!workspaceFolder || !config || !rootDestinationFolder) {
      return;
    }

    const selectedDestinationEntry = getSelectedDestination();
    if (!selectedDestinationEntry) {
      vscode.window.showErrorMessage("No destination path selected.");
      return;
    }

    const sourceAbsolute = path.join(workspaceFolder.uri.fsPath, config.sourceFolder);
    const destAbsolute = path.join(rootDestinationFolder, selectedDestinationEntry.folderPath);
    if (!fs.existsSync(sourceAbsolute)) {
      vscode.window.showErrorMessage(`Source folder does not exist: ${sourceAbsolute}`);
      return;
    }

    // Copy destinaton to source files listed in config
    for (let index = 0; index < config.copyFromDestination.length; index++) {
      const paths = config.copyFromDestination[index];
      const configDestAbsolute = path.join(destAbsolute, paths.destinationPath);
      const configSourceAbsolute = path.join(workspaceFolder.uri.fsPath, paths.sourcePath);
      clearDestination(configSourceAbsolute);
      copyFolderRecursive(configDestAbsolute, configSourceAbsolute);
    }

    // Clear destination folder
    clearDestination(destAbsolute);

    // Process all files
    const allSourceFiles = getAllFiles(sourceAbsolute);
    for (const filePath of allSourceFiles) {
      processAndCloneFile(filePath, destAbsolute, config);
    }
    vscode.window.showInformationMessage(`Flattened ${allSourceFiles.length} file(s) into "${selectedDestinationEntry.folderPath}"!`);
  } catch (err: any) {
    vscode.window.showErrorMessage(`Error flattening folders: ${err.message || err}`);
  }
}

/**
 * Process and Clone the source file into the flattened directory. Making any modifications required when processing.
 */
export function processAndCloneFile(sourcePath: string, destAbsolute: string, config: OrphanageConfig) {
  if (!fs.existsSync(sourcePath)) {
    return;
  }

  const fileName = getNameWithPathPrefix(sourcePath);
  const destFilePath = path.join(destAbsolute, fileName);

  if (fileName.endsWith('.ts') || fileName.endsWith('.tsx')) {
    const content = fs.readFileSync(sourcePath, 'utf-8');
    let updated = rewriteImportsInTSFile(content, config.ignoreFlattenImports ?? [], sourcePath);
    updated = removeBlocksWithoutFlags(updated, config.compileFlags ?? []);
    fs.writeFileSync(destFilePath, updated, 'utf-8');
  }
  else {
    fs.copyFileSync(sourcePath, destFilePath);
  }
}

/**
 * Removes the corresponding file in the destination (if it exists).
 */
export function removeSingleFile(sourcePath: string, destAbsolute: string) {
  const fileName = getNameWithPathPrefix(sourcePath);
  const destFilePath = path.join(destAbsolute, fileName);

  if (fs.existsSync(destFilePath)) {
    fs.unlinkSync(destFilePath);
  }
}

/**
 * Recursively copies all files and subdirectories from the source to the destination folder.
 * Creates the destination folder if it doesn't exist.
 */
export function copyFolderRecursive(sourceFolder: string, destinationFolder: string): void {
  if (!fs.existsSync(sourceFolder)) {
      throw new Error(`Source folder does not exist: ${sourceFolder}`);
  }

  if (!fs.existsSync(destinationFolder)) {
      fs.mkdirSync(destinationFolder, { recursive: true });
  }

  const entries = fs.readdirSync(sourceFolder, { withFileTypes: true });

  for (const entry of entries) {
      const srcPath = path.join(sourceFolder, entry.name);
      const destPath = path.join(destinationFolder, entry.name);

      if (entry.isDirectory()) {
          copyFolderRecursive(srcPath, destPath);
      } else {
          fs.copyFileSync(srcPath, destPath);
      }
  }
}

/**
 * Recursively collects all file paths within a given directory (including subdirectories).
 */
export function getAllFiles(dirPath: string): string[] {
  let results: string[] = [];
  const entries = fs.readdirSync(dirPath, { withFileTypes: true });

  for (const entry of entries) {
    const fullPath = path.join(dirPath, entry.name);
    if (entry.isDirectory()) {
      results = results.concat(getAllFiles(fullPath));
    } else {
      results.push(fullPath);
    }
  }
  return results;
}

/**
 * Clear all generated files in destination dir
 */
export function clearDestination(dirPath: string): void {
  if (!fs.existsSync(dirPath)) {
    return;
  }

  const entries = fs.readdirSync(dirPath, { withFileTypes: true });
  for (const entry of entries) {
    if (entry.isFile() && entry.name.startsWith(filePrefix)) {
      const fileToRemove = path.join(dirPath, entry.name);
      fs.unlinkSync(fileToRemove);
    }
  }
}

/**
 * Flatten file path into a filename
 */
export function getNameWithPathPrefix(dirPath: string): string {
  const workspaceFolder = getRootWorkspaceFolder();
  const config = getConfig();
  if (!workspaceFolder || !config) {
    return path.basename(dirPath);
  }
  const sourceAbsolute = path.join(workspaceFolder.uri.fsPath, config.sourceFolder);
  const relativePath = path.relative(sourceAbsolute, dirPath);
  const flattenedName = relativePath.split(path.sep).join('_');
  return `${filePrefix}${flattenedName}`;
}
import * as path from 'path';
import * as fs from 'fs';
import { OrphanageConfig } from '../config';
import { rewriteImportsInTSFile } from './tsImportFlattener';

/**
 * Process and Clone the source file into the flattened directory. Making any modifications required when processing.
 */
export function processAndCloneFile(sourcePath: string, destAbsolute: string, config: OrphanageConfig) {
  if (!fs.existsSync(sourcePath)) {
    return;
  }
  
  const fileName = path.basename(sourcePath);
  const destFilePath = path.join(destAbsolute, fileName);

  if (fileName.endsWith('.ts') || fileName.endsWith('.tsx')) {
    const content = fs.readFileSync(sourcePath, 'utf-8');
    const updated = rewriteImportsInTSFile(content, config.ignoreFlattenImports ?? []);
    fs.writeFileSync(destFilePath, updated, 'utf-8');
  } else {
    fs.copyFileSync(sourcePath, destFilePath);
  }
}

/**
 * Removes the corresponding file in the destination (if it exists).
 */
export function removeSingleFile(sourcePath: string, destAbsolute: string) {
  const fileName = path.basename(sourcePath);
  const destFilePath = path.join(destAbsolute, fileName);

  if (fs.existsSync(destFilePath)) {
    fs.unlinkSync(destFilePath);
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
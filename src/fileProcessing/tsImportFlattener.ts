import * as path from 'path';
import { getNameWithPathPrefix } from './fileProcessor';

/**
 * Rewrites relative imports in a TypeScript file to reference flattened filenames,
 * ignoring any folder structure in the import path. For example:
 *   from '../../utils/something' => from './FF_utils_something.ts'
 *
 * @param fileContent The text of the .ts file
 * @param ignorePatterns An array of substrings; if an import path contains any of these, skip rewriting
 * @param currentFilePath The absolute path of the .ts file being rewritten
 */
export function rewriteImportsInTSFile(
  fileContent: string,
  ignorePatterns: string[],
  currentFilePath: string
): string {
  const importRegex = /(\bfrom\s+['"])([^'"]+)(['"])/g;

  return fileContent.replace(importRegex, (match, prefix, importPath, suffix) => {
    // Only rewrite relative paths (starting with './' or '../').
    // Anything else is likely an NPM package or absolute import.
    if (!importPath.startsWith('.')) {
      return match;
    }

    // Skip if it matches any ignore pattern
    if (ignorePatterns.some((p) => importPath.includes(p))) {
      return match;
    }

    const absoluteImportPath = path.resolve(path.dirname(currentFilePath), importPath);
    const flattenedName = getNameWithPathPrefix(absoluteImportPath);
    const newImportPath = `./${flattenedName}`;
    return `${prefix}${newImportPath}${suffix}`;
  });
}

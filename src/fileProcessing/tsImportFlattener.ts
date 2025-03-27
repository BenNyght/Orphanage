import * as path from 'path';

/**
 * Rewrites relative imports in the given file content to single-level flattened form.
 * e.g., from '../../utils/something' => './something'
 * If an import path includes any ignore pattern or doesn't start with '.', skip rewriting.
 */
export function rewriteImportsInTSFile(fileContent: string, ignorePatterns: string[]): string {
  // Matches: from "..." or from '...'
  const importRegex = /(\bfrom\s+['"])([^'"]+)(['"])/g;

  return fileContent.replace(importRegex, (match, prefix, importPath, suffix) => {
	// If path doesn't start with '.', it's external or absolute -> skip
	if (!importPath.startsWith('.')) {
	  return match;
	}

	// If path is in ignore list -> skip
	if (ignorePatterns.some(p => importPath.includes(p))) {
	  return match;
	}

	// Flatten to just the base name
	const base = path.basename(importPath);
	return `${prefix}./${base}${suffix}`;
  });
}

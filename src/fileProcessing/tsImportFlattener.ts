import * as path from 'path';
import { getNameWithPathPrefix } from './fileProcessor';

/**
 * Rewrites relative imports in a TypeScript file to reference flattened filenames.
 * e.g. from '../../utils/something' => './FF_utils_something.ts'
 *
 * @param fileContent The entire text of the .ts file
 * @param ignorePatterns If an import path includes any string in this list, skip rewriting
 * @param currentFilePath The absolute path of the .ts file being rewritten
 */
export function rewriteImportsInTSFile(
	fileContent: string,
	ignorePatterns: string[],
	currentFilePath: string
): string {
	const importRegex = /(\bfrom\s+['"])([^'"]+)(['"])/g;

	return fileContent.replace(importRegex, (match, prefix, importPath, suffix) => {
		// If path doesn't start with '.' it's probably external/absolute => skip
		if (!importPath.startsWith('.')) {
			return match;
		}

		if (ignorePatterns.some((p) => importPath.includes(p))) {
			return match;
		}

		const absoluteImport = path.resolve(path.dirname(currentFilePath), importPath);
		const flattenedName = getNameWithPathPrefix(absoluteImport);
		const newImportPath = `./${flattenedName}`;
		return `${prefix}${newImportPath}${suffix}`;
	});
}
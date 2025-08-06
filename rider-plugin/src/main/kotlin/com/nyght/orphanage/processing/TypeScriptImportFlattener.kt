package com.nyght.orphanage.processing

import com.intellij.openapi.project.Project
import java.nio.file.Path
import java.nio.file.Paths

/**
 * TypeScript import flattener - equivalent to tsImportFlattener.ts
 * Rewrites relative imports in TypeScript files to reference flattened filenames
 */
object TypeScriptImportFlattener {
    
    /**
     * Rewrites relative imports in a TypeScript file to reference flattened filenames,
     * ignoring any folder structure in the import path. For example:
     *   from '../../utils/something' => from './PF_utils_something.ts'
     *
     * @param fileContent The text of the .ts file
     * @param currentFilePath The absolute path of the .ts file being rewritten
     * @param workspaceRoot The workspace root path
     * @param project The current project
     */
    fun rewriteImportsInTSFile(
        fileContent: String,
        currentFilePath: Path,
        workspaceRoot: Path,
        project: Project
    ): String {
        val config = com.nyght.orphanage.config.OrphanageConfigManager.loadConfig(project)
        val ignorePatterns = config?.ignoreFlattenImports ?: emptyList()
        val importRegex = Regex("""(\bfrom\s+['"])([^'"]+)(['"])""")
        
        return importRegex.replace(fileContent) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val importPath = matchResult.groupValues[2]
            val suffix = matchResult.groupValues[3]
            
            println("Orphanage: Processing import: '$importPath'")
            
            // Only rewrite relative paths (starting with './' or '../').
            // Anything else is likely an NPM package or absolute import.
            if (!importPath.startsWith(".")) {
                println("Orphanage: Skipping non-relative import: '$importPath'")
                return@replace matchResult.value
            }
            
            // Skip imports that match ignore patterns
            if (ignorePatterns.any { pattern -> importPath.contains(pattern) }) {
                println("Orphanage: Ignoring import due to pattern match: '$importPath' (patterns: $ignorePatterns)")
                return@replace matchResult.value
            }
            
            println("Orphanage: Rewriting import: '$importPath'")
            
            // Resolve the absolute import path
            val absoluteImportPath = currentFilePath.parent.resolve(Paths.get(importPath)).normalize()
            
            // Convert to flattened name
            val flattenedName = FileProcessor.getNameWithPathPrefix(absoluteImportPath, workspaceRoot, project)
            
            // Remove extension if it exists, as we'll let TypeScript resolve it
            val nameWithoutExtension = if (flattenedName.endsWith(".ts") || flattenedName.endsWith(".tsx")) {
                flattenedName.substringBeforeLast(".")
            } else {
                flattenedName
            }
            
            val newImportPath = "./$nameWithoutExtension"
            "$prefix$newImportPath$suffix"
        }
    }
}
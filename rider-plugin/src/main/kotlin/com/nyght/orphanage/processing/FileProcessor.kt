package com.nyght.orphanage.processing

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.nyght.orphanage.config.OrphanageConfigManager
import com.nyght.orphanage.config.OrphanageConfigStateHelper
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

/**
 * Main file processor for Orphanage plugin - equivalent to fileProcessor.ts
 */
object FileProcessor {
    private const val FILE_PREFIX = "PF_"
    
    /**
     * Process all files in selected destination
     */
    fun processAllFiles(project: Project) {
        try {
            val config = OrphanageConfigManager.loadConfig(project)
            if (config == null) {
                showErrorMessage(project, "No orphanage.json configuration found")
                return
            }
            
            val selectedDestination = OrphanageConfigStateHelper.getSelectedDestination(project)
            if (selectedDestination == null) {
                showErrorMessage(project, "No destination path selected")
                return
            }
            
            val projectBasePath = Paths.get(project.basePath ?: return)
            val rootDestinationFolder = Paths.get(OrphanageConfigStateHelper.getRootDestinationFolder(project))
            val sourceAbsolute = projectBasePath.resolve(config.sourceFolder)
            val destAbsolute = rootDestinationFolder.resolve(selectedDestination.folderPath)
            
            if (!sourceAbsolute.exists()) {
                showErrorMessage(project, "Source folder does not exist: $sourceAbsolute")
                return
            }
            
            // Copy destination to source files listed in config
            config.copyFromDestination.forEach { copyConfig ->
                try {
                    val configDestAbsolute = destAbsolute.resolve(copyConfig.destinationPath)
                    val configSourceAbsolute = projectBasePath.resolve(copyConfig.sourcePath)
                    
                    println("Orphanage: Copying from destination ${configDestAbsolute} to source ${configSourceAbsolute}")
                    
                    if (configDestAbsolute.exists()) {
                        clearDestination(configSourceAbsolute, forceClear = true, project = project)
                        copyFolderRecursive(configDestAbsolute, configSourceAbsolute)
                    } else {
                        println("Orphanage: Destination path does not exist: ${configDestAbsolute}")
                    }
                } catch (e: Exception) {
                    println("Orphanage: Error in copyFromDestination: ${e.message}")
                    // Continue with other copy operations
                }
            }
            
            // Clear destination folder
            clearDestination(destAbsolute, project = project)
            
            // Process all files
            val allSourceFiles = getAllFiles(sourceAbsolute)
            val compileFlags = OrphanageConfigStateHelper.getAllCompileFlags(project)
            
            allSourceFiles.forEach { filePath ->
                processAndCloneFile(project, filePath, destAbsolute, compileFlags, projectBasePath)
            }
            
            showInfoMessage(project, "Flattened ${allSourceFiles.size} file(s) into \"${selectedDestination.folderPath}\"!")
            
        } catch (e: Exception) {
            showErrorMessage(project, "Error flattening folders: ${e.message}")
        }
    }
    
    /**
     * Process and clone a single file
     */
    fun processAndCloneFile(
        project: Project,
        sourcePath: Path,
        destAbsolute: Path,
        compileFlags: List<String>,
        workspaceRoot: Path
    ) {
        try {
            if (!sourcePath.exists()) {
                println("Orphanage: Source file does not exist: $sourcePath")
                return
            }
            
            val fileName = sourcePath.fileName.toString()
            
            // Skip orphanage ignore files
            if (fileName.endsWith(".orphanageIgnore")) return
            
            // Skip files that are already processed (have PF_ prefix)
            if (fileName.startsWith(FILE_PREFIX)) {
                println("Orphanage: Skipping already processed file: $fileName")
                return
            }
            
            // Skip common files that shouldn't be flattened
            if (shouldSkipFile(fileName)) {
                println("Orphanage: Skipping file type: $fileName")
                return
            }
            
            // Check if file is ignored
            if (isFileIgnored(sourcePath, workspaceRoot)) {
                println("Orphanage: File is ignored: $sourcePath")
                return
            }
        } catch (e: Exception) {
            showErrorMessage(project, "Error processing file $sourcePath: ${e.message}")
            return
        }
        
        val fileName = getNameWithPathPrefix(sourcePath, workspaceRoot, project)
        val destFilePath = destAbsolute.resolve(fileName)
        
        // Ensure destination directory exists
        destFilePath.parent?.createDirectories()
        
        // Remove existing file
        if (destFilePath.exists()) {
            destFilePath.deleteExisting()
        }
        
        when {
            fileName.endsWith(".ts") || fileName.endsWith(".tsx") -> {
                val content = sourcePath.readText()
                var updated = TypeScriptImportFlattener.rewriteImportsInTSFile(content, sourcePath, workspaceRoot, project)
                updated = TypeScriptFlagPreprocessor.removeBlocksWithoutFlags(updated, compileFlags)
                destFilePath.writeText(updated)
            }
            else -> {
                sourcePath.copyTo(destFilePath, overwrite = true)
            }
        }
    }
    
    /**
     * Remove corresponding file in destination
     */
    fun removeSingleFile(project: Project, sourcePath: Path, destAbsolute: Path, workspaceRoot: Path) {
        val fileName = getNameWithPathPrefix(sourcePath, workspaceRoot, project)
        val destFilePath = destAbsolute.resolve(fileName)
        
        if (destFilePath.exists()) {
            destFilePath.deleteExisting()
        }
    }
    
    /**
     * Recursively copy folder
     */
    private fun copyFolderRecursive(sourceFolder: Path, destinationFolder: Path) {
        try {
            if (!sourceFolder.exists()) {
                println("Orphanage: Source folder does not exist: $sourceFolder")
                return
            }
            
            if (!destinationFolder.exists()) {
                destinationFolder.createDirectories()
            }
            
            sourceFolder.listDirectoryEntries().forEach { entry ->
                try {
                    val destPath = destinationFolder.resolve(entry.fileName)
                    when {
                        entry.isDirectory() -> copyFolderRecursive(entry, destPath)
                        else -> {
                            // Skip files that shouldn't be copied
                            if (!shouldSkipFile(entry.fileName.toString())) {
                                entry.copyTo(destPath, overwrite = true)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Orphanage: Error copying ${entry}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Orphanage: Error in copyFolderRecursive: ${e.message}")
        }
    }
    
    /**
     * Get all files recursively, skipping excluded directories
     */
    private fun getAllFiles(dirPath: Path): List<Path> {
        val results = mutableListOf<Path>()
        
        if (!dirPath.exists() || !dirPath.isDirectory()) {
            return results
        }
        
        dirPath.listDirectoryEntries().forEach { entry ->
            val entryName = entry.fileName.toString()
            
            when {
                entry.isDirectory() -> {
                    // Skip common directories that should never be processed
                    if (shouldSkipDirectory(entryName)) {
                        println("Orphanage: Skipping directory: $entryName")
                    } else {
                        results.addAll(getAllFiles(entry))
                    }
                }
                else -> results.add(entry)
            }
        }
        
        return results
    }
    
    /**
     * Check if directory should be skipped entirely
     */
    private fun shouldSkipDirectory(dirName: String): Boolean {
        val skipDirectories = setOf(
            "node_modules",
            ".git",
            ".vscode", 
            ".idea",
            ".vs",
            "bin",
            "obj",
            "build",
            "dist",
            "out",
            "target",
            ".cache",
            ".temp",
            ".tmp",
            "coverage",
            ".nyc_output"
        )
        
        return skipDirectories.contains(dirName) || 
               dirName.startsWith(".") && dirName.length > 1 // Skip hidden directories except current/parent
    }
    
    /**
     * Clear destination directory of generated files
     */
    private fun clearDestination(dirPath: Path, forceClear: Boolean = false, project: Project? = null) {
        if (!dirPath.exists()) return
        
        dirPath.listDirectoryEntries().forEach { entry ->
            if (entry.isRegularFile()) {
                val fileName = entry.fileName.toString()
                val config = project?.let { OrphanageConfigManager.loadConfig(it) }
                val usePrefix = config?.useFilePrefix ?: true
                
                val shouldDelete = forceClear || 
                    fileName.startsWith(FILE_PREFIX) ||
                    (!usePrefix && project != null)
                    
                if (shouldDelete) {
                    try {
                        entry.deleteExisting()
                    } catch (e: Exception) {
                        println("Orphanage: Could not delete file $fileName: ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Generate flattened filename with optional path prefix
     */
    fun getNameWithPathPrefix(filePath: Path, workspaceRoot: Path, project: Project): String {
        val config = OrphanageConfigManager.loadConfig(project) ?: return filePath.fileName.toString()
        val sourceAbsolute = workspaceRoot.resolve(config.sourceFolder)
        val relativePath = sourceAbsolute.relativize(filePath)
        val flattenedName = relativePath.toString().replace(File.separator, "_")
        
        return if (config.useFilePrefix) {
            "$FILE_PREFIX$flattenedName"
        } else {
            flattenedName
        }
    }
    
    /**
     * Check if file should be ignored based on .orphanageIgnore files
     */
    private fun isFileIgnored(filePath: Path, workspaceRoot: Path): Boolean {
        var current = filePath.parent
        
        while (current != null && current.startsWith(workspaceRoot)) {
            val ignoreFile = current.resolve(".orphanageIgnore")
            if (ignoreFile.exists()) {
                val content = ignoreFile.readText()
                val rules = content.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                
                // Simple ignore pattern matching (could be enhanced with proper gitignore parsing)
                val relativePath = current.relativize(filePath).toString()
                for (rule in rules) {
                    if (matchesIgnoreRule(relativePath, rule)) {
                        return true
                    }
                }
            }
            current = current.parent
        }
        
        return false
    }
    
    /**
     * Simple pattern matching for ignore rules
     */
    private fun matchesIgnoreRule(path: String, rule: String): Boolean {
        return when {
            rule.contains("*") -> {
                val regex = rule.replace("*", ".*").toRegex()
                regex.matches(path)
            }
            rule.endsWith("/") -> path.startsWith(rule.dropLast(1))
            else -> path == rule || path.endsWith("/$rule")
        }
    }
    
    /**
     * Check if file should be skipped based on file type
     */
    private fun shouldSkipFile(fileName: String): Boolean {
        val skipFiles = setOf(
            // Package manager files
            "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
            "composer.json", "composer.lock",
            // Build/config files
            "tsconfig.json", "jsconfig.json", "webpack.config.js", "vite.config.js",
            "rollup.config.js", "babel.config.js", ".babelrc",
            "eslint.config.js", ".eslintrc", ".eslintrc.json", ".eslintrc.js",
            "prettier.config.js", ".prettierrc", ".prettierrc.json",
            // Git files
            ".gitignore", ".gitkeep", ".gitattributes",
            // IDE files
            ".DS_Store", "Thumbs.db",
            // Orphanage files
            ".orphanageIgnore", "orphanage.json"
        )
        
        val skipExtensions = setOf(
            ".git", ".vscode", ".idea", 
            ".npm", ".yarn", ".cache",
            ".log", ".tmp", ".temp"
        )
        
        val skipPrefixes = setOf(
            ".", // Hidden files (except those we explicitly want)
            "~", // Temporary files
        )
        
        return skipFiles.contains(fileName) ||
               skipExtensions.any { fileName.endsWith(it) } ||
               (skipPrefixes.any { fileName.startsWith(it) } && !skipFiles.contains(fileName)) ||
               fileName.contains(".lock") ||
               fileName.contains("node_modules")
    }
    
    fun showInfoMessage(project: Project, message: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Orphanage")
            .createNotification(message, NotificationType.INFORMATION)
        
        notification.notify(project)
        
        // Auto-expire after 3 seconds for info messages
        com.intellij.util.Alarm().addRequest({
            notification.expire()
        }, 3000)
    }
    
    private fun showErrorMessage(project: Project, message: String) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Orphanage")
            .createNotification(message, NotificationType.ERROR)
        
        notification.notify(project)
        
        // Auto-expire after 5 seconds for error messages (longer so user can read them)
        com.intellij.util.Alarm().addRequest({
            notification.expire()
        }, 5000)
    }
}
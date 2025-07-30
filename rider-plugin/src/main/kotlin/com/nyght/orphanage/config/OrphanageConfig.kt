package com.nyght.orphanage.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

/**
 * Configuration data class matching the VSCode extension's orphanage.json format
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OrphanageConfig(
    @JsonProperty("sourceFolder") val sourceFolder: String = "src",
    @JsonProperty("destinations") val destinations: List<DestinationEntry> = emptyList(),
    @JsonProperty("copyFromDestination") val copyFromDestination: List<DestinationToSource> = emptyList(),
    @JsonProperty("compileFlags") val compileFlags: List<String> = emptyList(),
    @JsonProperty("useFilePrefix") val useFilePrefix: Boolean = true,
    @JsonProperty("ignoreFlattenImports") val ignoreFlattenImports: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DestinationEntry(
    @JsonProperty("displayName") val displayName: String,
    @JsonProperty("folderPath") val folderPath: String,
    @JsonProperty("compileFlags") val compileFlags: List<String> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DestinationToSource(
    @JsonProperty("destinationPath") val destinationPath: String,
    @JsonProperty("sourcePath") val sourcePath: String
)

/**
 * Configuration manager for Orphanage plugin
 */
object OrphanageConfigManager {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    
    private val defaultConfig = OrphanageConfig(
        sourceFolder = "src",
        destinations = listOf(
            DestinationEntry("Destination 1", "dist1", listOf("COMPILE_FLAG_1")),
            DestinationEntry("Destination 2", "dist2", listOf("COMPILE_FLAG_2"))
        ),
        copyFromDestination = listOf(
            DestinationToSource("types", "types")
        ),
        compileFlags = listOf("COMPILE_FLAG_3"),
        useFilePrefix = true,
        ignoreFlattenImports = listOf("node_modules")
    )
    
    fun loadConfig(project: Project): OrphanageConfig? {
        val configFile = getConfigFile(project)
        
        if (configFile == null) {
            println("Orphanage: No orphanage.json found in project root: ${project.basePath}")
            return null
        }
        
        if (!configFile.exists()) {
            println("Orphanage: Config file path exists but file not found: ${configFile.path}")
            return null
        }
        
        return try {
            val content = String(configFile.contentsToByteArray())
            println("Orphanage: Successfully loaded config from ${configFile.path}")
            objectMapper.readValue<OrphanageConfig>(content)
        } catch (e: Exception) {
            println("Orphanage: Error parsing config file: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    fun createDefaultConfig(project: Project): Boolean {
        val projectDir = project.basePath ?: return false
        val configFile = File(projectDir, "orphanage.json")
        
        if (configFile.exists()) {
            return false
        }
        
        return try {
            val configJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultConfig)
            configFile.writeText(configJson)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getConfigFile(project: Project): VirtualFile? {
        val projectDir = project.basePath ?: return null
        val configPath = "$projectDir/orphanage.json"
        
        // Refresh the file system to ensure we see the latest files
        com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshAndFindFileByPath(configPath)?.let { file ->
            return file
        }
        
        // Fallback: try without refresh first
        return com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(configPath)
    }
    
    fun getConfigPath(project: Project): String? {
        val projectDir = project.basePath ?: return null
        return File(projectDir, "orphanage.json").absolutePath
    }
}
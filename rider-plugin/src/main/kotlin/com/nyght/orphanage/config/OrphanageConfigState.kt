package com.nyght.orphanage.config

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent configuration state for Orphanage plugin
 * Stores user preferences and selected destination per project
 */
@Service(Service.Level.PROJECT)
@State(
    name = "OrphanageConfigState",
    storages = [Storage("orphanage.xml")]
)
class OrphanageConfigState : PersistentStateComponent<OrphanageConfigState> {
    
    // Selected destination folder path
    var selectedDestinationPath: String? = null
    
    // Selected destination display name for UI
    var selectedDestinationDisplayName: String? = null
    
    // Root destination folder (user-configurable base path)
    var rootDestinationFolder: String = System.getProperty("user.home")
    
    // Auto-processing enabled flag
    var autoProcessEnabled: Boolean = true
    
    // Debug mode enabled flag
    var debugEnabled: Boolean = false
    
    override fun getState(): OrphanageConfigState = this
    
    override fun loadState(state: OrphanageConfigState) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project): OrphanageConfigState {
            return project.service<OrphanageConfigState>()
        }
    }
}

/**
 * Helper functions to work with config state
 */
object OrphanageConfigStateHelper {
    
    fun getSelectedDestination(project: Project): DestinationEntry? {
        val state = OrphanageConfigState.getInstance(project)
        val config = OrphanageConfigManager.loadConfig(project) ?: return null
        
        return config.destinations.find { it.folderPath == state.selectedDestinationPath }
    }
    
    fun setSelectedDestination(project: Project, destination: DestinationEntry) {
        val state = OrphanageConfigState.getInstance(project)
        state.selectedDestinationPath = destination.folderPath
        state.selectedDestinationDisplayName = destination.displayName
    }
    
    fun getRootDestinationFolder(project: Project): String {
        return OrphanageConfigState.getInstance(project).rootDestinationFolder
    }
    
    fun setRootDestinationFolder(project: Project, path: String) {
        OrphanageConfigState.getInstance(project).rootDestinationFolder = path
    }
    
    fun isAutoProcessEnabled(project: Project): Boolean {
        return OrphanageConfigState.getInstance(project).autoProcessEnabled
    }
    
    fun setAutoProcessEnabled(project: Project, enabled: Boolean) {
        OrphanageConfigState.getInstance(project).autoProcessEnabled = enabled
    }
    
    fun isDebugEnabled(project: Project): Boolean {
        return OrphanageConfigState.getInstance(project).debugEnabled
    }
    
    fun setDebugEnabled(project: Project, enabled: Boolean) {
        OrphanageConfigState.getInstance(project).debugEnabled = enabled
    }
    
    fun getAllCompileFlags(project: Project): List<String> {
        val config = OrphanageConfigManager.loadConfig(project) ?: return emptyList()
        val selectedDestination = getSelectedDestination(project)
        val state = OrphanageConfigState.getInstance(project)
        
        val flags = mutableListOf<String>()
        
        // Add global compile flags
        flags.addAll(config.compileFlags)
        
        // Add destination-specific compile flags
        selectedDestination?.let { dest ->
            flags.addAll(dest.compileFlags)
        }
        
        // Add debug flag if enabled
        if (state.debugEnabled) {
            flags.add("DEBUG")
        }
        
        return flags.distinct()
    }
}
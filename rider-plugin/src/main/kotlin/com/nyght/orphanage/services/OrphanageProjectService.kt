package com.nyght.orphanage.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.nyght.orphanage.config.OrphanageConfigStateHelper
import com.nyght.orphanage.ui.OrphanageToolWindowContent

/**
 * Project-level service for Orphanage plugin
 */
@Service(Service.Level.PROJECT)
class OrphanageProjectService(private val project: Project) {
    
    private var toolWindowContent: OrphanageToolWindowContent? = null
    private var autoProcessEnabled: Boolean = true
    
    fun initialize() {
        // Initialize auto-process state from config
        autoProcessEnabled = OrphanageConfigStateHelper.isAutoProcessEnabled(project)
    }
    
    fun setAutoProcessEnabled(enabled: Boolean) {
        autoProcessEnabled = enabled
    }
    
    fun isAutoProcessEnabled(): Boolean {
        return autoProcessEnabled
    }
    
    fun setToolWindowContent(content: OrphanageToolWindowContent) {
        toolWindowContent = content
        content.updateContent()
    }
    
    fun updateToolWindow() {
        toolWindowContent?.updateContent()
    }
    
    fun refreshToolWindow() {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Orphanage")
        if (toolWindow?.isVisible == true) {
            updateToolWindow()
        }
    }
}
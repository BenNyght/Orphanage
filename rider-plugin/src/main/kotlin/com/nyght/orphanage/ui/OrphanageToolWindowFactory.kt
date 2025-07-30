package com.nyght.orphanage.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the Orphanage tool window
 */
class OrphanageToolWindowFactory : ToolWindowFactory, DumbAware {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = OrphanageToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.getContent(),
            "Controls",
            false
        )
        toolWindow.contentManager.addContent(content)
        
        // Register tool window content with project service
        val projectService = project.getService(com.nyght.orphanage.services.OrphanageProjectService::class.java)
        projectService.setToolWindowContent(toolWindowContent)
        projectService.initialize()
    }
}
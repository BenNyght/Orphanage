package com.nyght.orphanage.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.nyght.orphanage.processing.FileProcessor

/**
 * Action to flatten project files - equivalent to VSCode's 'Orphanage.flatten' command
 */
class FlattenProjectAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        FileProcessor.processAllFiles(project)
    }
    
    override fun update(e: AnActionEvent) {
        // Enable action only when we have a project
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
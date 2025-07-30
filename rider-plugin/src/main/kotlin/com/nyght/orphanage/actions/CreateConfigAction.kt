package com.nyght.orphanage.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.nyght.orphanage.config.OrphanageConfigManager

/**
 * Action to create default config - equivalent to VSCode's 'Orphanage.createConfig' command
 */
class CreateConfigAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        if (OrphanageConfigManager.createDefaultConfig(project)) {
            showInfoMessage(project, "Created config file. Restart IDE to apply changes.")
        } else {
            showInfoMessage(project, "Config file already exists.")
        }
    }
    
    override fun update(e: AnActionEvent) {
        // Enable action only when we have a project
        e.presentation.isEnabledAndVisible = e.project != null
    }
    
    private fun showInfoMessage(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Orphanage")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}
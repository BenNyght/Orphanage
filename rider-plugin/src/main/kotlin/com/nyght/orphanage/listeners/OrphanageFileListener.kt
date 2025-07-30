package com.nyght.orphanage.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.util.Alarm
import com.nyght.orphanage.config.OrphanageConfigManager
import com.nyght.orphanage.config.OrphanageConfigStateHelper
import com.nyght.orphanage.processing.FileProcessor
import com.nyght.orphanage.services.OrphanageProjectService
import java.nio.file.Paths

/**
 * File listener for auto-processing changes - equivalent to autoRunPartial.ts
 */
class OrphanageFileListener : AsyncFileListener {
    
    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD)
    private val processDelay = 1000 // 1 second delay like VSCode extension
    
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
        val relevantEvents = events.filter { event ->
            isRelevantEvent(event)
        }
        
        if (relevantEvents.isEmpty()) {
            return null
        }
        
        return object : AsyncFileListener.ChangeApplier {
            override fun afterVfsChange() {
                // Cancel previous pending process
                alarm.cancelAllRequests()
                
                // Schedule processing with delay
                alarm.addRequest({
                    processRelevantEvents(relevantEvents)
                }, processDelay)
            }
        }
    }
    
    private fun isRelevantEvent(event: VFileEvent): Boolean {
        val file = event.file ?: return false
        
        return when (event) {
            is VFileCreateEvent,
            is VFileDeleteEvent,
            is VFileContentChangeEvent,
            is VFileMoveEvent -> {
                // Check if this is in a project with Orphanage config
                val project = ProjectManager.getInstance().openProjects.find { project ->
                    val basePath = project.basePath ?: return@find false
                    file.path.startsWith(basePath) && OrphanageConfigManager.loadConfig(project) != null
                } ?: return false
                
                // Check if auto-processing is enabled for this project
                val projectService = project.getService(OrphanageProjectService::class.java)
                if (!projectService.isAutoProcessEnabled()) {
                    return false
                }
                
                // Check if file is in source folder
                val config = OrphanageConfigManager.loadConfig(project) ?: return false
                val sourceFolder = Paths.get(project.basePath!!).resolve(config.sourceFolder)
                val filePath = Paths.get(file.path)
                
                filePath.startsWith(sourceFolder)
            }
            else -> false
        }
    }
    
    private fun processRelevantEvents(events: List<VFileEvent>) {
        ApplicationManager.getApplication().runReadAction {
            val projectsToProcess = mutableSetOf<com.intellij.openapi.project.Project>()
            
            for (event in events) {
                val file = event.file ?: continue
                
                // Find the project this file belongs to
                val project = ProjectManager.getInstance().openProjects.find { project ->
                    val basePath = project.basePath ?: return@find false
                    file.path.startsWith(basePath)
                } ?: continue
                
                projectsToProcess.add(project)
                
                // Process individual file changes
                when (event) {
                    is VFileCreateEvent, is VFileContentChangeEvent -> {
                        processFileChange(project, file, isDelete = false)
                    }
                    is VFileDeleteEvent -> {
                        processFileChange(project, file, isDelete = true)
                    }
                    is VFileMoveEvent -> {
                        // Handle move as delete + create
                        val moveEvent = event as VFileMoveEvent
                        moveEvent.oldPath?.let { oldPath ->
                            // Create a virtual file reference for the old path
                            val oldPathStr = oldPath
                            processFileChange(project, file, isDelete = true) // Process as delete first
                        }
                        processFileChange(project, file, isDelete = false)
                    }
                }
            }
            
            // Update tool windows for affected projects
            projectsToProcess.forEach { project ->
                val projectService = project.getService(OrphanageProjectService::class.java)
                projectService.refreshToolWindow()
            }
        }
    }
    
    private fun processFileChange(project: com.intellij.openapi.project.Project, file: VirtualFile, isDelete: Boolean) {
        val config = OrphanageConfigManager.loadConfig(project) ?: return
        val selectedDestination = OrphanageConfigStateHelper.getSelectedDestination(project) ?: return
        
        val projectBasePath = Paths.get(project.basePath!!)
        val rootDestinationFolder = Paths.get(OrphanageConfigStateHelper.getRootDestinationFolder(project))
        val destAbsolute = rootDestinationFolder.resolve(selectedDestination.folderPath)
        val sourcePath = Paths.get(file.path)
        
        if (isDelete) {
            println("Orphanage: AUTO-PROCESS - Removing single file: ${file.name}")
            FileProcessor.removeSingleFile(project, sourcePath, destAbsolute, projectBasePath)
            FileProcessor.showInfoMessage(project, "Auto-process: Removed file \"${file.name}\"")
        } else {
            println("Orphanage: AUTO-PROCESS - Processing single file: ${file.name}")
            val compileFlags = OrphanageConfigStateHelper.getAllCompileFlags(project)
            FileProcessor.processAndCloneFile(project, sourcePath, destAbsolute, compileFlags, projectBasePath)
            FileProcessor.showInfoMessage(project, "Auto-process: Updated file \"${file.name}\"")
        }
    }
}
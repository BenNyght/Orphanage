package com.nyght.orphanage.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import com.nyght.orphanage.config.DestinationEntry
import com.nyght.orphanage.config.OrphanageConfigManager
import com.nyght.orphanage.config.OrphanageConfigStateHelper
import com.nyght.orphanage.processing.FileProcessor
import com.nyght.orphanage.services.OrphanageProjectService
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Tool window content equivalent to VSCode's extensionView.html
 */
class OrphanageToolWindowContent(private val project: Project) {
    
    private val projectService = project.getService(OrphanageProjectService::class.java)
    
    private lateinit var destinationComboBox: ComboBox<DestinationEntry>
    private lateinit var autoProcessCheckBox: JBCheckBox
    private lateinit var debugModeCheckBox: JBCheckBox
    private lateinit var rootFolderTextField: JBTextField
    private lateinit var processButton: JButton
    
    private var isUpdatingUI = false
    
    fun getContent(): JComponent {
        return panel {
            group("Destination") {
                row {
                    destinationComboBox = ComboBox<DestinationEntry>().apply {
                        renderer = DestinationListCellRenderer()
                        addActionListener { onDestinationChanged() }
                    }
                    cell(destinationComboBox)
                        .align(AlignX.FILL)
                }
            }
            
            group("Settings") {
                row {
                    autoProcessCheckBox = checkBox("Auto-Process")
                        .onChanged { checkbox ->
                            OrphanageConfigStateHelper.setAutoProcessEnabled(project, checkbox.isSelected)
                            projectService.setAutoProcessEnabled(checkbox.isSelected)
                        }
                        .component
                }
                
                row {
                    debugModeCheckBox = checkBox("Debug Mode")
                        .onChanged { checkbox ->
                            OrphanageConfigStateHelper.setDebugEnabled(project, checkbox.isSelected)
                        }
                        .component
                }
            }
            
            group("Actions") {
                row {
                    processButton = button("Process/Flatten Now") { 
                        onProcessAll() 
                    }.component
                }
            }
            
            separator()
            
            group("Root Project Folder") {
                row {
                    rootFolderTextField = textField()
                        .align(AlignX.FILL)
                        .onChanged { textField ->
                            OrphanageConfigStateHelper.setRootDestinationFolder(project, textField.text)
                        }
                        .component
                }
            }
        }.apply {
            border = JBUI.Borders.empty(8)
        }
    }
    
    /**
     * Update the UI with current configuration
     */
    fun updateContent() {
        isUpdatingUI = true
        try {
            val config = OrphanageConfigManager.loadConfig(project)
            val state = OrphanageConfigStateHelper
            
            // Update destinations combo box
            destinationComboBox.removeAllItems()
            if (config != null) {
                config.destinations.forEach { destination ->
                    destinationComboBox.addItem(destination)
                }
                
                // Set selected destination
                val selectedDestination = state.getSelectedDestination(project)
                if (selectedDestination != null) {
                    destinationComboBox.selectedItem = selectedDestination
                }
            } else {
                // Add placeholder when no config
                destinationComboBox.addItem(DestinationEntry("No config found", "", emptyList()))
            }
            
            // Update checkboxes
            autoProcessCheckBox.isSelected = state.isAutoProcessEnabled(project)
            debugModeCheckBox.isSelected = state.isDebugEnabled(project)
            
            // Update root folder
            rootFolderTextField.text = state.getRootDestinationFolder(project)
            
            // Enable/disable controls based on config availability
            val hasConfig = config != null
            destinationComboBox.isEnabled = hasConfig
            processButton.isEnabled = hasConfig
        } finally {
            isUpdatingUI = false
        }
    }
    
    private fun onDestinationChanged() {
        // Don't trigger during UI updates
        if (isUpdatingUI) return
        
        val selectedDestination = destinationComboBox.selectedItem as? DestinationEntry
        if (selectedDestination != null) {
            OrphanageConfigStateHelper.setSelectedDestination(project, selectedDestination)
            // Only show a message that destination changed, don't auto-process all files
            FileProcessor.showInfoMessage(project, "Destination changed to \"${selectedDestination.displayName}\"")
        }
    }
    
    private fun onProcessAll() {
        FileProcessor.processAllFiles(project)
    }
    
    /**
     * Custom renderer for destination combo box
     */
    private class DestinationListCellRenderer : com.intellij.ui.SimpleListCellRenderer<DestinationEntry>() {
        override fun customize(
            list: javax.swing.JList<out DestinationEntry>,
            value: DestinationEntry?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            text = value?.displayName ?: "None"
        }
    }
}
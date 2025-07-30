package com.nyght.orphanage.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.nyght.orphanage.config.OrphanageConfigStateHelper
import javax.swing.JComponent

/**
 * Settings configurable for Orphanage plugin
 */
class OrphanageConfigurable(private val project: Project) : Configurable {
    
    private lateinit var rootFolderTextField: JBTextField
    
    override fun getDisplayName(): String = "Orphanage"
    
    override fun createComponent(): JComponent {
        return panel {
            group("Orphanage Settings") {
                row("Root Destination Folder:") {
                    rootFolderTextField = textField()
                        .align(AlignX.FILL)
                        .component
                }
                
                row {
                    comment("The base directory where destination folders are resolved relative to.")
                }
            }
            
            group("Configuration File") {
                row {
                    comment("Create an 'orphanage.json' file in your project root to configure source folders, destinations, and compile flags.")
                }
                row {
                    comment("Use the 'Orphanage: Create Default Config' action to generate a sample configuration.")
                }
            }
        }
    }
    
    override fun isModified(): Boolean {
        val currentRootFolder = OrphanageConfigStateHelper.getRootDestinationFolder(project)
        return rootFolderTextField.text != currentRootFolder
    }
    
    override fun apply() {
        OrphanageConfigStateHelper.setRootDestinationFolder(project, rootFolderTextField.text)
    }
    
    override fun reset() {
        rootFolderTextField.text = OrphanageConfigStateHelper.getRootDestinationFolder(project)
    }
}
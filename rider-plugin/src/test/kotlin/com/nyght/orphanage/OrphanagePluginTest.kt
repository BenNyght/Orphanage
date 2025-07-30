package com.nyght.orphanage

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.nyght.orphanage.config.OrphanageConfig
import com.nyght.orphanage.config.OrphanageConfigManager
import com.nyght.orphanage.processing.TypeScriptFlagPreprocessor
import com.nyght.orphanage.processing.TypeScriptImportFlattener
import java.nio.file.Paths

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class OrphanagePluginTest : BasePlatformTestCase() {
    
    fun testTypeScriptFlagPreprocessor() {
        val input = """
            console.log("start");
            // #if DEBUG
            console.log("debug line");
            // #endif
            console.log("end");
        """.trimIndent()
        
        val resultWithDebug = TypeScriptFlagPreprocessor.removeBlocksWithoutFlags(input, listOf("DEBUG"))
        assertTrue(resultWithDebug.contains("debug line"))
        
        val resultWithoutDebug = TypeScriptFlagPreprocessor.removeBlocksWithoutFlags(input, emptyList())
        assertFalse(resultWithoutDebug.contains("debug line"))
    }
    
    fun testTypeScriptImportFlattener() {
        val input = """
            import { utils } from './utils/helper';
            import { component } from '../components/Button';
            import { external } from 'external-package';
        """.trimIndent()
        
        val currentFile = Paths.get("/project/src/main.ts")
        val workspaceRoot = Paths.get("/project")
        
        val result = TypeScriptImportFlattener.rewriteImportsInTSFile(input, currentFile, workspaceRoot, project)
        
        // Should rewrite relative imports
        assertTrue(result.contains("from './PF_utils_helper'"))
        assertTrue(result.contains("from './PF_components_Button'"))
        
        // Should not rewrite external package imports
        assertTrue(result.contains("from 'external-package'"))
    }
    
    fun testConfigCreation() {
        val created = OrphanageConfigManager.createDefaultConfig(project)
        assertTrue("Should create config file", created)
        
        val config = OrphanageConfigManager.loadConfig(project)
        assertNotNull("Should load created config", config)
        assertEquals("src", config?.sourceFolder)
        assertEquals(2, config?.destinations?.size)
    }
}
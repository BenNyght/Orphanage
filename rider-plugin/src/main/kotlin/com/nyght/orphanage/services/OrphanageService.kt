package com.nyght.orphanage.services

import com.intellij.openapi.components.Service

/**
 * Application-level service for Orphanage plugin
 */
@Service(Service.Level.APP)
class OrphanageService {
    
    fun getVersion(): String = "0.0.3"
    
    fun isPluginEnabled(): Boolean = true
}
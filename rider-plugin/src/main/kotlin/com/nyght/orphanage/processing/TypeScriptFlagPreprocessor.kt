package com.nyght.orphanage.processing

/**
 * TypeScript flag preprocessor - equivalent to tsFlagPreprocessor.ts
 * Removes lines between #if FLAG and #endif if the FLAG is not in compileFlags
 * Supports nested blocks
 */
object TypeScriptFlagPreprocessor {
    
    /**
     * Removes lines between #if FLAG and #endif if the FLAG is not in compileFlags.
     * Supports *nested* blocks. For example:
     *
     * // #if FOO
     *   line1
     * // #if BAR
     *     line2
     * // #endif
     *   line3
     * // #endif
     *
     * A block is only active if *all* containing blocks are active.
     * If the parent block is inactive, child blocks are implicitly inactive.
     */
    fun removeBlocksWithoutFlags(
        fileContent: String,
        compileFlags: List<String>
    ): String {
        val lines = fileContent.split(Regex("\r?\n"))
        val blockStack = mutableListOf(true) // Start with root block active
        val outputLines = mutableListOf<String>()
        
        for (line in lines) {
            val trimmed = line.trim()
            
            // Check for // #if <FLAG>
            val ifMatch = Regex("""^//\s*#if\s+(\w+)""").find(trimmed)
            if (ifMatch != null) {
                val flagName = ifMatch.groupValues[1]
                val parentActive = blockStack.last()
                val currentBlockActive = parentActive && compileFlags.contains(flagName)
                
                blockStack.add(currentBlockActive)
                outputLines.add("// $trimmed = $currentBlockActive")
                continue
            }
            
            // Check for // #endif
            if (Regex("""^//\s*#endif""").matches(trimmed)) {
                if (blockStack.size > 1) {
                    blockStack.removeAt(blockStack.size - 1)
                } else {
                    // Log error - unmatched endif (in VSCode this shows an error message)
                    println("Unmatched // #endif detected")
                }
                
                outputLines.add("// $trimmed")
                continue
            }
            
            // Only include line if all surrounding blocks are active
            if (blockStack.last()) {
                outputLines.add(line)
            }
        }
        
        return outputLines.joinToString("\r\n")
    }
}
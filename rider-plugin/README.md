# Orphanage - JetBrains Rider Plugin

**Orphanage** is a JetBrains Rider plugin that **flattens** your project files into a single-level directory.

This is a direct port of the VSCode Orphanage extension with full feature parity.

## Features

1. **Flatten on Demand**:
   - Use the `Orphanage: Flatten Project` action to copy all files from a `sourceFolder` into a `destFolder`.
2. **Auto File Sync**:
   - When you add, edit, or delete files in `sourceFolder`, Orphanage updates only those files in the destination after a short delay.
3. **Import Rewriting**:
   - For `.ts` or `.tsx` files, relative import paths are updated to reflect the flattened file structure (e.g., `../../utils/foo` → `./foo`).
4. **Compile Flag Processing**:
   - Need some code to disabled and enable based on the destination? You can setup compile flags in the config to strip away code.
5. **Copy From Destination**:
   - Have files automatically copy back to your working space based on your target destination. This can be useful if you need to copy back files to link to.
6. **Ignore Files**:
   - Often you have files in your project you don't want to have flattened and processed. `.orphanageIgnore` files can be used to ignore any files. Following the same formatting rules at `.gitignore`
7. **Configurable File Prefixes**:
   - Control whether flattened files use the `PF_` prefix with the `useFilePrefix` option. When disabled, files are flattened without the prefix for cleaner output.
8. **Import Flattening Control**:
   - Use `ignoreFlattenImports` to specify import patterns that should be ignored during TypeScript import rewriting, useful for excluding package imports and type definitions.

## Usage

### Tool Window

The plugin provides a tool window (accessible from the right sidebar) for easy swapping of current destination, and toggling of user preferences.

The Root Project Folder defines where the destinations are relative to. This is defined per user.

### Configuration

1. **Manual Flatten**  
   - Use the **Tools > Orphanage > Flatten Project** action or right-click in the project view and select **Orphanage > Flatten Project**.
   - All files in your `sourceFolder` are copied to `destFolder`, with TypeScript imports rewritten if necessary.

2. **Partial Auto-Run**  
   - By default, Orphanage sets up a watcher on your `sourceFolder`.
   - When you create/edit/delete a file, only that file is flattened in the destination folder.
   - This incremental approach is more efficient for large projects than constantly re-flattening everything.

3. **Configuration**  
   - In your project root, include a file named `orphanage.json`:

     ```jsonc
     {
         "sourceFolder": "src",
         "useFilePrefix": true,
         "ignoreFlattenImports": [
            "node_modules",
            "@types/",
            "react"
         ],
         "destinations": [
            {
               "displayName": "Horizon World Folder",
               "folderPath": "New world_9494984697284707\\scripts\\",
               "compileFlags": [
                  "COMPILE_FLAG_1"
               ]
            },
            {
               "displayName": "Destination 2",
               "folderPath": "flattened2",
               "compileFlags": [
                  "COMPILE_FLAG_2"
               ]
            }
         ],
         "copyFromDestination": [
            {
               "destinationPath": "types",
               "sourcePath": "types"
            }
         ],
         "compileFlags": [
            "COMPILE_FLAG_3"
         ]
      }
     ```

   - If `orphanage.json` is missing, use the **Tools > Orphanage > Create Default Config** action to generate a default config.

## Installation

1. Download the plugin JAR or build from source
2. Install via **File > Settings > Plugins > Install Plugin from Disk**
3. Restart Rider
4. The Orphanage tool window should appear in the right sidebar

## Building from Source

```bash
./gradlew buildPlugin
```

The built plugin will be in `build/distributions/`.

## Version

This plugin matches the functionality of VSCode Orphanage extension version 0.0.3.
# Orphanage Rider Plugin - Development Setup

## IntelliJ IDEA / Rider Setup

### 1. Open Project
1. Open IntelliJ IDEA or Rider
2. Choose "Open" and select the `rider-plugin` folder
3. IDEA should automatically detect it as a Gradle project

### 2. Import Gradle Project
1. If not auto-imported, you'll see a notification to "Import Gradle Project" - click it
2. Accept default settings and click "OK"
3. Wait for Gradle to sync and download dependencies

### 3. Build and Run

#### Method 1: Gradle Tool Window
1. Open Gradle tool window (View → Tool Windows → Gradle)
2. Navigate to `orphanage-rider-plugin → Tasks → build`
3. Double-click `buildPlugin` to build the plugin
4. The built plugin will be in `build/distributions/`

#### Method 2: Run Configurations
1. Click "Add Configuration" (top right)
2. Click "+" and select "Gradle"
3. Name: "Build Plugin"
4. Gradle project: select the current project
5. Tasks: `buildPlugin`
6. Click "OK"
7. Click the run button to build

#### Method 3: Terminal
1. Open Terminal in IDEA (View → Tool Windows → Terminal)
2. Run: `./gradlew buildPlugin` (Linux/Mac) or `gradlew.bat buildPlugin` (Windows)

### 4. Test Plugin
1. After building, find the JAR in `build/distributions/`
2. Go to File → Settings → Plugins
3. Click gear icon → "Install Plugin from Disk..."
4. Select the generated JAR file
5. Restart the IDE

### 5. Development Workflow
1. For development/testing, use the "Run Plugin" gradle task
2. This will start a new IDE instance with your plugin loaded
3. Navigate to `orphanage-rider-plugin → Tasks → intellij`
4. Double-click `runIde`

## Troubleshooting

### Common Issues:

**Java Version**: Ensure you have JDK 17+ installed
- Check: File → Project Structure → Project Settings → Project → Project SDK

**Gradle Sync Failed**: 
- Try: File → Invalidate Caches and Restart
- Or refresh Gradle project in Gradle tool window

**Plugin Not Loading**:
- Check that the plugin.xml is properly configured
- Verify all dependencies are resolved
- Look at IDE logs for error messages

### Build Variants:
- `buildPlugin`: Builds production plugin JAR
- `runIde`: Runs IDE with plugin for testing
- `test`: Runs unit tests
- `verifyPlugin`: Validates plugin structure

## Plugin Features

The Orphanage Rider Plugin includes several configuration options in `orphanage.json`:

### Core Configuration Options

**`useFilePrefix`** (boolean, default: `true`)
- Controls whether flattened files use the `PF_` prefix
- When `false`, files are flattened without the prefix for cleaner output
- Example: `src/components/Button.tsx` becomes `components_Button.tsx` instead of `PF_components_Button.tsx`

**`ignoreFlattenImports`** (array of strings, default: `[]`)
- Specifies import patterns to ignore during TypeScript import flattening
- Useful for excluding package imports, type definitions, or specific modules
- Supports partial string matching within import paths
- Example: `["node_modules", "@types/", "react"]` will skip flattening imports containing these patterns

### Example Configuration

```json
{
  "sourceFolder": "src",
  "useFilePrefix": false,
  "ignoreFlattenImports": [
    "node_modules",
    "@types/",
    "react"
  ],
  "destinations": [
    {
      "displayName": "Production Build",
      "folderPath": "dist",
      "compileFlags": ["PROD_FLAG"]
    }
  ],
  "compileFlags": ["GLOBAL_FLAG"]
}
```

## IDE Configuration

The project includes:
- `.idea/gradle.xml` - Gradle integration settings
- `.idea/misc.xml` - Project SDK and framework settings  
- `.idea/vcs.xml` - Version control settings

These should be automatically recognized by IntelliJ IDEA.
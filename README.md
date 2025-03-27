# Orphanage

## All your files, now parentless and afraid

**Orphanage** is a Visual Studio Code extension that **flattens** your project files into a single-level directory. Its main features include:

- **Manual Flatten**: Copy (and optionally rewrite imports in) all files from a source folder into a single-level destination folder.
- **Partial Auto-Run**: Watch the source folder and update only changed files in the destination, keeping everything in sync.
- **Flexible Configuration**: Reads an `orphanage.json` in your workspace root to define source/destination paths and ignored imports.

## Table of Contents

1. [Features](#features)  
2. [Installation](#installation)  
3. [Usage](#usage)  
4. [Building from Source](#building-from-source)  
5. [Running Tests](#running-tests)  
6. [Configuration](#project-configuration)  

## Features

1. **Flatten on Demand**  
   - Use the `Orphanage.flatten` command (or “Flatten Project” in the Command Palette) to copy all files from a `sourceFolder` into a `destFolder`.
2. **Auto File Sync**  
   - When you add, edit, or delete files in `sourceFolder`, Orphanage updates only those files in the destination after a short delay.
3. **Import Rewriting**  
   - For `.ts` or `.tsx` files, relative import paths are updated to reflect the flattened file structure (e.g., `../../utils/foo` → `./foo`).
4. **Ignore Certain Imports**  
   - Set an array of patterns (e.g., `node_modules`) in `orphanage.json` to skip rewriting specific import paths.

## Installation

### A. From VSIX Package

1. **Obtain** the `.vsix` file (from a release or by building with `vsce package`).
2. In VS Code, press <kbd>Ctrl+Shift+P</kbd> (Windows/Linux) or <kbd>Cmd+Shift+P</kbd> (Mac) → select **“Extensions: Install from VSIX...”**.
3. Browse to and **select** the `.vsix` file.
4. **Reload** VS Code if prompted.

### B. From Source (Extension Development Host)

1. **Clone** or **download** this repo.
2. **Open** the folder in VS Code.
3. Press <kbd>F5</kbd> to launch a new **Extension Development Host** with Orphanage loaded.

## Usage

1. **Manual Flatten**  
   - Open the **Command Palette** (<kbd>Ctrl+Shift+P</kbd> or <kbd>Cmd+Shift+P</kbd>) and search for **“Flatten Project”** or “**Orphanage.flatten**”.
   - All files in your `sourceFolder` are copied to `destFolder`, with TypeScript imports rewritten if necessary.

2. **Partial Auto-Run**  
   - By default, Orphanage sets up a watcher on your `sourceFolder`.
   - When you create/edit/delete a file, only that file is flattened in the destination folder.
   - This incremental approach is more efficient for large projects than constantly re-flattening everything.

3. **Configuration**  
   - In your workspace root, include a file named `orphanage.json`:

     ```jsonc
     {
       "sourceFolder": "src",
       "destFolder": "dist",
       "ignoreFlattenImports": [
         "fake-import",
         "test_import"
       ]
     }
     ```

   - If `orphanage.json` is missing, Orphanage creates one with default values.

## Building from Source

1. **Prerequisites**  
   - [Node.js](https://nodejs.org/)  
   - [npm](https://www.npmjs.com/)  
   - [VS Code](https://code.visualstudio.com/)

2. **Install dependencies**  
	```bash
	npm install
	```

3. **Compile**
	```bash
	npm run compile
	```

4. **(Optional) Package as VSIX**
	```bash
	npm install -g vsce
	vsce package
	```

## Running Tests
1. **Compile & test together:**
	```bash
	npm test
	```
	- This uses the default Mocha-based test runner with ```@vscode/test-electron```.

2. **View** results in the terminal, or open the Test Explorer in VS Code (depending on your setup).

3. **Integration**
	- If you have special test configs, you can press <kbd>F5</kbd> and select “Extension Tests” in the debug dropdown to run them in a dedicated Extension Development Host.


## Project Configuration
The orphanage.json file can define:
- sourceFolder: Where your original files live.
- destFolder: Where the flattened files are copied.
- ignoreFlattenImports (optional): Array of strings to skip rewriting in imports.

**Example:**
```jsonc
{
  "sourceFolder": "src",
  "destFolder": "dist",
  "ignoreFlattenImports": [
    "node_modules",
    "@alias/"
  ]
}
```
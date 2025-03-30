import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import * as vscode from 'vscode';
import { OrphanageConfig, getConfig } from '../../config/config';

suite('Config Tests', () => {
  const testWorkspace = vscode.workspace.workspaceFolders?.[0];

  test('Creates config if missing', () => {
    // Create a temp directory
    const tempDir = path.join(__dirname, 'tempConfigTest');
    if (!fs.existsSync(tempDir)) {
      fs.mkdirSync(tempDir);
    }

    // Mock a "workspaceFolder"
    const mockWorkspace: vscode.WorkspaceFolder = {
      uri: vscode.Uri.file(tempDir),
      name: 'TempWorkspace',
      index: 0
    };

    // Ensure there's no orphanage.json
    const configPath = path.join(tempDir, 'orphanage.json');
    if (fs.existsSync(configPath)) {
      fs.unlinkSync(configPath);
    }

    // Call readOrCreateConfig
    const result: OrphanageConfig | null = getConfig();

    // Because it didn't exist, readOrCreateConfig should create it and return null
    assert.strictEqual(result, null, 'Expected null on first creation');

    // Now orphanage.json should exist
    assert.ok(fs.existsSync(configPath), 'orphanage.json was not created');

    // Clean up
    fs.rmSync(tempDir, { recursive: true, force: true });
  });

  test('Reads existing config', () => {
    // Create a temp directory
    const tempDir = path.join(__dirname, 'tempConfigTest2');
    if (!fs.existsSync(tempDir)) {
      fs.mkdirSync(tempDir);
    }

    //Mock a "workspaceFolder"
    const mockWorkspace: vscode.WorkspaceFolder = {
      uri: vscode.Uri.file(tempDir),
      name: 'TempWorkspace',
      index: 0
    };

    // Create a custom orphanage.json
    const configPath = path.join(tempDir, 'orphanage.json');
    const sampleConfig: OrphanageConfig = {
      sourceFolder: 'foo',
      destinations: [
        { displayName: "bar", folderPath: "bar" },
      ],
      ignoreFlattenImports: ['baz']
    };
    fs.writeFileSync(configPath, JSON.stringify(sampleConfig), 'utf8');

    // readOrCreateConfig should return our existing config
    const result = getConfig();
    assert.ok(result, 'Expected a config object');
    assert.strictEqual(result?.sourceFolder, 'foo');
    assert.strictEqual(result?.destinations[0], 'bar');
    assert.deepStrictEqual(result?.ignoreFlattenImports, ['baz']);

    // Clean up
    fs.rmSync(tempDir, { recursive: true, force: true });
  });
});

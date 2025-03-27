import * as assert from 'assert';
import * as fs from 'fs';
import * as path from 'path';
import { OrphanageConfig } from '../../config';
import { processAndCloneFile, removeSingleFile } from '../../fileProcessing/fileProcessor';

suite('Partial Flatten Tests', () => {

  const tempDir = path.join(__dirname, 'partialFlattenTest');
  const destDir = path.join(tempDir, 'dest');

  // Sample config
  const config: OrphanageConfig = {
    sourceFolder: 'src',
    destFolder: 'dist',
    ignoreFlattenImports: ['node_modules']
  };

  setup(() => {
    // Create fresh temp directory
    if (fs.existsSync(tempDir)) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
    fs.mkdirSync(tempDir);
    fs.mkdirSync(destDir);
  });

  teardown(() => {
    // Clean up
    if (fs.existsSync(tempDir)) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  });

  test('flattenSingleFile copies .ts with rewritten imports', () => {
    const sourceFile = path.join(tempDir, 'example.ts');
    const content = `import foo from '../foo'; import nodeDep from 'node_modules/lib';`;
    fs.writeFileSync(sourceFile, content, 'utf8');

    processAndCloneFile(sourceFile, destDir, config);

    const copiedPath = path.join(destDir, 'example.ts');
    assert.ok(fs.existsSync(copiedPath), 'File not copied');
    const newContent = fs.readFileSync(copiedPath, 'utf8');
    // ../foo => ./foo
    assert.ok(newContent.includes(`import foo from './foo'`), 'Relative path not rewritten');
    // node_modules is ignored
    assert.ok(newContent.includes(`import nodeDep from 'node_modules/lib'`), 'node_modules import was incorrectly rewritten');
  });

  test('flattenSingleFile copies non-TS verbatim', () => {
    const sourceFile = path.join(tempDir, 'example.js');
    fs.writeFileSync(sourceFile, `console.log("test");`, 'utf8');

    processAndCloneFile(sourceFile, destDir, config);

    const copiedPath = path.join(destDir, 'example.js');
    assert.ok(fs.existsSync(copiedPath), 'JS file not copied');
    const newContent = fs.readFileSync(copiedPath, 'utf8');
    // Should be identical
    assert.strictEqual(newContent, `console.log("test");`);
  });

  test('removeSingleFile deletes flattened file', () => {
    // Simulate a file that was previously flattened
    const flattenedFile = path.join(destDir, 'example.ts');
    fs.writeFileSync(flattenedFile, 'some content', 'utf8');

    // Remove it
    const sourceFile = path.join(tempDir, 'example.ts');
    removeSingleFile(sourceFile, destDir);

    assert.ok(!fs.existsSync(flattenedFile), 'File not removed');
  });

});

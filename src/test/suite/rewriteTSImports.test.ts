import * as assert from 'assert';
import { rewriteImportsInTSFile } from '../../fileProcessing/tsImportFlattener';

suite('Rewrite Imports Tests', () => {

  test('Rewrite relative imports to flattened', () => {
    const input = `
      import foo from '../utils/foo';
      import bar from './sub/bar';
      import external from 'react';
      import nodeDep from 'node_modules/something';
    `;
    const ignorePatterns = ['node_modules'];

    const result = rewriteImportsInTSFile(input, ignorePatterns);

    // ../utils/foo => ./foo
    assert.ok(result.includes(`import foo from './foo'`), 'Expected ../utils/foo => ./foo');
    // ./sub/bar => ./bar
    assert.ok(result.includes(`import bar from './bar'`), 'Expected ./sub/bar => ./bar');
    // external import shouldn't be touched
    assert.ok(result.includes(`import external from 'react'`), 'External import changed unexpectedly');
    // node_modules import is ignored
    assert.ok(result.includes(`import nodeDep from 'node_modules/something'`), 'node_modules was incorrectly rewritten');
  });

  test('Skips non-relative imports', () => {
    const input = `import something from 'my-lib';`;
    const result = rewriteImportsInTSFile(input, []);

    // Should remain unchanged
    assert.strictEqual(result, input);
  });

});

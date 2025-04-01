import * as vscode from 'vscode';
import { getConfig } from '../config/config';

const inactiveDecorationType = vscode.window.createTextEditorDecorationType({
  opacity: '0.5'
});

export function activateTSLanaguageBlocks(context: vscode.ExtensionContext) {
  // Initial render if there's an editor open
  if (vscode.window.activeTextEditor) {
    updateAllDecorations();
  }

  context.subscriptions.push(
    vscode.window.onDidChangeActiveTextEditor(() => {
      updateAllDecorations();
    }),

    vscode.workspace.onDidChangeTextDocument(() => {
      updateAllDecorations();
    }),

    vscode.workspace.onDidChangeConfiguration(() => {
      updateAllDecorations();
    }),

    vscode.languages.registerFoldingRangeProvider({ language: 'typescript', scheme: 'file' }, {
      provideFoldingRanges(document, _context, _token) {
        const config = getConfig();
        const activeBlocks = config?.compileFlags ?? [];
        const text = document.getText();
        const ranges: vscode.FoldingRange[] = [];
        const inactiveRanges = getInactiveRanges(text, activeBlocks);
        for (const r of inactiveRanges) {
          const startLine = document.positionAt(r.start).line;
          const endLine = document.positionAt(r.end).line;
          if (endLine > startLine + 1) {
            ranges.push(new vscode.FoldingRange(startLine, endLine - 1, vscode.FoldingRangeKind.Region));
          }
        }
        return ranges;
      }
    })
  );
}

function updateAllDecorations() {
  for (const editor of vscode.window.visibleTextEditors) {
    updateInactiveDecorations(editor);
  }
}

function updateInactiveDecorations(editor: vscode.TextEditor) {
  if (!editor) {
	return;
  }

  const config = getConfig();
  const activeBlocks = config?.compileFlags ?? [];
  const text = editor.document.getText();
  const inactiveRanges = getInactiveRanges(text, activeBlocks);
  const decorations: vscode.DecorationOptions[] = [];

  for (const r of inactiveRanges) {
    const startPos = editor.document.positionAt(r.start);
    const endPos = editor.document.positionAt(r.end);
    decorations.push({ range: new vscode.Range(startPos, endPos) });
  }

  editor.setDecorations(inactiveDecorationType, decorations);
}

function getInactiveRanges(text: string, activeBlocks: string[]): { start: number, end: number }[] {
  const ranges: { start: number, end: number }[] = [];
  let currentPos = 0;
  const ifRegex = /^\/\/\s*#if\s+(\w+)/gm;

  while (true) {
    ifRegex.lastIndex = currentPos;
    const ifMatch = ifRegex.exec(text);
    if (!ifMatch) {
		break;
	}

    const blockName = ifMatch[1];
    const startIndex = ifMatch.index;
    const startOfBlockContent = ifRegex.lastIndex;
    let endIndex = startOfBlockContent;

    const endRegex = /^\/\/\s*#endif\b/gm;
    endRegex.lastIndex = startOfBlockContent;
    const endMatch = endRegex.exec(text);
    if (endMatch) {
      endIndex = endMatch.index + endMatch[0].length;
    } else {
      endIndex = text.length;
    }

    if (!activeBlocks.includes(blockName)) {
      ranges.push({ start: startIndex, end: endIndex });
    }

    currentPos = endIndex;
  }

  return ranges;
}

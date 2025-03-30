import * as vscode from 'vscode';
import * as fs from 'fs';
import { DestinationEntry } from '../config/config';
import { getSelectedDestination, isAutoProcessEnabled, isDebugEnabled, setAutoProcessEnabled, setDebugEnabled, setSelectedDestination } from '../config/configState';
import { setAutoProcessRunning } from '../autoRunPartial';
import { processAllFiles } from '../fileProcessing/fileProcessor';

export class DestinationsWebviewProvider implements vscode.WebviewViewProvider {
    public static readonly viewType = 'orphanageView';

    private _view?: vscode.WebviewView;
    private _destinations: DestinationEntry[] = [];

    constructor(private readonly context: vscode.ExtensionContext) { }

    resolveWebviewView(
        webviewView: vscode.WebviewView,
        _context: vscode.WebviewViewResolveContext,
        _token: vscode.CancellationToken
    ) {
        this._view = webviewView;

        webviewView.webview.options = { enableScripts: true };

        webviewView.webview.onDidReceiveMessage((message) => {
            switch (message.command) {
                case 'selectedDestination':
                    vscode.window.showInformationMessage(`Destination set to: ${message.value}`);
                    const selectedEntry: DestinationEntry | undefined = this._destinations.find(dest => dest.folderPath === message.value);
                    if (selectedEntry) {
                        setSelectedDestination(selectedEntry);
                    }
                    processAllFiles();
                    break;
                case 'toggleAutoProcess':
                    vscode.window.showInformationMessage(`Auto processing: ${message.value ? "enabled" : "disabled"}`);
                    setAutoProcessRunning(message.value);
                    break;
                case 'toggleDebugMode':
                    vscode.window.showInformationMessage(`Debug mode: ${message.value ? "enabled" : "disabled"}`);
                    setDebugEnabled(message.value);
                    break;
                case 'processAll':
                    processAllFiles();
                    break;
            }
        });

        // Render initial content
        this.updateWebviewContent();
    }

    public setAllDestinations(destinations: DestinationEntry[]): void {
        this._destinations = destinations;
        this.updateWebviewContent();
    }

    private updateWebviewContent() {
        if (!this._view) {
            return;
        }
        const webview = this._view.webview;
        const selectedDest = getSelectedDestination();
        
        // Build #OPTIONS#
        const optionsHtml = this._destinations
            .map(dest => {
                const isSelected = (selectedDest && selectedDest.folderPath === dest.folderPath) ? 'selected' : '';
                return `<option value="${dest.folderPath}" ${isSelected}>${dest.displayName}</option>`;
            })
            .join('');

        // Build #AUTO_FLATTEN_CHECKED#
        const autoFlattenState = isAutoProcessEnabled() ? 'checked' : '';

        // Build #DEBUG_MODE_CHECKED#
        const debugModeState = isDebugEnabled() ? 'checked' : '';

        // Load Html
        const htmlFilePath = vscode.Uri.joinPath(this.context.extensionUri, 'media', 'extensionView.html');
        let html = fs.readFileSync(htmlFilePath.fsPath, 'utf-8');

        // Replace Html
        html = html.replace('#OPTIONS#', optionsHtml);
        html = html.replace('#AUTO_FLATTEN_CHECKED#', autoFlattenState);
        html = html.replace('#DEBUG_MODE_CHECKED#', debugModeState);

        webview.html = html;
    }
}
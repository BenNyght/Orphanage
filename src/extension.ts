import { ExtensionContext, window, workspace, commands } from 'vscode';
import { setupPartialAutoRun } from './autoRunPartial';
import { DestinationsWebviewProvider } from './views/extensionView';
import { OrphanageConfig, getConfig } from './config/config';
import { getSelectedDestination, initializeConfigState } from './config/configState';
import { registerCreateConfigCommand, registerProcessCommand } from './commands';
import { activateTSLanaguageBlocks } from './languageService/tsLanguageStyle';

export function activate(context: ExtensionContext) {
  // Always register create config command for project setup
  registerCreateConfigCommand(context);

  // If there is no config, don't run the extension
  const config: OrphanageConfig | null = getConfig();
  if (!config) {
    return;
  }

  // Confirm selected destination
  initializeConfigState(context);
  const previouslySelected = getSelectedDestination();
  if (previouslySelected) {
    window.showInformationMessage(`Restored previous selection: ${previouslySelected.displayName}`);
  }
  
  // Register extension view
  const provider = new DestinationsWebviewProvider(context);
  context.subscriptions.push(
    window.registerWebviewViewProvider(DestinationsWebviewProvider.viewType, provider)
  );
  provider.setAllDestinations(config.destinations);

  // Auto process
  setupPartialAutoRun(context);

  // Register Commands
  registerProcessCommand(context);

  // Enable language blocks
  activateTSLanaguageBlocks(context);
}

export function deactivate() {
  // Cleanup
}
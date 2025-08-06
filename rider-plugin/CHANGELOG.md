# Changelog

All notable changes to the Orphanage Rider plugin will be documented in this file.

## [0.0.3] - 2024-01-XX

### Added
- Initial Rider plugin release
- Full feature parity with VSCode extension
- Tool window for destination management
- Auto-processing with file watching
- TypeScript import rewriting
- Compile flag preprocessing
- Configuration via orphanage.json
- Settings integration
- Project-specific state persistence

### Features
- **Flatten on Demand**: Manual project flattening via actions
- **Auto File Sync**: Automatic processing of file changes
- **Import Rewriting**: TypeScript relative import path updates
- **Compile Flag Processing**: Conditional code inclusion/exclusion
- **Copy From Destination**: Reverse file copying
- **Ignore Files**: .orphanageIgnore support
- **Tool Window**: GUI for destination selection and settings
- **Actions**: Menu and context menu integration

### Technical
- Built with Kotlin and IntelliJ Platform SDK
- Uses Rider-specific APIs for better integration
- Async file listening for performance
- Persistent configuration state
- Notification system for user feedback

## Compatibility

- JetBrains Rider 2023.2+
- IntelliJ Platform 232+
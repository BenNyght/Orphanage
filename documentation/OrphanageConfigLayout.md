# Project Configuration

The orphanage.json file can define:

- sourceFolder: Where your original files live.
- destinations: Define the display name for the destination and the path to flatten to.
- copyFromDestination: Folders to copy back from the current destination and where to clone them to in the sourceFolder.
- compileFlags: Arrary of compile flags, if a flag block is defined in code without being define in the config, it will be removed from the code when flattened.

**Example:**

```jsonc
{
   "sourceFolder": "src",
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

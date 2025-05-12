# Compile Flags

You can use conditional compile-time flags in your TypeScript files to include or exclude sections of code using `// #if <FLAG>` and `// #endif` markers.

These blocks are automatically removed from the output unless the specified `FLAG` is included in your build configuration.

## Debug Mode

If in the Orphanage panel you have "Debug Mode" enable, the `DEBUG` flag will automatically be added when processing files.

## Syntax

```ts
// #if FEATURE_X
console.log("This will only be included if FEATURE_X is defined.");
// #endif
```

You can also nest blocks:

```ts
// #if FEATURE_A
console.log("Inside A");
// #if FEATURE_B
console.log("Inside B");
// #endif
console.log("Back in A");
// #endif
```

A block is only active if:

- The flag is listed in your compile-time flags array
- All parent blocks are also active

Inactive blocks (and their nested content) are completely removed during processing.

## Example

Given the compile flags:

```csharp
["FEATURE_A"]
```

And this code:

```csharp
// #if FEATURE_A
console.log("A");
// #if FEATURE_B
console.log("B");
// #endif
console.log("Still A");
// #endif
```

The output will be:

```csharp
console.log("A");
console.log("Still A");
```

## Notes

- Nested blocks are supported and respected.
- If an unmatched // #endif is found, a error will be shown.
- Only one flag is allowed per #if directive.
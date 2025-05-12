# Ignoring Files

You can use `.orphanageIgnore` files to exclude specific files or folders from being processed and flattened. These files follow the same syntax as `.gitignore`.

## Rules

- A `.orphanageIgnore` applies to **its own directory and all subdirectories**.
- If a child directory contains its **own** `.orphanageIgnore`, it **overrides** any parent ignore rules for that subtree.
- Only the **first `.orphanageIgnore` file found when walking upward** from a file is used to decide whether to ignore it.
- Ignore rules are interpreted **relative to the folder the `.orphanageIgnore` is in**.

## Example Structure

```csharp
project/
├── .orphanageIgnore # ignores: temp/
├── temp/ # ❌ ignored
│ └── debug.ts # ❌ ignored
├── src/
│ ├── .orphanageIgnore # ignores: *.test.ts
│ ├── main.ts # ✅ included
│ └── helper.test.ts # ❌ ignored
```

- `temp/debug.ts` is ignored by `project/.orphanageIgnore`.
- `src/helper.test.ts` is ignored by `src/.orphanageIgnore`.
- `src/main.ts` is **included**, not matched by any rule.
- `src/` is **not affected** by the root ignore file since it has its own `.orphanageIgnore`.

## Example Patterns

You can use standard glob-like patterns:

| Pattern         | Description                                      |
|-----------------|--------------------------------------------------|
| `*.test.ts`     | Ignore all `.test.ts` files                      |
| `temp/`         | Ignore the `temp` folder and everything in it    |
| `!keep.ts`      | Include `keep.ts` even if excluded above         |
| `**/*.bak`      | Ignore all `.bak` files recursively              |

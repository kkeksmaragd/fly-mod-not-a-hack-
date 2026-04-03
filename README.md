# Minecraft Fabric Mod Template

This is a starter template for creating Minecraft mods using the **Fabric** modloader.

## What's inside?

| File | What it does |
|------|-------------|
| `src/main/.../TemplateMod.java` | Runs when the game/server starts. Add your main mod logic here. |
| `src/client/.../TemplateModClient.java` | Runs only on the player's game (not server). Add visuals, HUD, and key binds here. |
| `src/main/.../mixin/ExampleMixin.java` | Shows how to modify vanilla Minecraft code without replacing it entirely. |
| `fabric.mod.json` | Mod info — name, version, author, and what Java classes to load. |
| `gradle.properties` | Version numbers for Minecraft, Fabric, and your mod. Change these when updating. |

## What does this mod do RIGHT NOW?

`flymod.java` is the active mod class. It currently:

- Registers a **client-side chat command** `/fly check inv`
- When run in-game, it checks whether the player has **at least 64 glass blocks** in their inventory and prints the result in chat

More actions will be added that use the glass-check as a requirement.

## How to build the mod yourself

### Prerequisites
- **Java 21** installed and on your PATH
- No other setup needed — Gradle downloads everything else automatically

### Build commands

Open a terminal in the project folder (`C:\01_dev\minecraft_mod`) and run:

```bash
# Build a .jar you can drop into your mods folder
./gradlew build
# Output: build/libs/template-mod-1.0.0.jar

# Or launch Minecraft directly with the mod loaded (easiest for testing)
./gradlew runClient

# Launch a local test server with the mod
./gradlew runServer

# Regenerate Minecraft source files (needed after changing Minecraft/Fabric versions)
./gradlew genSources
```

### Where is the built JAR?
After `./gradlew build`, the file is at:
```
build/libs/template-mod-1.0.0.jar
```
Copy it to your Minecraft `mods/` folder alongside Fabric Loader and Fabric API.

### Useful VS Code shortcuts
| Shortcut | What it does |
|----------|-------------|
| `Ctrl+Shift+B` | Run the default build task |
| `Ctrl+`` ` | Open the integrated terminal |
| `Ctrl+P` | Quick-open any file by name |
| `Ctrl+Shift+P` | Command palette |



### Step 1 — Rename things (make it yours)
In `gradle.properties`, change:
```
mod_version=1.0.0
maven_group=com.yourname
archives_base_name=your-mod-name
```
In `fabric.mod.json`, update `"id"`, `"name"`, `"description"`, and `"authors"`.

### Step 2 — Add logic
- Open `TemplateMod.java` and add code inside `onInitialize()` to register items, blocks, commands, etc.
- Open `TemplateModClient.java` and add code inside `onInitializeClient()` for anything visual (only needed for player-side effects).

### Step 3 — Modify vanilla Minecraft behavior (optional)
`ExampleMixin.java` is an example of a **Mixin** — a way to inject your code into vanilla Minecraft classes.
The current example targets `MinecraftServer.loadLevel()` but the method body is empty, so nothing happens yet.

### Step 4 — Build and run
Run these commands in the terminal:

```bash
# Build a .jar file you can drop into a mods folder
./gradlew build

# Launch Minecraft with your mod loaded (for testing)
./gradlew runClient

# Launch a test server with your mod loaded
./gradlew runServer
```

## Useful links

- [Fabric Wiki](https://fabricmc.net/wiki/) — learn how to add blocks, items, recipes, and more
- [Fabric API on GitHub](https://github.com/FabricMC/fabric) — browse available game hooks and events
- [fabricmc.net/develop](https://fabricmc.net/develop/) — find the latest version numbers for Minecraft and Fabric

## Current versions

| Thing | Version |
|-------|---------|
| Minecraft | 1.21.10 |
| Fabric Loader | 0.18.6 |
| Fabric API | 0.138.4+1.21.10 |
| Java | 21 |

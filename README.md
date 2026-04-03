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

Almost nothing. If you added it to Minecraft today it would:

- Print `"Hello Fabric world!"` in the game log when the game starts.
- Do nothing else — no new items, blocks, or gameplay changes.

It is just a **blank starting point** for you to build on.

## How to start building your mod

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

# Mano 0.01 V

Fabric **1.21.11** client **base**. No combat, movement, or render cheats — click GUI shell and self-destruct only.

## Install

1. Minecraft **1.21.11** with [Fabric Loader](https://fabricmc.net/use/installer/) **0.18.4+** and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Drop `mano-0.0.1.jar` into `.minecraft/mods`.
3. Launch. Press **Right Shift** in-game.

Fabric loads mods at startup. You cannot attach this jar to a Minecraft process that is already running — close the game, put the jar in `mods`, start again.

## Controls

- **Right Shift** — open / close the top navbar
- **Esc** — close
- Navbar: **Module** · **Friends** · **Config** · **Self destruct**
- Hold **Right Ctrl + Backspace** for ~1.2s — silent self-destruct (no GUI)

## Self-destruct

Wipes Mano from the running session:

- Closes the GUI
- Unregisters the keybind
- Stops HUD / tick hooks
- Clears internal state

A restart is required to load Mano again. This does not delete the jar from disk.

## Build

Needs **JDK 21**.

```bash
./gradlew build
```

Jar lands in `build/libs/mano-0.0.1.jar`.

## Versions

| Piece        | Pin                    |
|--------------|------------------------|
| Minecraft    | 1.21.11                |
| Yarn         | 1.21.11+build.6        |
| Fabric Loader| 0.18.4                 |
| Fabric API   | 0.140.2+1.21.11        |
| Loom         | 1.14-SNAPSHOT          |
| Java         | 21                     |

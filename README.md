# Mano 0.01 V

Fabric **1.21.11** client **base**. No combat, movement, or render cheats — click GUI shell and self-destruct only.

## Desktop window

Double-click `mano-0.0.1.jar` (or `java -jar mano-0.0.1.jar`). A dark installer window opens:

- **Inject** — lists a running Minecraft process (`version - instance`) and copies Mano into that instance `mods` folder
- **Advanced inject** — same install path, with an explanation
- **Install** — pick a `.minecraft` / Prism / Modrinth folder by hand
- **Updates** — this build
- Gear — in-game keybinds

**This is not JVM attach.** Fabric loads mods at game start. After Inject, close Minecraft and launch again. There is no “inject into an already-running client” for a Fabric jar.

## In-game

Needs Fabric Loader **0.18.4+**, Fabric API, Minecraft **1.21.11**.

- **Shift** — open / close the top navbar
- **Esc** — close
- Navbar: **Module** · **Friends** · **Config** · **Self destruct**
- Hold **Right Ctrl + Backspace** ~1.2s — silent self-destruct (ignored in chat)

Shift is also Minecraft sneak by default. Rebind sneak in vanilla Controls if they clash.

## Self-destruct

Wipes Mano from the running session (GUI, keybind, tick hooks). The jar on disk stays. Restart to load again.

## Build

Needs **JDK 21**.

```bash
./gradlew build
```

Jar: `build/libs/mano-0.0.1.jar` and `dist/mano-0.0.1.jar`.

## Versions

- Minecraft 1.21.11
- Yarn 1.21.11+build.6
- Fabric Loader 0.18.4
- Fabric API 0.140.2+1.21.11
- Loom 1.14-SNAPSHOT
- Java 21

# Locura

Locura is a psychological horror roguelike deckbuilding game built with Java and
libGDX. Play as **The Patient**, choose a route through a branching map, and face
grotesque enemies in turn-based card battles. Build your deck, manage limited
energy, and decide when to fight, shop, or recover before the boss encounter.

## Features

- **Card combat:** play attacks, gain block, and apply status effects while
  managing energy, your draw pile, discard pile, and exhausted cards.
- **Branching runs:** choose between combat, elite encounters, rest sites,
  treasure rooms, shops, and a boss battle.
- **Deckbuilding:** start with Scalpel, Cower, and Pipe cards, then expand your
  deck through rewards and purchases.
- **Run progression:** carry health, gold, cards, and potions between rooms;
  rest to heal or improve your attack and defense bonuses.
- **Animated battles:** enemy intents, targeting arrows, attack animations,
  slash effects, and damage feedback help you follow combat.
- **Inspection tools:** view the map, deck, and card piles during battle.
- **Display settings:** toggle VSync and switch between windowed and fullscreen
  modes from the settings screen.

The game is under development. Runs currently remain in memory only: the
**LOAD GAME** menu option is a placeholder, and closing the game loses the run.
Developer tools are currently available in the game, including packaged builds.

## Play the Windows build

If you have `Locura-windows.zip`:

1. Extract the ZIP to a folder on your computer.
2. Open the extracted `Locura` folder.
3. Double-click `Locura.exe`.

Keep `Locura.exe`, `app/`, and `runtime/` together. Players do not need Java,
Gradle, or the source code installed; the package includes a Java runtime.

To create this ZIP from source, follow [Build a Windows executable](#build-a-windows-executable).

## Getting started from source

### Requirements

- A JDK compatible with the included Gradle wrapper. JDK 17 or JDK 21 are suitable;
  the project targets Java 17, and Windows packaging has been tested with JDK 21.
- `JAVA_HOME` pointing to the JDK installation directory.
- Internet access for the first build to download Gradle and dependencies.
- A desktop environment capable of running the LWJGL3 graphics backend.

Gradle is provided through the wrapper; a separate Gradle installation is not
required. Run the following commands from the directory containing this README,
`settings.gradle`, and `gradlew.bat` (`VP-Project/` in the enclosing workspace).

### Windows

```powershell
.\gradlew.bat :lwjgl3:run
```

You can also double-click `run.bat` in the project directory. The desktop launcher
opens a 1440 x 900 window by default.

If needed, set the JDK for the current PowerShell session before running Gradle:

```powershell
# Replace this example with your actual JDK installation directory.
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.11'
.\gradlew.bat :lwjgl3:run
```

### Linux and macOS

```bash
chmod +x gradlew
./gradlew :lwjgl3:run
```

The desktop launcher includes the macOS JVM startup helper required by LWJGL3.
The executable packaging tasks described below target Windows only.

## How to play

1. Choose **PRESS START** from the main menu to begin a run as The Patient.
2. Select a reachable map node to enter the next room.
3. In combat, check enemy intents and spend energy on cards. Drag attack cards
   onto an enemy; drag other cards upward into the battlefield to play them.
4. Select **End Turn** when you are finished. Enemies act, and the next player
   turn refills energy and draws a new hand.
5. Collect rewards, visit shops, and use rest sites to prepare for later fights.

### Controls

| Input | Action |
| --- | --- |
| Mouse click | Select menu options, map nodes, rewards, and HUD controls |
| Hover over a card | Enlarge the card for inspection |
| Drag a card | Play it in the battlefield; attacks require an enemy target |
| Mouse wheel over the hand | Scroll the hand horizontally |
| `Esc` during combat | Close map or pile inspection first; otherwise toggle pause |
| Arrow keys or `Tab` / `Shift+Tab` | Move between main menu buttons |
| `Enter` or `Space` | Activate the focused main menu button |
| `F1` or the grave/backtick key | Toggle developer tools |

Developer tools can jump to rooms and encounters or adjust the current run.
Use them to inspect and test gameplay during development.

## Build a Windows executable

Build on Windows using a full JDK that includes `jpackage`. Set `JAVA_HOME` as
described above, then double-click `build-exe.bat` or run:

```powershell
.\gradlew.bat :lwjgl3:packageWindowsZip
```

| Output | Purpose |
| --- | --- |
| `lwjgl3/build/windows/Locura/Locura.exe` | Launch the packaged game |
| `lwjgl3/build/windows/Locura/` | Complete portable game folder |
| `lwjgl3/build/distributions/Locura-windows.zip` | Archive to share with players |

To create the game folder without the ZIP:

```powershell
.\gradlew.bat :lwjgl3:packageWindows
```

Packaging uses the JDK's [jpackage tool](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
in `app-image` mode to bundle the launcher, assets, dependencies, and runtime.
No installer tooling is required. The package targets the architecture of the
JDK used to build it, normally Windows x64. Close the packaged game before
rebuilding, and distribute the entire folder or ZIP rather than the EXE alone.

## Development commands

| Task | Windows PowerShell |
| --- | --- |
| Compile all modules | `.\gradlew.bat compileJava --warning-mode all` |
| Assemble project artifacts | `.\gradlew.bat assemble` |
| Build and run checks | `.\gradlew.bat build` |
| Run all tests | `.\gradlew.bat test` |
| Run core tests | `.\gradlew.bat :core:test` |
| Launch with JVM debugging | `.\gradlew.bat :lwjgl3:run --debug-jvm` |
| Remove generated build outputs | `.\gradlew.bat clean` |

On Linux or macOS, replace `.\gradlew.bat` with `./gradlew`.
`assemble` creates module artifacts; use the packaging tasks for the portable
Windows application.

Tests use JUnit 5 and cover card effects, damage events, elite enemies, stat
upgrades, shop purchases, and animation behavior. The tests run without opening
an LWJGL3 window. After a test run, open
`core/build/reports/tests/test/index.html` for the report.

## Project structure

```text
VP-Project/
|-- assets/                   # Images, sprites, fonts, audio, and JSON data
|   `-- data/                 # Character, card, monster, potion, and relic definitions
|-- core/
|   `-- src/
|       |-- main/java/com/cardgame/
|       |   |-- data/          # Data models and JSON loaders
|       |   |-- logic/         # Combat, cards, monsters, rooms, and run state
|       |   |-- screens/       # Menus, map, combat, shop, and reward screens
|       |   |-- ui/            # Scene2D actors, overlays, and visual effects
|       |   `-- utils/         # Shared constants and utilities
|       `-- test/java/         # JUnit regression tests
|-- lwjgl3/                   # Desktop launcher and Windows packaging tasks
|-- gradle/wrapper/            # Gradle wrapper files
|-- tools/                    # Development and visual preview utilities
|-- build.gradle              # Shared Java and repository configuration
|-- settings.gradle           # Module definitions
|-- gradle.properties         # Dependency versions and Gradle settings
|-- build-exe.bat              # Build the portable Windows game and ZIP
`-- run.bat                    # Launch the game through Gradle
```

### Architecture

`Lwjgl3Launcher` creates `CardBattlerGame`, which manages screen transitions and
shared game data. Screens present the game through libGDX Scene2D actors.
`RunManager` retains the current run between rooms, while `GameState` holds
combat state. `TurnManager` and `CombatResolver` resolve actions and produce
`GameEvent` objects that the battle screen uses for feedback and animations.

Platform-specific startup and packaging belong in `lwjgl3/`. Keep LWJGL3 backend
imports out of `core/`, and keep combat resolution testable without a graphics
context. JSON definitions live in `assets/data/`; playable behavior also lives
in Java classes, so editing JSON alone does not add a new card or enemy.

### Technology

| Component | Configured version |
| --- | --- |
| Java language target | 17 |
| Gradle wrapper | 8.10.2 |
| libGDX / LWJGL3 backend | 1.12.1 |
| Gson | 2.11.0 |
| JUnit Jupiter | 5.11.3 |

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Gradle cannot find Java | Set `JAVA_HOME` to the JDK root, not its `bin` directory, and reopen the terminal if you changed system variables. |
| `jpackage.exe` is missing | Use a full JDK containing `bin/jpackage.exe`; a standalone JRE cannot build the executable. |
| First build cannot download dependencies | Check access to the Gradle distribution and configured Maven repositories. |
| Packaged game fails after copying it | Extract the full ZIP and keep `app/` and `runtime/` beside `Locura.exe`. |
| Packaging cannot replace existing files | Close any running copy of the packaged game, then rebuild. |
| Need startup error output | Run `.\gradlew.bat :lwjgl3:run` from a terminal to see the game logs. |
| Load Game does not resume a run | Saving and loading runs is not implemented yet. Start a new run with **PRESS START**. |

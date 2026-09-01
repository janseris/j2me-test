# Discord for J2ME
Discord client for Java ME (MIDP 1.0 and 2.0) devices, inspired by [Discord for Symbian](https://web.archive.org/web/20240402175051/https://github.com/uwmpr/discord-symbian-fixed). Uses proxy servers for the [HTTP](/proxy/) and [gateway](https://github.com/gtrxAC/discord-j2me-server) connection.

Also see [Droidcord](https://github.com/leap0x7b/Droidcord), a Discord client for old Android devices, and [Discord WAP](https://github.com/gtrxAC/discord-wap), a client for old mobile browsers.

![Screenshots](img/screenshots.png)

* [Download](https://github.com/gtrxAC/discord-j2me/releases/latest)
* [Discord server](https://discord.gg/2GKuJjQagp) (#discord-j2me-wap channel)
* [Telegram group](https://t.me/dscforsymbian)

## Status
### Working
* Server, channel and thread lists
* Direct messages and group DMs
* Message reading, sending, editing, <abbr title="Only your own messages">deleting</abbr>
* Replying to messages
* Reading older messages
* Attachment viewing
* Attachment sending (<abbr title="Requires FileConnection API or HTML browser with file uploading support">device dependent</abbr>)
* Gateway/live message updates (<abbr title="Not supported on MIDP 1.0">device dependent</abbr>)
* <abbr title="Not in sync with official clients">Unread message indicators</abbr>
* Emojis

### Not implemented
* Jumping to messages (e.g. replies)
* Initiating DM conversations
* Ping indicators
* Reactions

## How to build
This fork bundles the whole build toolchain (JDK 8, ProGuard, and the J2ME stub API jars) directly in the repository, so cloning it is enough to build it - nothing else to download.

1. Install [Node.js](https://nodejs.org) (used to run the build scripts).
2. *(Optional, for editing the code)* Any Java-aware editor works here - the build never goes through an IDE, it's just `build.sh`/`build.bat` calling `javac` directly, so nothing depends on your editor choice. This repo's `.vscode/` config is set up for [VS Code](https://code.visualstudio.com/) (install the **Extension Pack for Java** extension) since that's what's easiest to preconfigure, but Eclipse, IntelliJ IDEA, or NetBeans work just as well - just add the jars in `sdk/lib` (plus `sdk/proguard.jar`) as external/referenced libraries in whichever one you use, for completion against the J2ME APIs.
3. Run `build.sh` (Linux) or `build.bat` (Windows). The first run installs a couple of small npm packages, converts the translation files, then compiles every target listed in `build.json` into `bin/` (a `.jar` + `.jad` per target).

### Testing builds
[KEmulator nnmod](https://nnproject.cc/kem/) is a Java ME emulator, useful for quick testing without a real device. It's bundled in this repo too, at `tools/kemnnx64` (same reasoning as the build toolchain - it's a niche community tool, and niche download links have a habit of going dead).
- Double-click `run_kemulator.bat` at the repo root for the quickest option - it launches `bin\discord_s60v2.jar` in KEmulator. Edit the jar name inside it once you have your own target built.
- Or use the VS Code tasks (`Terminal > Run Task > Run Discord J2ME for ...`) - these already point at the bundled `tools/kemnnx64` via `${workspaceFolder}`, nothing to configure.
- Or run it directly yourself, e.g. `tools\kemnnx64\KEmulator_Console.bat bin\discord_s60v2.jar`.
- Or launch/debug it from another IDE instead - KEmulator supports the old UEI (Unified Emulator Interface) standard, so it can be wired up as a run target from Eclipse (with the MTJ plugin) or NetBeans, and it also ships some bundled IntelliJ IDEA integration. None of this is VS Code-specific.

The bundled copy already has a `640x200 (Nokia 9300/9500 - Series 80)` device preset added (see `tools/kemnnx64/presets_custom.xml`), set as the default - useful context if you're also targeting Series 80 devices.

### Updating the bundled toolchain
If you ever need to bump a version, here's where the bundled files originally came from: [Temurin OpenJDK 8](https://adoptium.net/temurin/releases/?version=8&package=jdk) (extract into `sdk/`, keep the `sdk/jdk8u...` folder name pattern), [ProGuard](https://github.com/Guardsquare/proguard/releases/latest) (`lib/proguard.jar` from the release archive, into `sdk/proguard.jar`), the stub API jars - [midpapi20](https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/midpapi20.jar), [cldcapi10](https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/cldcapi10.jar), [cldcapi11](https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/cldcapi11.jar), [jsr75](https://github.com/vipaoL/j2me-build-tools/raw/c1598b6916f2ba2ad5be1c0accd1ed2a54c156f3/WTK2.5.2/lib/jsr75.jar), [jsr82](https://github.com/vipaoL/j2me-build-tools/raw/e48bfaa97600f4aea8e5e1fff8af769755e2d1e9/lib/jsr82.jar), [javapiglerapi](https://nnp.nnchan.ru/pna/lib/javapiglerapi.jar), and [nokiaui](https://github.com/vipaoL/j2me-build-tools/raw/refs/heads/master/lib/nokiaui.jar) - into `sdk/lib`, and [KEmulator nnmod](https://github.com/shinovon/KEmulator/releases) (the `kemnnx64` Windows x64 build) - extract into `tools/kemnnx64`. The two `.bat` launchers in that folder auto-detect the bundled JDK (same `dir /b sdk\jdk*` trick `build.bat` uses) instead of relying on a system-wide Java install, so they don't need editing when the JDK version changes - only `.vscode/settings.json`'s `java.jdt.ls.java.home` has the JDK folder name hardcoded and needs a manual bump.

## Thanks
* [@uwmpr](https://github.com/uwmpr) for formerly hosting the default proxy server
* [@WunderWungiel](https://github.com/WunderWungiel) for formerly hosting the CDN proxy
* [@shinovon](https://github.com/shinovon) for their Java ME [JSON library](https://github.com/shinovon/NNJSON)
* [@AeroPurple](https://github.com/AeroPurple) for composing the default notification sound
* Language translation contributors (see About screen in the app)

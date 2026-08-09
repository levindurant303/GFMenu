# Changelog

## R1.1.5 - 2026-08-09

### Changed

- Switched the compile dependency from Spigot API to the standalone Bukkit API.
- Removed all compile-time dependencies on Spigot and Paper APIs while retaining runtime compatibility with both.
- Kept the runtime API baseline at Minecraft 1.21 and standardized builds on Java 21.

## R1.1.3-hotfix - 2026-08-08

### Added

- Added Bukkit and Spigot 1.21+ support while retaining Paper compatibility in the same plugin JAR.

### Changed

- Replaced the Paper API build dependency with the minimum `spigot-api:1.21` baseline.
- Replaced Paper-only plugin metadata and command map calls with Bukkit-compatible access paths.
- Release builds now use JDK 25 while emitting Java 21-compatible bytecode.
- Lowered `plugin.yml` API compatibility from `26.1` to `1.21`.

## R1.1.2 - 2026-08-04

### Changed
- Updated the server release version 26.1+
- Updated the plugin release version to 1.1.2.
- Maven now compiles with Java 25 `release` semantics.

## R1.1.1 - 2026-08-04

### Fixed

- Fixed dynamic command removal on Paper 1.21.11 throwing `UnsupportedOperationException` during `/gfm reload`.
- Fixed reload failures still reporting a successful reload.
- Fixed `/gfm open` and `/gfm edit` menu names containing spaces or parentheses.
- Fixed the visual editor opening an invalid writable book on Paper 1.21.11.
- Fixed TrMenu scalar and structured catcher actions being parsed incorrectly.
- Added replacement support for TrMenu `%trmenu_meta_input-<id>%` catcher placeholders.

### Changed

- Saving an edited menu now reparses and replaces only that menu instead of synchronously reloading every menu and dynamic command.
- Dynamic commands are tracked by their owning menu, so editing one menu does not unregister commands belonging to other menus.
- Plugin startup now reads the version from `plugin.yml` instead of using a hard-coded version string.
- Maven now compiles with Java 21 `release` semantics.
- Removed unused legacy action implementations, layout parsers, validation classes, and duplicate resources.
- Removed generated `target/` files from version control and added repository ignore rules.

### Validation

- Added regression coverage for TrMenu catcher parsing and catcher placeholder sessions.
- Added regression coverage for menu-specific dynamic command ownership.
- Verified with `mvn clean package`: 3 tests, 0 failures, 0 errors.

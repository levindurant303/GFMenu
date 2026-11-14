# Changelog

## R1.3.0 - 2026-11-14

### Added

- Added `/gfmenu backup` to create timestamped ZIP backups of menu YAML files without blocking the server thread.
- Added `/gfmenu validate [menu]` to report missing TrMenu layout icons, invalid DeluxeMenus slots, and unknown materials before editing.
- Expanded the writable-book editor with shift-left, shift-right, middle, and drop action books; added readable TrMenu-style bracket actions.

### Fixed

- Repeated TrMenu layout symbols now render one configured icon at every matching slot and route clicks back to the same item.
- Added static TrMenu `slots` overrides, slot ranges, and inventory-size expansion for manually positioned icons.
- Preserved `Layout` versus `layout` casing while editing and retained untouched/unsupported action groups when saving visual changes.
- Routed shift, middle, drop, number-key, offhand, and double-click actions to their matching TrMenu action groups.
- Applied the configured default centering before calculating TrMenu icon positions.
- Marked R1.2.5 as withdrawn and superseded; use R1.3.0 instead.

### Compatibility note

- R1.3.0 supports static TrMenu layouts, icons, slots, common click actions, and standard action prefixes. Kether scripts, JavaScript-driven/dynamic slots, and conditional icon switching still require TrMenu itself.

## R1.2.5 - 2026-10-25 [WITHDRAWN]

> Do not use this release; it is superseded by R1.3.0.

### Added

- Added `/gfmenu checkin [claim|status]` to claim daily rewards and view today's status, streak, total check-ins, and last check-in date.
- Added `/gfmenu update` for administrators to manually check for a new release and receive the result in chat or console.
- Added permission `gfmenu.checkin` (enabled by default) and bilingual help/status messages.

### Changed

- Update checks now track releases published by the main `levindurant303/GFMenu` repository instead of the Trinity fork.
- Coalesced overlapping automatic and manual update checks so they do not issue duplicate network requests.
- This release is withdrawn; use `R1.3.0`.

## R1.2.0 - 2026-10-25

### Added

- Added `/gfmenu info <menu>` diagnostics for format, inventory size, item count, permission, and bound commands.
- Added `/gfmenu close` and console/admin support for `/gfmenu open <menu> <player>`.
- Added `%player_uuid%`, `%world%`, `%server%`, and `%online%` action placeholders.
- Added namespaced Minecraft sound support such as `minecraft:block_note_block_harp-1-1`.
- Added daily check-in rewards with configurable streak bonuses.
- Added localized startup/shutdown banners and clear language-switch help with tab completion.
- Added an asynchronous latest-release check, configurable with `update-check`.

### Fixed

- Synchronized the plugin descriptor and documentation version with the 1.2.0 build.
- Added a warning when menu files collide after case-insensitive name normalization.
- Run configured cancel actions when book input times out.
- Fixed menu-editor book input so books open directly and save when the player clicks Done.
- Preserved line breaks consistently when processing multi-page book input.

## R1.1.5-hotfix - 2026-08-09

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

- Updated the server release version 26.1+.
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

# Changelog

## R1.1.4 - 2026-08-14

### Added

- Added `/gfmenu info <menu>` diagnostics for format, inventory size, item count, permission, and bound commands.
- Added `/gfmenu close` and console/admin support for `/gfmenu open <menu> <player>`.
- Added `%player_uuid%`, `%world%`, `%server%`, and `%online%` action placeholders.
- Added namespaced Minecraft sound support such as `minecraft:block_note_block_harp-1-1`.

### Fixed

- Synchronized the plugin descriptor and documentation version with the 1.1.4 build.
- Added a warning when menu files collide after case-insensitive name normalization.

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

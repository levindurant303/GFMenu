# GFMenu R1.3.0

[![SpigotMC](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/gfmenu.137753/)

[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/gfmenu)

[![GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg)](https://github.com/levindurant303/GFMenu)

[English](README.md) | [Chinese](README.zh-CN.md)

GFMenu is a GUI menu plugin for Bukkit, Spigot, and Paper 1.21+ servers. It reads TrMenu-style and DeluxeMenus-style YAML, provides an in-game menu editor with writable-book input, and includes practical administration commands.

> R1.2.5 has been withdrawn. Use R1.3.0 instead.

## Features

- Load static TrMenu-style and DeluxeMenus-style menus.
- Edit menu titles, items, lore, permissions, and click actions in game.
- Edit text and actions in writable books instead of typing material IDs or YAML by hand.
- Back up menu YAML files and validate menu configuration with administrator commands.
- Switch player-facing messages between Simplified Chinese and English.
- Claim daily check-in rewards and inspect check-in status.

## Compatibility

GFMenu supports static TrMenu layouts and icons, repeated layout symbols, `Icons.<key>.slots` values and ranges, common click groups, and standard action prefixes. DeluxeMenus-style menu titles, sizes, items, and slots are also supported.

Kether scripts, JavaScript-driven or otherwise dynamic slots, and conditional icon switching are not evaluated by GFMenu. These advanced features still require TrMenu.

## Installation

1. Use Java 21 and a Bukkit, Spigot, or Paper 1.21+ server.
2. Place `GFMenu-R1.3.0.jar` in the server's `plugins` directory.
3. Start or restart the server. Menu files are loaded from `plugins/GFMenu/menus/`.
4. Grant administrators the `gfmenu.admin` permission (default: operators).

## Commands

| Command | Description |
| --- | --- |
| `/gfmenu help` | Show available commands. |
| `/gfmenu list` | List loaded menus. |
| `/gfmenu info <menu>` | Show menu format, size, item count, and permission. |
| `/gfmenu open <menu> <player>` | Open a menu for an online player. |
| `/gfmenu close` | Close your current menu. |
| `/gfmenu lang [zh_CN|en_US]` | View or change the server-wide message language. |
| `/gfmenu checkin [status]` | Claim today's reward or view check-in status. |
| `/gfmenu update` | Check for a release from the main GitHub repository. |
| `/gfmenu backup` | Create a timestamped ZIP backup of menu YAML files. |
| `/gfmenu validate [menu]` | Check menus for common configuration issues. |
| `/gfmenu reload` | Reload menu and plugin configuration. |
| `/gfmenu edit <menu>` | Open the in-game menu editor. |

Menu editing, reload, backup, validation, and update commands require `gfmenu.admin` unless your server grants access another way. `/gfmenu info` uses `gfmenu.info` (default: operators). Check-in uses `gfmenu.checkin` (default: enabled for everyone).

`/gfmenu backup` saves archives under `plugins/GFMenu/backups/`. `/gfmenu validate [menu]` reports missing TrMenu layout icons, invalid DeluxeMenus slots, and unknown materials.

## Language and configuration

The default message language is `zh_CN`. Run `/gfmenu lang en_US` or `/gfmenu lang zh_CN` to change it; the selection applies server-wide and is saved in `config.yml`. Short forms such as `en` and `zh` are also accepted.

Update checks run asynchronously against releases in `levindurant303/GFMenu`. Set `update-check: false` in `config.yml` to disable automatic startup checks; administrators can still run `/gfmenu update` manually.

Daily and streak rewards are configured in `plugins/GFMenu/checkin.yml`. Language resources are available in `plugins/GFMenu/languages/` and can override bundled messages.

## Editing menus in game

Run `/gfmenu edit <menu>` and click an existing item, or place an item from your inventory into an empty menu slot. The item editor provides books for the name, lore, and click actions. Write normal text in a book and click **Done** to apply it. Use **Save and return** to save the item and go back to the menu editor.

The editor supports left, right, any-click, shift-left, shift-right, middle, and drop actions. You can also edit fields from the command line:

```text
/gfmenu edit <menu> title <text>
/gfmenu edit <menu> permission <node|none>
/gfmenu edit <menu> size <9-54>
/gfmenu edit <menu> item <slot> <material> [amount] [name]
/gfmenu edit <menu> remove <slot>
/gfmenu edit <menu> lore <slot> <set|add|remove|clear> [text|index]
/gfmenu edit <menu> action <slot> <click-type> <set|add|remove|clear> [action|index]
```

`click-type` accepts `left`, `right`, `all`, `shift_left`, `shift_right`, `middle`, `drop`, and other lowercase Bukkit click names. The `size` command applies to DeluxeMenus-style menus; TrMenu inventory size follows its layout rows and highest configured static slot.

## Menu and action examples

Example TrMenu-style icon:

```yaml
layout:
  - "   A   "
Icons:
  A:
    display:
      material: DIAMOND
      name: "&bRewards"
    actions:
      left:
        - "command: warp rewards"
      right:
        - "console: give %player% diamond 1"
        - "sound: ENTITY_PLAYER_LEVELUP-1-1"
```

Supported action prefixes include `command:`, `console:`, `tell:`, `message:`, `chat:`, `menu:`, `sound:`, `close`, `catcher:`, and `book:`. TrMenu-style `[player]`, `[console]`, `[message]`, `[open]`, and `[close]` prefixes are also recognized. `op:` is treated as `console:`; GFMenu does not grant temporary operator status to players.

Actions can use `%player%`, `%uuid%`, `%player_uuid%`, `%world%`, `%server%`, and `%online%`. Sound names may include a namespace and volume/pitch, for example `minecraft:entity_player_levelup-1-1`.

## Book input action

Use `book:` to let a player submit text in a writable book. After confirmation, all pages are available as `%book_input%`, and the configured `end=` actions run.

```yaml
actions:
  left:
    - "book:feedback|prompt=&eWrite your feedback in the book.|end=console: feedback save %player% %book_input%|cancel=tell: &cFeedback cancelled."
```

Only place `%book_input%` in an argument handled by a trusted plugin. Do not use it as a complete console command, such as `console: %book_input%`, because that would let a player choose the command executed by the server.

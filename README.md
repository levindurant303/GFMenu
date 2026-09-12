<div align="center">

<h1 align="center">
  <img src="https://cdn.modrinth.com/data/xnqZ2y1v/a7b5d9b70909c6f8a537e62c5c5e23f32624f0ec_96.webp" alt="GFMenu" width="27" height="27" style="vertical-align: middle; margin-right: 12px;">
  GFMenu
</h1>

[![SpigotMC](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/gfmenu.137753/)
[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/gfmenu)

**GFMenu** is a simple and easy-to-use, Bukkit-based GUI menu plugin for Minecraft 1.21+.

Compatible with CraftBukkit, Spigot, Paper, and other Bukkit API server implementations. It provides in-game visual editing, writable book input, player and console command execution, and Chinese/English language switching for interactive server menus.

</div>

---

## Language / 语言

The server-wide player-facing language can be switched at runtime by an administrator:

```text
/gfmenu lang zh_CN
/gfmenu lang en_US
```

`config.yml` defaults to `zh_CN`. The selected language changes command feedback, permission errors, editor titles, the book editor, and input-session messages. Existing language files in the plugin data folder can override individual messages; new bundled keys remain available as defaults after an update.

With `zh_CN`, the item settings page contains separate books for `名称`, `描述`, `左键命令`, `右键命令`, and `通用命令`. The selected item decides its material and amount, so a player does not need to type Minecraft material IDs. Chinese action aliases including `控制台命令:`, `玩家命令:`, `消息:`, `打开菜单:`, `书本:`, and `关闭` are accepted as well.

## Editing menus

Administrators with `gfmenu.admin` can edit a loaded menu in game.

```text
/gfmenu edit <menu>
```

This opens an inventory editor. Click an existing menu item, or pick up an item from your own inventory and click an empty menu slot. A second inventory opens with separate books for the name, lore, left-click commands, right-click commands, and any-click commands. Click a book, write normal text in it, then click **Done** to return to the item settings page.

The selected item decides its material, amount, glow, and skull owner. Click the emerald **Save and return** button to write that item and go back to the menu editor. Repeat for other slots. Closing the main menu editor with Escape closes the editor after the saved changes have been written.

In a command book, write one command per line. A normal line such as `/warp rewards` runs as the clicking player. Use `console: give %player% diamond 1` only when the server must run the command.

Command editing is available for fields that cannot be changed through an inventory:

```text
/gfmenu edit <menu> title <text>
/gfmenu edit <menu> permission <node|none>
/gfmenu edit <menu> size <9-54>
/gfmenu edit <menu> item <slot> <material> [amount] [name]
/gfmenu edit <menu> remove <slot>
/gfmenu edit <menu> lore <slot> <set|add|remove|clear> [text|index]
/gfmenu edit <menu> action <slot> <left|right|all> <set|add|remove|clear> [action|index]
```

`size` is for DeluxeMenus-style menus. TrMenu menu size is determined by its `layout` rows.

## Item actions

Actions are configured per click type. `command:` runs as the clicking player; `console:` runs from the server console.

```yaml
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

Supported actions: `command:`, `console:`, `tell:`, `message:`, `chat:`, `menu:`, `sound:`, `close`, `catcher:`, and `book:`. `op:` is treated as `console:` and never grants temporary operator status to a player.

## Book input action

Use `book:` to open a writable book. Once the player confirms the edit, all pages are placed in `%book_input%` and the configured `end=` actions execute.

```yaml
actions:
  left:
    - "book:feedback|prompt=&eWrite your feedback in the book.|end=console: feedback save %player% %book_input%|cancel=tell: &cFeedback cancelled."
```

The same action can be added from the editor command:

```text
/gfmenu edit <menu> action <slot> left add book:feedback|prompt=&eWrite your feedback.|end=console: feedback save %player% %book_input%
```

Keep `%book_input%` in an argument position of a command owned by a trusted plugin. Do not configure a raw `console: %book_input%` action, because it would allow the player to decide the entire console command.

# ⚠️ 转载/搬运政策 | Repost Policy | Политика перепубликации

**中文**：关于转载/搬运：欢迎中文社区搬运本插件，但请务必保留原作者署名、附上原帖链接，并保持免费下载。如有违反，将发起 DMCA 投诉。

**English**: **Repost Policy**: The Chinese community is welcome to redistribute this plugin, provided that you retain the original author's credit, include a link to the original post, and keep it free to download. Violations will result in a DMCA takedown notice.

**Русский**: **О перепубликации**: Китайское сообщество может свободно распространять этот плагин при условии указания оригинального автора, наличия ссылки на оригинальный пост и сохранения бесплатного доступа. При нарушении этих условий будет подана жалоба в рамках DMCA.

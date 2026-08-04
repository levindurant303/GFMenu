# GFMenu 1.1.2

GFMenu is an easy-to-use Minecraft Paper 1.21+/26.1+ GUI menu plugin with TrMenu- and DeluxeMenus-style YAML support. It provides in-game visual editing, writable book input, player and console commands, and Chinese/English language switching for interactive server menus

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

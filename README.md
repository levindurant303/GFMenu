# GFMenu R1.3.0

GFMenu is an easy-to-use Minecraft Bukkit, Spigot, and Paper 1.21+ GUI menu plugin with TrMenu- and DeluxeMenus-style YAML support. It provides in-game visual editing, writable book input, player and console commands, and Chinese/English language switching for interactive server menus.

## Language / 语言

The server-wide player-facing language can be switched at runtime by an administrator:

```text
/gfmenu lang zh_CN
/gfmenu lang en_US
```

Running `/gfmenu lang` shows the current selection and accepted values. Language names are case-insensitive; `zh`/`zh-CN` and `en`/`en-US` are also accepted. The selection is server-wide and saved in `config.yml`.

常用管理命令：

```text
/gfmenu info <menu>                 # 查看格式、大小、物品数和权限
/gfmenu close                       # 关闭当前菜单
/gfmenu open <menu> <player>        # 管理员/控制台为在线玩家打开菜单
/gfmenu checkin [status]            # 签到，或查看连续/累计签到状态
/gfmenu update                      # 管理员手动触发主仓版本检查
/gfmenu backup                      # 将菜单 YAML 打包备份到 backups/
/gfmenu validate [menu]             # 检查菜单布局、图标和材质配置
```

`R1.2.5` 已作废，请改用 `R1.3.0`。

动作中的 `%player%`、`%uuid%`、`%player_uuid%`、`%world%`、`%server%` 和 `%online%`
会在执行时替换为当前玩家和服务器信息。`sound:` 也接受 `minecraft:entity_player_levelup-1-1`
这样的命名空间格式。

`config.yml` defaults to `zh_CN`. The selected language changes command feedback, permission errors, editor titles, the book editor, and input-session messages. Existing language files in the plugin data folder can override individual messages; new bundled keys remain available as defaults after an update.

Update checks run asynchronously against the latest `levindurant303/GFMenu` main-repository release and report in the configured language. Administrators can run `/gfmenu update` for an on-demand check even when automatic checks are disabled. Set `update-check: false` in `config.yml` to disable startup checks.

With `zh_CN`, the item settings page contains separate books for `名称`, `描述`, `左键命令`, `右键命令`, and `通用命令`. The selected item decides its material and amount, so a player does not need to type Minecraft material IDs. Chinese action aliases including `控制台命令:`, `玩家命令:`, `消息:`, `打开菜单:`, `书本:`, and `关闭` are accepted as well.

The TrMenu parser supports repeated layout symbols, static `Icons.<key>.slots` values and ranges, standard click groups (including shift-click), and common action prefixes. Kether scripts, JavaScript-driven slots, and conditional icon switching are not evaluated by GFMenu; those advanced TrMenu features still require TrMenu.

`/gfmenu backup` writes a timestamped ZIP of menu YAML files to `plugins/GFMenu/backups/`. `/gfmenu validate [menu]` checks loaded menu files for missing TrMenu layout icons, invalid DeluxeMenus slots, and unknown materials before editing or deployment.

## Editing menus

Administrators with `gfmenu.admin` can edit a loaded menu in game.

```text
/gfmenu edit <menu>
```

This opens an inventory editor. Click an existing menu item, or pick up an item from your own inventory and click an empty menu slot. A second inventory opens with separate books for the name, lore, left-click commands, right-click commands, and any-click commands. Click a book, write normal text in it, then click **Done** to return to the item settings page.

The selected item decides its material, amount, glow, and skull owner. Click the emerald **Save and return** button to write that item and go back to the menu editor. Repeat for other slots. Closing the main menu editor with Escape closes the editor after the saved changes have been written.

In an action book, write one action per line. `/warp rewards` runs as the clicking player; `console: give %player% diamond 1` runs from console. `message:`, `menu:`, `sound: ENTITY_PLAYER_LEVELUP-1-1`, and `close` are also accepted. TrMenu-style `[player]`, `[console]`, `[message]`, `[open]`, and `[close]` prefixes are normalized when loaded or edited.

Command editing is available for fields that cannot be changed through an inventory:

```text
/gfmenu edit <menu> title <text>
/gfmenu edit <menu> permission <node|none>
/gfmenu edit <menu> size <9-54>
/gfmenu edit <menu> item <slot> <material> [amount] [name]
/gfmenu edit <menu> remove <slot>
/gfmenu edit <menu> lore <slot> <set|add|remove|clear> [text|index]
/gfmenu edit <menu> action <slot> <click-type> <set|add|remove|clear> [action|index]
```

`click-type` accepts `left`, `right`, `all`, `shift_left`, `shift_right`, `middle`, `drop`, and other lowercase Bukkit click names. `size` is for DeluxeMenus-style menus; TrMenu inventory size follows its layout rows and highest configured static slot.

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

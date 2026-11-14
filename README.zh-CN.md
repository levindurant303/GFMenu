# GFMenu R1.3.0

[![SpigotMC](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/gfmenu.137753/)

[![Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/gfmenu)

[![GitHub](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg)](https://github.com/levindurant303/GFMenu)

[英文版](README.md) | 简体中文

GFMenu 是面向 Bukkit、Spigot 和 Paper 1.21 及以上服务器的图形菜单插件。它可以读取 TrMenu 风格和 DeluxeMenus 风格的 YAML 配置，提供带书本输入的游戏内菜单编辑器，并包含方便服主使用的管理命令。

> R1.2.5 已作废，请使用 R1.3.0。

## 功能特性

- 加载静态 TrMenu 风格和 DeluxeMenus 风格的菜单。
- 在游戏内编辑菜单标题、物品、描述、权限和点击动作。
- 使用可写书本编辑文字和动作，无需手动输入材质编号或 YAML。
- 通过管理命令备份菜单 YAML 文件并检查常见配置问题。
- 在简体中文和英文之间切换玩家看到的提示语言。
- 领取每日签到奖励并查询签到状态。

## 兼容范围

GFMenu 支持静态 TrMenu 布局和图标、重复布局字符、`Icons.<key>.slots` 槽位及范围、常用点击动作组和标准动作前缀。也支持 DeluxeMenus 风格的菜单标题、大小、物品和槽位配置。

GFMenu 不会解析 Kether 脚本、JavaScript 驱动或其他动态槽位，也不会执行条件图标切换。这些高级功能仍需由 TrMenu 本身处理。

## 安装

1. 服务器使用 Java 21，以及 Bukkit、Spigot 或 Paper 1.21 及以上版本。
2. 将 `GFMenu-R1.3.0.jar` 放入服务器的 `plugins` 目录。
3. 启动或重启服务器。菜单文件从 `plugins/GFMenu/menus/` 加载。
4. 为管理员授予 `gfmenu.admin` 权限（默认仅管理员组，即 OP）。

## 命令

| 命令                             | 说明                         |
|--------------------------------|----------------------------|
| `/gfmenu help`                 | 显示可用命令。                    |
| `/gfmenu list`                 | 列出已加载的菜单。                  |
| `/gfmenu info <menu>`          | 查看菜单格式、大小、物品数量和权限。         |
| `/gfmenu open <menu> <player>` | 为在线玩家打开菜单。                 |
| `/gfmenu close`                | 关闭当前打开的菜单。                 |
| `/gfmenu lang [zh_CN/en_US]`   | 查看或切换全服提示语言。 |
| `/gfmenu checkin [status]`     | 领取今日奖励或查看签到状态。             |
| `/gfmenu update`               | 检查主 GitHub 仓库是否发布了新版本。     |
| `/gfmenu backup`               | 将菜单 YAML 文件打包成带时间戳的 ZIP 备份。 |
| `/gfmenu validate [menu]`      | 检查菜单中的常见配置问题。              |
| `/gfmenu reload`               | 重新加载菜单和插件配置。               |
| `/gfmenu edit <menu>`          | 打开游戏内菜单编辑器。                |

菜单编辑、重载、备份、验证和更新检查命令需要 `gfmenu.admin` 权限，除非服务器另行授权。`/gfmenu info` 使用 `gfmenu.info`（默认仅管理员组）。签到使用 `gfmenu.checkin`（默认所有玩家可用）。

`/gfmenu backup` 会将备份保存到 `plugins/GFMenu/backups/`。`/gfmenu validate [menu]` 会报告缺失的 TrMenu 布局图标、无效的 DeluxeMenus 槽位和未知材质。

## 语言与配置

默认提示语言为 `zh_CN`。使用 `/gfmenu lang en_US` 或 `/gfmenu lang zh_CN` 切换语言；设置对全服生效，并保存到 `config.yml`。也接受 `en`、`zh` 等简写。

更新检查会异步查询 `levindurant303/GFMenu` 仓库的 Release。将 `config.yml` 中的 `update-check` 设为 `false` 可关闭启动时自动检查；管理员仍可手动运行 `/gfmenu update`。

每日和连续签到奖励配置在 `plugins/GFMenu/checkin.yml`。语言资源位于 `plugins/GFMenu/languages/`，可覆盖插件自带的提示文本。

## 游戏内编辑菜单

运行 `/gfmenu edit <menu>`，点击现有物品，或将背包中的物品放入菜单空槽位。物品编辑界面提供名称、描述和点击动作书本。点击书本，写入普通文本后点击 **完成** 应用。点击 **保存并返回** 保存物品并回到菜单编辑界面。

编辑器支持左键、右键、任意点击、Shift+左键、Shift+右键、中键和丢弃键动作。也可以通过命令行编辑字段：

```text
/gfmenu edit <menu> title <text>
/gfmenu edit <menu> permission <node|none>
/gfmenu edit <menu> size <9-54>
/gfmenu edit <menu> item <slot> <material> [amount] [name]
/gfmenu edit <menu> remove <slot>
/gfmenu edit <menu> lore <slot> <set|add|remove|clear> [text|index]
/gfmenu edit <menu> action <slot> <click-type> <set|add|remove|clear> [action|index]
```

`click-type` 可使用 `left`、`right`、`all`、`shift_left`、`shift_right`、`middle`、`drop` 以及其他小写 Bukkit 点击类型名称。`size` 命令适用于 DeluxeMenus 风格菜单；TrMenu 菜单大小由布局行数和最高静态槽位决定。

## 菜单与动作示例

TrMenu 风格图标示例：

```yaml
layout:
  - "   A   "
Icons:
  A:
    display:
      material: DIAMOND
      name: "&b奖励"
    actions:
      left:
        - "command: warp rewards"
      right:
        - "console: give %player% diamond 1"
        - "sound: ENTITY_PLAYER_LEVELUP-1-1"
```

支持的动作前缀包括 `command:`、`console:`、`tell:`、`message:`、`chat:`、`menu:`、`sound:`、`close`、`catcher:` 和 `book:`。也识别 TrMenu 风格的 `[player]`、`[console]`、`[message]`、`[open]` 和 `[close]` 前缀。`op:` 会按 `console:` 处理；GFMenu 不会临时授予玩家管理员权限。

动作中可使用 `%player%`、`%uuid%`、`%player_uuid%`、`%world%`、`%server%` 和 `%online%`。音效名称可包含命名空间、音量和音调，例如 `minecraft:entity_player_levelup-1-1`。

## 书本输入动作

使用 `book:` 让玩家通过可写书本提交文本。确认后，所有页面内容会放入 `%book_input%`，随后执行配置的 `end=` 动作。

```yaml
actions:
  left:
    - "book:feedback|prompt=&e请在书本中填写反馈。|end=console: feedback save %player% %book_input%|cancel=tell: &c已取消反馈。"
```

只应将 `%book_input%` 放在可信插件处理的命令参数中。不要将它作为完整控制台命令，例如 `console: %book_input%`，否则玩家可以自行决定服务器执行的命令。

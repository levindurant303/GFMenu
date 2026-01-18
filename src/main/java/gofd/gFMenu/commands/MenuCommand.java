package gofd.gFMenu.commands;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

public class MenuCommand implements CommandExecutor {

    private final GFMenu plugin;
    private final MenuManager menuManager;

    public MenuCommand(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_reload_no_perm"));
                    return true;
                }
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getLanguageManager().getMessage("reload_success"));
                return true;

            case "open":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("player_only"));
                    return true;
                }
                if (!sender.hasPermission("gfmenu.open")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_usage", "/gfmenu open <菜单名>"));
                    return true;
                }
                String menuName = args[1];
                menuManager.openMenu((Player)sender, menuName);
                return true;

            case "list":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
                    return true;
                }

                Map<String, LayoutMenuData> allMenus = menuManager.getAllMenuData();
                if (allMenus.isEmpty()) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_list_empty"));
                    return true;
                }

                StringBuilder sb = new StringBuilder();
                sb.append(plugin.getLanguageManager().getMessage("cmd_list_header")).append("\n");

                for (String menu : allMenus.keySet()) {
                    sb.append(plugin.getLanguageManager().getMessage("cmd_list_item", menu)).append("\n");
                }

                sb.append(plugin.getLanguageManager().getMessage("cmd_list_footer", allMenus.size()));
                sender.sendMessage(sb.toString());
                return true;

            case "debug":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
                    return true;
                }

                sender.sendMessage("§6=== GFMenu 调试信息 ===");
                sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_debug_info", menuManager.getLoadedMenuCount()));
                sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_debug_commands", menuManager.getCommandRegistry().getRegisteredCommandCount()));
                return true;

            case "config":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_usage", "/gfmenu config <status|reload>"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("status")) {
                    sender.sendMessage(plugin.getGlobalConfigStatus());
                } else if (args[1].equalsIgnoreCase("reload")) {
                    plugin.reloadPlugin();
                    sender.sendMessage(plugin.getLanguageManager().getMessage("config_loaded"));
                }
                return true;

            case "lang":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("no_permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§7当前语言: §f" + plugin.getLanguageManager().getCurrentLanguage());
                    sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_usage", "/gfmenu lang <zh_CN|en_US>"));
                    return true;
                }

                String lang = args[1].toLowerCase();
                if (lang.equals("zh_cn") || lang.equals("en_us")) {
                    plugin.getLanguageManager().setLanguage(lang);
                    sender.sendMessage("§a语言已切换为: §f" + lang);
                } else {
                    sender.sendMessage("§c不支持的语言！支持: zh_CN, en_US");
                }
                return true;

            default:
                // 尝试打开菜单（作为玩家命令）
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    menuManager.openMenu(player, subCommand);
                } else {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("player_only"));
                }
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_header"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu reload", "重载插件"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu open <菜单>", "打开菜单"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu list", "菜单列表"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu debug", "调试信息"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu config <status|reload>", "配置管理"));
        sender.sendMessage(plugin.getLanguageManager().getMessage("cmd_help_item", "/gfmenu lang <语言>", "切换语言"));
    }
}
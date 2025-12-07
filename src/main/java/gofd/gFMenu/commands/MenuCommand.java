package gofd.gFMenu.commands;

import gofd.gFMenu.menu.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {

    private final MenuManager menuManager;

    public MenuCommand(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /gfmenu open <菜单名>");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c只有玩家可以使用此命令！");
                    return true;
                }
                menuManager.openMenu((Player) sender, args[1]);
                break;

            case "reload":
                if (!sender.hasPermission("gfmenu.admin")) {
                    sender.sendMessage("§c你没有权限执行此命令！");
                    return true;
                }
                menuManager.reloadMenus();
                sender.sendMessage("§a菜单已重新加载！");
                break;

            case "list":
                sender.sendMessage("§6已加载的菜单:");
                for (String menuName : menuManager.getLoadedMenus().keySet()) {
                    sender.sendMessage("§7- §f" + menuName);
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== GFMenu 帮助 ===");
        sender.sendMessage("§7/gfmenu open <菜单名> §f- 打开指定菜单");
        sender.sendMessage("§7/gfmenu list §f- 列出所有已加载菜单");
        if (sender.hasPermission("gfmenu.admin")) {
            sender.sendMessage("§7/gfmenu reload §f- 重新加载所有菜单");
        }
        sender.sendMessage("§7/<菜单命令> §f- 直接打开对应菜单");
    }
}

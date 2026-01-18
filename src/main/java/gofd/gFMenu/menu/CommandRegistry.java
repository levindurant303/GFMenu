package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

public class CommandRegistry {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final Map<String, String> registeredCommands;

    public CommandRegistry(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.registeredCommands = new HashMap<>();
    }

    /**
     * 为菜单自动注册命令
     */
    public void registerMenuCommands(LayoutMenuData menuData) {
        String menuName = menuData.getName();
        List<String> commands = menuData.getCommands();

        if (commands == null || commands.isEmpty()) {
            return;
        }

        for (String command : commands) {
            if (command == null || command.trim().isEmpty()) {
                continue;
            }

            String cmd = command.trim().toLowerCase();

            // 跳过已注册的命令
            if (registeredCommands.containsKey(cmd)) {
                continue;
            }

            // 注册命令
            if (registerCommand(cmd, menuName)) {
                registeredCommands.put(cmd, menuName);
            }
        }
    }

    /**
     * 动态注册命令（简化版）
     */
    private boolean registerCommand(String command, String menuName) {
        try {
            // 获取CommandMap
            CommandMap commandMap = getCommandMap();
            if (commandMap == null) {
                return false;
            }

            // 检查命令是否已存在（通过Bukkit的命令管理器）
            Command existing = commandMap.getCommand(command);
            if (existing != null) {
                // 检查是否是我们自己的命令
                boolean isOurCommand = false;
                try {
                    // 尝试通过反射判断
                    if (existing instanceof SimpleMenuCommand) {
                        isOurCommand = true;
                    }
                } catch (Exception e) {
                    // 忽略
                }

                if (!isOurCommand) {
                    return false;
                }
            }

            // 创建并注册命令
            SimpleMenuCommand menuCmd = new SimpleMenuCommand(command, menuManager, menuName);
            menuCmd.setDescription("打开 " + menuName + " 菜单");
            menuCmd.setUsage("/" + command);

            // 设置权限
            LayoutMenuData menu = menuManager.getMenu(menuName);
            if (menu != null && menu.getPermission() != null) {
                menuCmd.setPermission(menu.getPermission());
            }

            // 注册命令
            commandMap.register(plugin.getName().toLowerCase(), menuCmd);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取CommandMap
     */
    private CommandMap getCommandMap() {
        try {
            // 方法1：通过Bukkit获取
            CommandMap commandMap = Bukkit.getCommandMap();
            if (commandMap != null) {
                return commandMap;
            }

            // 方法2：通过反射获取
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清理已注册的命令
     */
    public void unregisterAllCommands() {
        try {
            CommandMap commandMap = getCommandMap();
            if (commandMap == null) return;

            // 获取已知命令
            Map<String, Command> knownCommands;
            try {
                Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            } catch (NoSuchFieldException e) {
                knownCommands = commandMap.getKnownCommands();
            }

            if (knownCommands == null) return;

            // 收集要移除的命令
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
                if (entry.getValue() instanceof SimpleMenuCommand) {
                    toRemove.add(entry.getKey());
                }
            }

            // 移除命令
            for (String cmd : toRemove) {
                knownCommands.remove(cmd);
            }

            registeredCommands.clear();

        } catch (Exception e) {
            // 静默处理
        }
    }

    /**
     * 获取已注册的命令数量
     */
    public int getRegisteredCommandCount() {
        return registeredCommands.size();
    }

    /**
     * 获取已注册的命令列表（调试用）
     */
    public String getRegisteredCommandsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 已注册命令列表 ===\n");

        if (registeredCommands.isEmpty()) {
            sb.append("没有已注册的命令\n");
        } else {
            for (Map.Entry<String, String> entry : registeredCommands.entrySet()) {
                sb.append(String.format("§7/%s → §f%s\n", entry.getKey(), entry.getValue()));
            }
        }

        sb.append("总计: ").append(registeredCommands.size()).append(" 个命令\n");
        sb.append("======================");
        return sb.toString();
    }

    /**
     * 简化的菜单命令类
     */
    public static class SimpleMenuCommand extends Command {
        private final MenuManager menuManager;
        private final String menuName;

        public SimpleMenuCommand(String name, MenuManager menuManager, String menuName) {
            super(name);
            this.menuManager = menuManager;
            this.menuName = menuName;
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c只有玩家可以使用此命令。");
                return true;
            }

            Player player = (Player) sender;

            // 检查权限
            if (getPermission() != null && !player.hasPermission(getPermission())) {
                player.sendMessage("§c你没有权限使用此命令。");
                return true;
            }

            // 打开菜单
            menuManager.openMenu(player, menuName);
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return new ArrayList<>();
        }
    }
}
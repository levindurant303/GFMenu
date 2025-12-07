package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
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
                plugin.getLogger().warning("命令 /" + cmd + " 已被菜单 '" +
                        registeredCommands.get(cmd) + "' 注册，跳过");
                continue;
            }

            // 注册命令
            if (registerCommand(cmd, menuName)) {
                registeredCommands.put(cmd, menuName);
                plugin.getLogger().info("已注册命令: /" + cmd + " → 菜单 '" + menuName + "'");
            }
        }
    }

    /**
     * 动态注册命令
     */
    private boolean registerCommand(String command, String menuName) {
        try {
            // 获取命令映射
            CommandMap commandMap = getCommandMap();
            if (commandMap == null) {
                plugin.getLogger().warning("无法获取CommandMap");
                return false;
            }

            // 创建自定义命令
            SimpleCommand simpleCmd = new SimpleCommand(command, menuManager, menuName);

            // 注册到命令映射
            commandMap.register(plugin.getName().toLowerCase(), simpleCmd);

            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("注册命令失败: /" + command + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取CommandMap（兼容方法）
     */
    private CommandMap getCommandMap() {
        try {
            // 方法1：通过Bukkit获取（1.13+）
            if (Bukkit.getServer() != null) {
                return Bukkit.getCommandMap();
            }
        } catch (NoSuchMethodError e) {
            // 方法2：通过反射获取（1.12及以下）
            try {
                Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                return (CommandMap) commandMapField.get(Bukkit.getServer());
            } catch (Exception ex) {
                plugin.getLogger().warning("无法通过反射获取CommandMap");
            }
        }
        return null;
    }

    /**
     * 清理已注册的命令
     */
    public void unregisterAllCommands() {
        try {
            CommandMap commandMap = getCommandMap();
            if (commandMap != null) {
                // 获取已知命令
                Map<String, Command> knownCommands;
                try {
                    Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                    knownCommandsField.setAccessible(true);
                    knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
                } catch (Exception e) {
                    knownCommands = commandMap.getKnownCommands();
                }

                // 移除本插件注册的命令
                if (knownCommands != null) {
                    Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Command> entry = it.next();
                        Command cmd = entry.getValue();

                        if (cmd instanceof SimpleCommand) {
                            it.remove();
                        }
                    }
                }
            }

            registeredCommands.clear();

        } catch (Exception e) {
            plugin.getLogger().warning("清理命令时出错: " + e.getMessage());
        }
    }

    /**
     * 获取已注册的命令数量
     */
    public int getRegisteredCommandCount() {
        return registeredCommands.size();
    }

    // 简化的命令类
    private static class SimpleCommand extends Command {
        private final MenuManager menuManager;
        private final String menuName;

        public SimpleCommand(String name, MenuManager menuManager, String menuName) {
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
            LayoutMenuData menu = menuManager.getMenu(menuName);

            if (menu == null) {
                player.sendMessage("§c菜单不存在或未加载。");
                return true;
            }

            // 权限检查
            if (menu.getPermission() != null && !player.hasPermission(menu.getPermission())) {
                player.sendMessage("§c你没有权限使用此命令。");
                return true;
            }

            menuManager.openMenu(player, menuName);
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            return new ArrayList<>();
        }
    }
}
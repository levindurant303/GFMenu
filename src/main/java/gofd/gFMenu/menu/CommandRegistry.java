package gofd.gFMenu.menu;

import gofd.gFMenu.GFMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Registers menu open commands and removes only commands owned by this registry. */
public final class CommandRegistry {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final OwnedCommandStore<Command> registeredCommands = new OwnedCommandStore<>();

    public CommandRegistry(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public void registerMenuCommands(LayoutMenuData menu) {
        for (String configuredCommand : menu.getCommands()) {
            String commandName = normalizeCommand(configuredCommand);
            if (commandName == null || registeredCommands.containsKey(commandName)) {
                continue;
            }
            Command command = registerCommand(commandName, menu);
            if (command != null) {
                registeredCommands.add(normalizeMenuName(menu.getName()), commandName, command);
            }
        }
    }

    private Command registerCommand(String commandName, LayoutMenuData menu) {
        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("Command map is unavailable; cannot register /" + commandName);
            return null;
        }
        Command existing = commandMap.getCommand(commandName);
        if (existing != null) {
            plugin.getLogger().warning("Skipping menu command /" + commandName + " because it is already registered.");
            return null;
        }

        MenuOpenCommand command = new MenuOpenCommand(commandName, plugin, menuManager, menu.getName());
        command.setDescription("Open menu " + menu.getName());
        command.setUsage("/" + commandName);
        if (menu.getPermission() != null && !menu.getPermission().isBlank()) {
            command.setPermission(menu.getPermission());
        }
        if (commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command)) {
            return command;
        }
        return null;
    }

    public void unregisterMenuCommands(String menuName) {
        unregisterCommands(registeredCommands.removeOwner(normalizeMenuName(menuName)));
    }

    public void unregisterAllCommands() {
        unregisterCommands(registeredCommands.removeAll());
    }

    private void unregisterCommands(Collection<Command> commands) {
        CommandMap commandMap = getCommandMap();
        Map<String, Command> knownCommands = getKnownCommands(commandMap);
        for (Command command : commands) {
            if (commandMap != null) {
                command.unregister(commandMap);
            }
            if (knownCommands != null) {
                List<String> commandKeys = knownCommands.entrySet().stream()
                        .filter(entry -> entry.getValue() == command)
                        .map(Map.Entry::getKey)
                        .toList();
                for (String commandKey : commandKeys) {
                    knownCommands.remove(commandKey, command);
                }
            }
        }
    }

    public int getRegisteredCommandCount() {
        return registeredCommands.size();
    }

    public String getRegisteredCommandsList() {
        if (registeredCommands.isEmpty()) {
            return "No dynamically registered menu commands.";
        }
        return String.join(", ", registeredCommands.keys());
    }

    private CommandMap getCommandMap() {
        try {
            return Bukkit.getCommandMap();
        } catch (NoSuchMethodError ignored) {
            try {
                Field field = findField(Bukkit.getServer().getClass(), "commandMap");
                if (field == null) {
                    return null;
                }
                field.setAccessible(true);
                return (CommandMap) field.get(Bukkit.getServer());
            } catch (ReflectiveOperationException exception) {
                plugin.getLogger().warning("Unable to access Bukkit command map: " + exception.getMessage());
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands(CommandMap commandMap) {
        if (commandMap == null) {
            return null;
        }
        try {
            Field field = findField(commandMap.getClass(), "knownCommands");
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object value = field.get(commandMap);
            return value instanceof Map<?, ?> ? (Map<String, Command>) value : null;
        } catch (IllegalAccessException exception) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static String normalizeCommand(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.matches("[a-z0-9_:-]+") ? normalized : null;
    }

    private static String normalizeMenuName(String menuName) {
        return menuName.toLowerCase(Locale.ROOT);
    }

    private static final class MenuOpenCommand extends Command {
        private final GFMenu plugin;
        private final MenuManager menuManager;
        private final String menuName;

        private MenuOpenCommand(String name, GFMenu plugin, MenuManager menuManager, String menuName) {
            super(name);
            this.plugin = plugin;
            this.menuManager = menuManager;
            this.menuName = menuName;
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLanguageManager().getMessage("player_only_open"));
                return true;
            }
            if (getPermission() != null && !player.hasPermission(getPermission())) {
                player.sendMessage(plugin.getLanguageManager().getMessage("menu_command_no_permission"));
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

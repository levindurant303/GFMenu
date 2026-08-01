package gofd.gFMenu.commands;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.editor.MenuEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MenuCommand implements CommandExecutor, TabCompleter {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final MenuEditor menuEditor;

    public MenuCommand(GFMenu plugin, MenuManager menuManager, MenuEditor menuEditor) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.menuEditor = menuEditor;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> reload(sender);
            case "open" -> open(sender, args);
            case "list" -> list(sender);
            case "debug" -> debug(sender);
            case "config" -> config(sender, args);
            case "lang" -> language(sender, args);
            case "edit" -> edit(sender, args);
            default -> {
                if (sender instanceof Player player) {
                    menuManager.openMenu(player, args[0]);
                } else {
                    sendKey(sender, "player_only_open");
                }
            }
        }
        return true;
    }

    private void reload(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return;
        }
        plugin.reloadPlugin();
        sendKey(sender, "reload_success");
    }

    private void open(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendKey(sender, "player_only_open");
            return;
        }
        if (!sender.hasPermission("gfmenu.open")) {
            sendKey(sender, "command.open_no_permission");
            return;
        }
        if (args.length < 2) {
            sendKey(sender, "command.open_usage");
            return;
        }
        menuManager.openMenu(player, args[1]);
    }

    private void list(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return;
        }
        if (menuManager.getAllMenuData().isEmpty()) {
            sendKey(sender, "command.no_menus");
            return;
        }
        sendKey(sender, "command.loaded_menus", menuManager.getLoadedMenuCount(),
                String.join("&7, &f", menuManager.getAllMenuData().keySet()));
    }

    private void debug(CommandSender sender) {
        if (!requireAdmin(sender)) {
            return;
        }
        sendKey(sender, "command.debug_menus", menuManager.getLoadedMenuCount());
        sendKey(sender, "command.debug_commands", menuManager.getCommandRegistry().getRegisteredCommandCount());
    }

    private void config(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) {
            return;
        }
        if (args.length < 2) {
            sendKey(sender, "command.config_usage");
            return;
        }
        if (args[1].equalsIgnoreCase("status")) {
            send(sender, menuManager.getGlobalConfigStatus());
        } else if (args[1].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sendKey(sender, "command.config_reloaded");
        } else {
            sendKey(sender, "command.config_usage");
        }
    }

    private void language(CommandSender sender, String[] args) {
        if (!requireAdmin(sender)) {
            return;
        }
        if (args.length < 2) {
            sendKey(sender, "command.lang_current", plugin.getLanguageManager().getCurrentLanguage());
            return;
        }
        String language = args[1].toLowerCase(Locale.ROOT);
        if (!language.equals("zh_cn") && !language.equals("en_us")) {
            sendKey(sender, "command.lang_supported");
            return;
        }
        plugin.getLanguageManager().setLanguage(language);
        sendKey(sender, "command.lang_changed", plugin.getLanguageManager().getCurrentLanguage());
    }

    private void edit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendKey(sender, "command.edit_player_only");
            return;
        }
        if (!requireAdmin(sender)) {
            return;
        }
        if (args.length < 2) {
            sendEditHelp(sender);
            return;
        }

        String menuName = args[1];
        if (args.length == 2) {
            sendResult(sender, menuEditor.openEditor(player, menuName));
            return;
        }

        String field = args[2].toLowerCase(Locale.ROOT);
        switch (field) {
            case "title" -> sendResult(sender, menuEditor.setTitle(menuName, join(args, 3)));
            case "permission" -> sendResult(sender, menuEditor.setPermission(menuName, join(args, 3)));
            case "size" -> sendResult(sender, setSize(menuName, args));
            case "item" -> sendResult(sender, setItem(menuName, args));
            case "remove" -> sendResult(sender, removeItem(menuName, args));
            case "lore" -> sendResult(sender, editLore(menuName, args));
            case "action" -> sendResult(sender, editAction(menuName, args));
            default -> sendEditHelp(sender);
        }
    }

    private MenuEditor.EditResult setSize(String menuName, String[] args) {
        if (args.length != 4) {
            return failure("command.usage_size");
        }
        try {
            return menuEditor.setSize(menuName, Integer.parseInt(args[3]));
        } catch (NumberFormatException exception) {
            return failure("command.error_size_number");
        }
    }

    private MenuEditor.EditResult setItem(String menuName, String[] args) {
        if (args.length < 5) {
            return failure("command.usage_item");
        }
        try {
            int slot = Integer.parseInt(args[3]);
            int amount = 1;
            int nameStart = 5;
            if (args.length > 5 && isInteger(args[5])) {
                amount = Integer.parseInt(args[5]);
                nameStart = 6;
            }
            return menuEditor.setItem(menuName, slot, args[4], amount, join(args, nameStart));
        } catch (NumberFormatException exception) {
            return failure("command.error_slot_amount_number");
        }
    }

    private MenuEditor.EditResult removeItem(String menuName, String[] args) {
        if (args.length != 4) {
            return failure("command.usage_remove");
        }
        try {
            return menuEditor.removeItem(menuName, Integer.parseInt(args[3]));
        } catch (NumberFormatException exception) {
            return failure("command.error_slot_number");
        }
    }

    private MenuEditor.EditResult editLore(String menuName, String[] args) {
        if (args.length < 5) {
            return failure("command.usage_lore");
        }
        try {
            return menuEditor.editLore(menuName, Integer.parseInt(args[3]), args[4], join(args, 5));
        } catch (NumberFormatException exception) {
            return failure("command.error_slot_number");
        }
    }

    private MenuEditor.EditResult editAction(String menuName, String[] args) {
        if (args.length < 6) {
            return failure("command.usage_action");
        }
        try {
            return menuEditor.editAction(menuName, Integer.parseInt(args[3]), args[4], args[5], join(args, 6));
        } catch (NumberFormatException exception) {
            return failure("command.error_slot_number");
        }
    }

    private boolean requireAdmin(CommandSender sender) {
        if (sender.hasPermission("gfmenu.admin")) {
            return true;
        }
        sendKey(sender, "command.admin_no_permission");
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sendKey(sender, "command.help_title");
        sendKey(sender, "command.help_open");
        sendKey(sender, "command.help_other");
        sendKey(sender, "command.help_edit");
    }

    private void sendEditHelp(CommandSender sender) {
        sendKey(sender, "command.edit_help_title");
        sendKey(sender, "command.edit_help_open");
        sendKey(sender, "command.edit_help_settings");
        sendKey(sender, "command.edit_help_item");
        sendKey(sender, "command.edit_help_lore");
        sendKey(sender, "command.edit_help_action");
    }

    private void sendResult(CommandSender sender, MenuEditor.EditResult result) {
        send(sender, result.message());
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(LayoutMenuItem.colorize(message));
    }

    private void sendKey(CommandSender sender, String key, Object... arguments) {
        sender.sendMessage(plugin.getLanguageManager().getMessage(key, arguments));
    }

    private MenuEditor.EditResult failure(String key, Object... arguments) {
        return new MenuEditor.EditResult(false, plugin.getLanguageManager().getMessage(key, arguments));
    }

    private static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static String join(String[] values, int start) {
        if (start >= values.length) {
            return "";
        }
        return String.join(" ", java.util.Arrays.copyOfRange(values, start, values.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(args[0], List.of("open", "list", "reload", "debug", "config", "lang", "edit"));
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("edit"))) {
            return filter(args[1], new ArrayList<>(menuManager.getAllMenuData().keySet()));
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            return filter(args[2], List.of("title", "permission", "size", "item", "remove", "lore", "action"));
        }
        return List.of();
    }

    private static List<String> filter(String input, List<String> values) {
        String lowercase = input.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lowercase)).toList();
    }
}

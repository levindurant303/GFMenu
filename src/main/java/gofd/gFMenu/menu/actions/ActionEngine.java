package gofd.gFMenu.menu.actions;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.actions.actions.BookInputSession;
import gofd.gFMenu.menu.actions.actions.CatcherSession;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Executes menu actions and owns short-lived text-input sessions. */
public final class ActionEngine {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final Map<UUID, CatcherSession> activeCatchers = new ConcurrentHashMap<>();
    private final Map<UUID, BookInputSession> activeBookInputs = new ConcurrentHashMap<>();

    public ActionEngine(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public boolean hasActiveCatcher(Player player) {
        CatcherSession session = activeCatchers.get(player.getUniqueId());
        return session != null && session.isActive();
    }

    public void startCatcherSession(Player player, String catcherId, List<String> startActions,
                                    List<String> endActions, List<String> cancelActions) {
        cancelCatcherSession(player, false);
        CatcherSession session = new CatcherSession(player, catcherId, startActions, endActions, cancelActions, this);
        activeCatchers.put(player.getUniqueId(), session);
        session.start();
    }

    public void startCatcherSession(Player player, String config) {
        String body = config.substring("catcher:".length()).trim();
        String[] parts = body.split(java.util.regex.Pattern.quote("|"));
        String catcherId = parts.length == 0 || parts[0].isBlank() ? "default" : parts[0].trim();
        List<String> start = new ArrayList<>();
        List<String> end = new ArrayList<>();
        List<String> cancel = new ArrayList<>();
        for (int index = 1; index < parts.length; index++) {
            String part = parts[index].trim();
            if (part.regionMatches(true, 0, "start=", 0, 6)) {
                start.add(part.substring(6).trim());
            } else if (part.regionMatches(true, 0, "end=", 0, 4)) {
                end.add(part.substring(4).trim());
            } else if (part.regionMatches(true, 0, "cancel=", 0, 7)) {
                cancel.add(part.substring(7).trim());
            }
        }
        startCatcherSession(player, catcherId, start, end, cancel);
    }

    public void endCatcherSession(Player player, String input) {
        CatcherSession session = activeCatchers.remove(player.getUniqueId());
        if (session != null) {
            session.handleInput(input);
        }
    }

    public void cancelCatcherSession(Player player) {
        cancelCatcherSession(player, true);
    }

    private void cancelCatcherSession(Player player, boolean notify) {
        CatcherSession session = activeCatchers.remove(player.getUniqueId());
        if (session != null) {
            session.cancel(notify);
        }
    }

    /**
     * Supported form: book:id|prompt=Write a message|end=console: say %book_input%
     * Repeat end= or cancel= to configure multiple actions.
     */
    public void startBookInput(Player player, String config) {
        String body = config.substring("book:".length()).trim();
        String[] parts = body.split(java.util.regex.Pattern.quote("|"));
        String inputId = parts.length == 0 || parts[0].isBlank() ? "book" : parts[0].trim();
        String prompt = plugin.getLanguageManager().getMessage("book_input_default_prompt");
        List<String> endActions = new ArrayList<>();
        List<String> cancelActions = new ArrayList<>();
        for (int index = 1; index < parts.length; index++) {
            String part = parts[index].trim();
            if (part.regionMatches(true, 0, "prompt=", 0, 7)) {
                prompt = part.substring(7).trim();
            } else if (part.regionMatches(true, 0, "end=", 0, 4)) {
                endActions.add(part.substring(4).trim());
            } else if (part.regionMatches(true, 0, "cancel=", 0, 7)) {
                cancelActions.add(part.substring(7).trim());
            }
        }
        cancelBookInput(player, false);
        BookInputSession session = new BookInputSession(player, inputId, prompt, endActions, cancelActions, this);
        activeBookInputs.put(player.getUniqueId(), session);
        session.openBook();
    }

    public boolean hasActiveBookInput(Player player) {
        BookInputSession session = activeBookInputs.get(player.getUniqueId());
        return session != null && session.isActive();
    }

    public void finishBookInput(Player player, List<String> pages) {
        BookInputSession session = activeBookInputs.remove(player.getUniqueId());
        if (session != null) {
            session.handleInput(String.join(System.lineSeparator(), pages));
        }
    }

    public void cancelBookInput(Player player) {
        cancelBookInput(player, true);
    }

    private void cancelBookInput(Player player, boolean notify) {
        BookInputSession session = activeBookInputs.remove(player.getUniqueId());
        if (session != null) {
            session.cancel(notify);
        }
    }

    public void cancelInputSessions(Player player) {
        cancelCatcherSession(player, false);
        cancelBookInput(player, false);
    }

    public void cleanupTimeoutSessions() {
        activeCatchers.entrySet().removeIf(entry -> {
            CatcherSession session = entry.getValue();
            if (!session.isTimedOut()) {
                return false;
            }
            session.timeout();
            return true;
        });
        activeBookInputs.entrySet().removeIf(entry -> {
            BookInputSession session = entry.getValue();
            if (!session.isTimedOut()) {
                return false;
            }
            session.timeout();
            return true;
        });
    }

    public void executeActions(Player player, List<String> actions) {
        if (actions == null) {
            return;
        }
        for (String action : actions) {
            executeSingleAction(player, action);
        }
    }

    public void executeSingleAction(Player player, String action) {
        if (action == null || action.isBlank()) {
            return;
        }
        String trimmed = normalizeActionAliases(replacePlayerPlaceholders(player, action.trim()));
        String lower = trimmed.toLowerCase(Locale.ROOT);
        try {
            if (lower.equals("close")) {
                player.closeInventory();
            } else if (lower.startsWith("command:")) {
                player.performCommand(stripLeadingSlash(trimmed.substring(8).trim()));
            } else if (lower.startsWith("console:") || lower.startsWith("op:")) {
                int prefixLength = lower.startsWith("console:") ? 8 : 3;
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(console, stripLeadingSlash(trimmed.substring(prefixLength).trim()));
            } else if (lower.startsWith("tell:") || lower.startsWith("message:")) {
                int separator = trimmed.indexOf(':');
                player.sendMessage(LayoutMenuItem.colorize(trimmed.substring(separator + 1).trim()));
            } else if (lower.startsWith("chat:")) {
                player.chat(trimmed.substring(5).trim());
            } else if (lower.startsWith("menu:")) {
                menuManager.openMenu(player, trimmed.substring(5).trim());
            } else if (lower.startsWith("sound:")) {
                playSound(player, trimmed.substring(6).trim());
            } else if (lower.startsWith("catcher:")) {
                player.closeInventory();
                startCatcherSession(player, trimmed);
            } else if (lower.startsWith("book:")) {
                player.closeInventory();
                startBookInput(player, trimmed);
            } else {
                plugin.getLogger().warning("Unknown menu action: " + action);
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("Failed to execute menu action '" + action + "': " + exception.getMessage());
        }
    }

    public void handleItemClick(InventoryClickEvent event, Map<String, List<String>> actions) {
        Player player = (Player) event.getWhoClicked();
        String type = event.isLeftClick() ? "left" : event.isRightClick() ? "right" : "all";
        List<String> selected = actions.get(type);
        if (selected == null || selected.isEmpty()) {
            selected = actions.get("all");
        }
        executeActions(player, selected);
    }

    public void handleItemClick(InventoryClickEvent event, int ignoredSlot, Map<String, List<String>> actions) {
        handleItemClick(event, actions);
    }

    public GFMenu getPlugin() {
        return plugin;
    }

    private static String replacePlayerPlaceholders(Player player, String value) {
        return value.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%server%", Bukkit.getServer().getName())
                .replace("%online%", Integer.toString(Bukkit.getOnlinePlayers().size()));
    }

    /** Chinese aliases keep book-authored actions readable without changing the YAML action model. */
    public static String normalizeActionAliases(String action) {
        if (action.equals("\u5173\u95ed")) {
            return "close";
        }
        return replaceActionPrefix(action, "\u63a7\u5236\u53f0\u547d\u4ee4:", "console:")
                .or(() -> replaceActionPrefix(action, "\u63a7\u5236\u53f0:", "console:"))
                .or(() -> replaceActionPrefix(action, "\u73a9\u5bb6\u547d\u4ee4:", "command:"))
                .or(() -> replaceActionPrefix(action, "\u547d\u4ee4:", "command:"))
                .or(() -> replaceActionPrefix(action, "\u6d88\u606f:", "message:"))
                .or(() -> replaceActionPrefix(action, "\u63d0\u793a:", "message:"))
                .or(() -> replaceActionPrefix(action, "\u804a\u5929:", "chat:"))
                .or(() -> replaceActionPrefix(action, "\u6253\u5f00\u83dc\u5355:", "menu:"))
                .or(() -> replaceActionPrefix(action, "\u83dc\u5355:", "menu:"))
                .or(() -> replaceActionPrefix(action, "\u58f0\u97f3:", "sound:"))
                .or(() -> replaceActionPrefix(action, "\u804a\u5929\u8f93\u5165:", "catcher:"))
                .or(() -> replaceActionPrefix(action, "\u4e66\u672c:", "book:"))
                .orElse(action);
    }

    private static java.util.Optional<String> replaceActionPrefix(String action, String prefix, String replacement) {
        return action.startsWith(prefix)
                ? java.util.Optional.of(replacement + action.substring(prefix.length()).trim())
                : java.util.Optional.empty();
    }

    private static String stripLeadingSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }

    private static void playSound(Player player, String soundData) {
        String[] parts = soundData.split("-", -1);
        if (parts.length == 0 || parts[0].isBlank()) {
            return;
        }
        String soundName = parts[0].trim();
        int namespaceSeparator = soundName.indexOf(':');
        if (namespaceSeparator >= 0) {
            soundName = soundName.substring(namespaceSeparator + 1);
        }
        Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
        float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1.0F;
        float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0F;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}

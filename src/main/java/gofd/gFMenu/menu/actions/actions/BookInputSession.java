package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Tracks a writable-book submission created by a menu action. */
public final class BookInputSession {

    private static final long TIMEOUT_MILLIS = 120_000L;

    private final Player player;
    private final String id;
    private final String prompt;
    private final List<String> endActions;
    private final List<String> cancelActions;
    private final ActionEngine actionEngine;
    private final long startedAt = System.currentTimeMillis();
    private boolean active = true;

    public BookInputSession(Player player, String id, String prompt, List<String> endActions,
                            List<String> cancelActions, ActionEngine actionEngine) {
        this.player = player;
        this.id = id;
        this.prompt = prompt;
        this.endActions = endActions == null ? List.of() : new ArrayList<>(endActions);
        this.cancelActions = cancelActions == null ? List.of() : new ArrayList<>(cancelActions);
        this.actionEngine = actionEngine;
    }

    public void openBook() {
        player.sendMessage(LayoutMenuItem.colorize(prompt));
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta instanceof BookMeta bookMeta) {
            bookMeta.setDisplayName(actionEngine.getPlugin().getLanguageManager().getMessage("book_input_title", id));
            book.setItemMeta(bookMeta);
        }
        player.openBook(book);
    }

    public void handleInput(String input) {
        if (!active) {
            return;
        }
        active = false;
        for (String action : endActions) {
            actionEngine.executeSingleAction(player, replaceInput(action, input));
        }
    }

    public void cancel(boolean notify) {
        if (!active) {
            return;
        }
        active = false;
        actionEngine.executeActions(player, cancelActions);
        if (notify) {
            player.sendMessage(actionEngine.getPlugin().getLanguageManager().getMessage("book_input_cancelled"));
        }
    }

    public void timeout() {
        if (!active) {
            return;
        }
        active = false;
        player.sendMessage(actionEngine.getPlugin().getLanguageManager().getMessage("book_input_timeout"));
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() - startedAt >= TIMEOUT_MILLIS;
    }

    public boolean isActive() {
        return active;
    }

    private String replaceInput(String action, String input) {
        String commandSafeInput = input.replace('\r', ' ').replace('\n', ' ').trim();
        return action.replace("%book_input%", commandSafeInput)
                .replace("{book_input}", commandSafeInput)
                .replace("%book_input_" + id + "%", commandSafeInput)
                .replace("{input}", commandSafeInput)
                .replace("%input%", commandSafeInput);
    }
}

package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** A short-lived chat input session created by a catcher action. */
public final class CatcherSession {

    private static final long TIMEOUT_MILLIS = 30_000L;

    private final Player player;
    private final String id;
    private final List<String> startActions;
    private final List<String> endActions;
    private final List<String> cancelActions;
    private final ActionEngine actionEngine;
    private final long startedAt = System.currentTimeMillis();
    private boolean active = true;

    public CatcherSession(Player player, String id, List<String> startActions, List<String> endActions,
                          List<String> cancelActions, ActionEngine actionEngine) {
        this.player = player;
        this.id = id;
        this.startActions = startActions == null ? List.of() : new ArrayList<>(startActions);
        this.endActions = endActions == null ? List.of() : new ArrayList<>(endActions);
        this.cancelActions = cancelActions == null ? List.of() : new ArrayList<>(cancelActions);
        this.actionEngine = actionEngine;
    }

    public void start() {
        actionEngine.executeActions(player, startActions);
        player.sendMessage(actionEngine.getPlugin().getLanguageManager().getMessage("catcher_input_prompt"));
    }

    public void handleInput(String input) {
        if (!active) {
            return;
        }
        if (input.equalsIgnoreCase("cancel") || input.equals("\u53d6\u6d88")) {
            cancel(true);
            return;
        }
        active = false;
        for (String action : endActions) {
            actionEngine.executeSingleAction(player, replaceInput(action, input, id));
        }
    }

    public void cancel(boolean notify) {
        if (!active) {
            return;
        }
        active = false;
        actionEngine.executeActions(player, cancelActions);
        if (notify) {
            player.sendMessage(actionEngine.getPlugin().getLanguageManager().getMessage("catcher_input_cancelled"));
        }
    }

    public void timeout() {
        if (!active) {
            return;
        }
        active = false;
        player.sendMessage(actionEngine.getPlugin().getLanguageManager().getMessage("catcher_input_timeout"));
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() - startedAt >= TIMEOUT_MILLIS;
    }

    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    static String replaceInput(String action, String input, String catcherId) {
        return action.replace("{input}", input)
                .replace("%input%", input)
                .replace("%player_input%", input)
                .replace("{meta:input}", input)
                .replace("%trmenu_meta_input%", input)
                .replace("%trmenu_meta_input-" + catcherId + "%", input);
    }
}

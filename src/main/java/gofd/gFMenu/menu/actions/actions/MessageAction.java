package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.actions.Action;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;
import java.util.Map;

public class MessageAction implements Action {

    @Override
    public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
        String message = action.substring(5).trim(); // 去掉 "tell:"
        player.sendMessage(message.replace("&", "§"));
    }

    @Override
    public boolean canExecute(Player player, String action) {
        return action.startsWith("tell:");
    }
}
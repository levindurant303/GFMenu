package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.actions.Action;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;
import java.util.Map;

public class CloseAction implements Action {

    @Override
    public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
        player.closeInventory();
    }

    @Override
    public boolean canExecute(Player player, String action) {
        return action.equals("close");
    }
}

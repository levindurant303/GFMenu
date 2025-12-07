package gofd.gFMenu.menu.actions.actions;

import gofd.gFMenu.menu.actions.Action;
import gofd.gFMenu.menu.actions.ActionEngine;
import org.bukkit.entity.Player;
import java.util.Map;

public class CommandAction implements Action {

    @Override
    public void execute(Player player, String action, ActionEngine engine, Map<String, String> variables) {
        String command = action.substring(8).trim(); // 去掉 "command:"

        // 执行命令
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        player.performCommand(command);
    }

    @Override
    public boolean canExecute(Player player, String action) {
        return action.startsWith("command:");
    }
}
package gofd.gFMenu.menu.actions;

import org.bukkit.entity.Player;
import java.util.Map;

public interface Action {
    void execute(Player player, String action, ActionEngine engine, Map<String, String> variables);
    boolean canExecute(Player player, String action);
}
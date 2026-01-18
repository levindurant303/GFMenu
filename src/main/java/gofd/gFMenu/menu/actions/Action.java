package gofd.gFMenu.menu.actions;

import java.util.Map;
import org.bukkit.entity.Player;

public interface Action {
  void execute(Player paramPlayer, String paramString, ActionEngine paramActionEngine, Map<String, String> paramMap);
  
  boolean canExecute(Player paramPlayer, String paramString);
}

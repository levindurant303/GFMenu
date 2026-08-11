package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.configuration.file.YamlConfiguration;

public interface MenuParser {
  MenuFormat getFormat();
  
  LayoutMenuData parse(String paramString, YamlConfiguration paramYamlConfiguration);
}


package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class DeluxeMenuParser implements MenuParser {

    @Override
    public MenuFormat getFormat() {
        return MenuFormat.DELUXE;
    }

    @Override
    public LayoutMenuData parse(String menuName, YamlConfiguration config) {
        LayoutMenuData menu = new LayoutMenuData(menuName);
        menu.setTitle(config.getString("menu_title", config.getString("title", "Menu")));
        menu.setPermission(config.getString("open_permission"));
        menu.setCommands(config.getStringList("open_command"));
        menu.setMenuSize(normalizeSize(config.getInt("size", config.getInt("menu_size", 54))));
        menu.setOpenEvents(config.getStringList("open_commands"));
        menu.setCloseEvents(config.getStringList("close_commands"));

        ConfigurationSection nestedItems = config.getConfigurationSection("items");
        if (nestedItems != null) {
            parseItems(menu, nestedItems, "items.");
        }
        parseItems(menu, config, "");
        return menu;
    }

    private void parseItems(LayoutMenuData menu, ConfigurationSection root, String prefix) {
        char nextIcon = (char) ('A' + menu.getItems().size());
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null || !section.contains("slot")) {
                continue;
            }
            int slot = section.getInt("slot", -1);
            if (slot < 0 || slot >= menu.getMenuSize()) {
                continue;
            }

            LayoutMenuItem item = new LayoutMenuItem();
            item.setIconChar(nextIcon);
            item.setSlot(slot);
            item.setSourceKey(prefix + key);
            item.setMaterial(section.getString("material", "STONE"));
            item.setName(section.getString("display_name", section.getString("name", "")));
            item.setLore(section.getStringList("lore"));
            item.setAmount(section.getInt("amount", 1));
            item.setGlowing(section.getBoolean("glow", false));
            item.setSkullOwner(section.getString("skull_owner"));
            item.setActions("left", convertCommands(section.getStringList("left_click_commands")));
            item.setActions("right", convertCommands(section.getStringList("right_click_commands")));
            item.setActions("all", convertCommands(section.getStringList("click_commands")));
            menu.addItem(nextIcon, item);
            nextIcon++;
        }
    }

    private List<String> convertCommands(List<String> commands) {
        return commands.stream()
                .filter(command -> command != null && !command.isBlank())
                .map(this::convertCommand)
                .toList();
    }

    private String convertCommand(String command) {
        String trimmed = command.trim();
        String lowercase = trimmed.toLowerCase(java.util.Locale.ROOT);
        if (lowercase.startsWith("command:") || lowercase.startsWith("console:")
                || lowercase.startsWith("tell:") || lowercase.startsWith("message:")
                || lowercase.startsWith("chat:") || lowercase.startsWith("menu:")
                || lowercase.startsWith("sound:") || lowercase.startsWith("catcher:")
                || lowercase.startsWith("book:") || lowercase.equals("close")) {
            return trimmed;
        }
        if (trimmed.regionMatches(true, 0, "[player]", 0, 8)) {
            return "command: " + trimmed.substring(8).trim();
        }
        if (trimmed.regionMatches(true, 0, "[console]", 0, 9)) {
            return "console: " + trimmed.substring(9).trim();
        }
        if (trimmed.regionMatches(true, 0, "[close]", 0, 7)) {
            return "close";
        }
        if (trimmed.regionMatches(true, 0, "[open]", 0, 6)) {
            return "menu: " + trimmed.substring(6).trim();
        }
        if (trimmed.regionMatches(true, 0, "[message]", 0, 9)) {
            return "tell: " + trimmed.substring(9).trim();
        }
        return "command: " + trimmed;
    }

    private int normalizeSize(int size) {
        int bounded = Math.max(9, Math.min(54, size));
        return ((bounded + 8) / 9) * 9;
    }
}

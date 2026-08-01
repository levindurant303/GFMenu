package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.TrMenuLayoutParser;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;

public final class TrMenuParser implements MenuParser {

    @Override
    public MenuFormat getFormat() {
        return MenuFormat.TRMENU;
    }

    @Override
    public LayoutMenuData parse(String menuName, YamlConfiguration config) {
        LayoutMenuData menu = new LayoutMenuData(menuName);
        menu.setTitle(config.getString("Title", config.getString("title", "Menu")));

        List<String> layout = config.getStringList(config.contains("layout") ? "layout" : "Layout");
        menu.setRawLayout(layout);
        boolean centerEnabled = config.getBoolean("Settings.center", false);
        menu.setCenterEnabled(centerEnabled);
        menu.setLayoutSlots(TrMenuLayoutParser.parseLayout(layout, centerEnabled));
        menu.setMenuSize(TrMenuLayoutParser.calculateInventorySize(layout));

        ConfigurationSection icons = config.getConfigurationSection("Icons");
        if (icons != null) {
            parseIcons(menu, icons);
        }
        menu.setCommands(config.getStringList("Bindings.Commands"));
        menu.setPermission(config.getString("Settings.permission"));
        menu.setOpenEvents(config.getStringList("Events.Open"));
        menu.setCloseEvents(config.getStringList("Events.Close"));
        return menu;
    }

    private void parseIcons(LayoutMenuData menu, ConfigurationSection icons) {
        Map<Character, Integer> slots = menu.getLayoutSlots();
        for (String iconKey : icons.getKeys(false)) {
            if (iconKey.length() != 1) {
                continue;
            }
            char iconChar = iconKey.charAt(0);
            Integer slot = slots.get(iconChar);
            ConfigurationSection iconConfig = icons.getConfigurationSection(iconKey);
            if (slot == null || iconConfig == null) {
                continue;
            }

            LayoutMenuItem item = parseItem(iconChar, slot, iconConfig);
            menu.addItem(iconChar, item);
        }
    }

    private LayoutMenuItem parseItem(char iconChar, int slot, ConfigurationSection config) {
        LayoutMenuItem item = new LayoutMenuItem();
        item.setIconChar(iconChar);
        item.setSlot(slot);
        item.setSourceKey("Icons." + iconChar);

        ConfigurationSection display = config.getConfigurationSection("display");
        if (display == null) {
            display = config;
        }
        item.setMaterial(display.getString("material", "STONE"));
        item.setName(display.getString("name", display.getString("display_name", "")));
        item.setLore(display.getStringList("lore"));
        item.setAmount(display.getInt("amount", 1));
        item.setGlowing(display.getBoolean("glow", config.getBoolean("glow", false)));
        item.setSkullOwner(display.getString("skull_owner", config.getString("skull_owner")));

        Object actions = config.get("actions");
        if (actions instanceof List<?>) {
            item.setActions("all", config.getStringList("actions"));
        } else if (actions instanceof ConfigurationSection actionSection) {
            for (String type : actionSection.getKeys(false)) {
                item.setActions(type, actionSection.getStringList(type));
            }
        }
        return item;
    }
}

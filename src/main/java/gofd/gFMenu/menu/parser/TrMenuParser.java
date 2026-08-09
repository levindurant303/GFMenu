package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.TrMenuLayoutParser;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
            item.setActions("all", parseActionValues(actions));
        } else if (actions instanceof ConfigurationSection actionSection) {
            for (String type : actionSection.getKeys(false)) {
                item.setActions(type, parseActionValues(actionSection.get(type)));
            }
        }
        return item;
    }

    private List<String> parseActionValues(Object configured) {
        List<String> actions = new ArrayList<>();
        if (configured instanceof String action) {
            if (!action.isBlank()) {
                actions.add(action);
            }
        } else if (configured instanceof List<?> values) {
            for (Object value : values) {
                actions.addAll(parseActionValues(value));
            }
        } else {
            Map<String, Object> values = sectionValues(configured);
            Object catchers = values.get("catcher");
            if (catchers != null) {
                actions.addAll(parseCatchers(catchers));
            }
        }
        return actions;
    }

    private List<String> parseCatchers(Object configured) {
        List<String> actions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sectionValues(configured).entrySet()) {
            Map<String, Object> catcher = sectionValues(entry.getValue());
            StringBuilder action = new StringBuilder("catcher:").append(entry.getKey());
            appendSegments(action, "start", parseActionValues(catcher.get("start")));
            appendSegments(action, "cancel", parseActionValues(catcher.get("cancel")));
            appendSegments(action, "end", extractEndActions(catcher.get("end")));
            actions.add(action.toString());
        }
        return actions;
    }

    private List<String> extractEndActions(Object configured) {
        List<String> direct = parseActionValues(configured);
        if (!direct.isEmpty()) {
            return direct;
        }
        List<String> actions = new ArrayList<>();
        if (configured instanceof List<?> values) {
            for (Object value : values) {
                actions.addAll(extractEndActions(value));
            }
            return actions;
        }
        Map<String, Object> values = sectionValues(configured);
        if (values.containsKey("actions")) {
            actions.addAll(parseActionValues(values.get("actions")));
        } else if (values.containsKey("action")) {
            actions.addAll(parseActionValues(values.get("action")));
        }
        return actions;
    }

    private static void appendSegments(StringBuilder action, String key, List<String> values) {
        for (String value : values) {
            action.append('|').append(key).append('=').append(value);
        }
    }

    private static Map<String, Object> sectionValues(Object configured) {
        Map<?, ?> source;
        if (configured instanceof ConfigurationSection section) {
            source = section.getValues(false);
        } else if (configured instanceof Map<?, ?> map) {
            source = map;
        } else {
            return Map.of();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        source.forEach((key, value) -> values.put(String.valueOf(key), value));
        return values;
    }
}

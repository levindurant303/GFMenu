package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.TrMenuLayoutParser;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TrMenuParser implements MenuParser {

    private final boolean defaultCenterEnabled;

    public TrMenuParser() {
        this(false);
    }

    public TrMenuParser(boolean defaultCenterEnabled) {
        this.defaultCenterEnabled = defaultCenterEnabled;
    }

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
        boolean centerEnabled = config.getBoolean("Settings.center", defaultCenterEnabled);
        menu.setCenterEnabled(centerEnabled);
        ConfigurationSection icons = config.getConfigurationSection("Icons");
        Map<Character, List<Integer>> positions = new LinkedHashMap<>(
                TrMenuLayoutParser.parseLayoutPositions(layout, centerEnabled));
        if (icons != null) {
            for (String iconKey : icons.getKeys(false)) {
                if (iconKey.length() != 1) {
                    continue;
                }
                ConfigurationSection iconConfig = icons.getConfigurationSection(iconKey);
                if (iconConfig == null || !iconConfig.contains("slots")) {
                    continue;
                }
                List<Integer> explicitSlots = parseSlots(iconConfig.get("slots"));
                if (!explicitSlots.isEmpty()) {
                    positions.put(iconKey.charAt(0), explicitSlots);
                }
            }
        }
        menu.setLayoutPositions(positions);
        int highestSlot = positions.values().stream().flatMap(List::stream).mapToInt(Integer::intValue).max().orElse(-1);
        int layoutSize = TrMenuLayoutParser.calculateInventorySize(layout);
        int requiredSize = highestSlot < 0 ? layoutSize : Math.max(layoutSize, ((highestSlot / 9) + 1) * 9);
        menu.setMenuSize(Math.max(9, Math.min(54, requiredSize)));
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
        Map<Character, List<Integer>> slots = menu.getLayoutPositions();
        for (String iconKey : icons.getKeys(false)) {
            if (iconKey.length() != 1) {
                continue;
            }
            char iconChar = iconKey.charAt(0);
            List<Integer> iconSlots = slots.get(iconChar);
            ConfigurationSection iconConfig = icons.getConfigurationSection(iconKey);
            if (iconSlots == null || iconSlots.isEmpty() || iconConfig == null) {
                continue;
            }

            LayoutMenuItem item = parseItem(iconChar, iconSlots.get(0), iconConfig);
            menu.addItem(iconChar, item);
        }
    }

    private static List<Integer> parseSlots(Object configured) {
        Set<Integer> slots = new LinkedHashSet<>();
        collectSlots(configured, slots, true);
        return List.copyOf(slots);
    }

    private static void collectSlots(Object configured, Set<Integer> slots, boolean allowNested) {
        if (configured instanceof Number number) {
            addSlot(number.intValue(), slots);
            return;
        }
        if (configured instanceof String text) {
            for (String token : text.split(",")) {
                String value = token.trim();
                int separator = value.indexOf('-');
                if (separator > 0 && separator < value.length() - 1) {
                    try {
                        int start = Integer.parseInt(value.substring(0, separator).trim());
                        int end = Integer.parseInt(value.substring(separator + 1).trim());
                        int firstSlot = Math.max(0, Math.min(start, end));
                        int lastSlot = Math.min(53, Math.max(start, end));
                        for (int slot = firstSlot; slot <= lastSlot; slot++) {
                            addSlot(slot, slots);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    try {
                        addSlot(Integer.parseInt(value), slots);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return;
        }
        if (configured instanceof List<?> values) {
            for (Object value : values) {
                if (value instanceof List<?> nested) {
                    if (allowNested) {
                        collectSlots(nested, slots, false);
                        break;
                    }
                } else {
                    collectSlots(value, slots, false);
                }
            }
        }
    }

    private static void addSlot(int slot, Set<Integer> slots) {
        if (slot >= 0 && slot < 54) {
            slots.add(slot);
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
            Object type = catcher.get("type");
            if (type != null && !String.valueOf(type).isBlank()) {
                action.append("|type=").append(type);
            }
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

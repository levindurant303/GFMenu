package gofd.gFMenu;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads a selected language resource and persists language changes to config.yml. */
public final class LanguageManager {

    private static final String FALLBACK_LANGUAGE = "en_US";

    private final GFMenu plugin;
    private final Map<String, String> messages = new LinkedHashMap<>();
    private String currentLanguage = FALLBACK_LANGUAGE;

    public LanguageManager(GFMenu plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        currentLanguage = normalizeLanguage(plugin.getConfig().getString("language", FALLBACK_LANGUAGE));
        ensureBundledLanguage(currentLanguage);
        messages.clear();

        // Bundled files provide defaults for new keys; disk files may override individual messages.
        loadBundled(FALLBACK_LANGUAGE);
        if (!FALLBACK_LANGUAGE.equals(currentLanguage)) {
            loadBundled(currentLanguage);
        }
        File languageFile = new File(plugin.getDataFolder(), "languages/" + currentLanguage + ".yml");
        load(languageFile);
    }

    public String getMessage(String key, Object... arguments) {
        String message = messages.getOrDefault(key, "&c[Missing message: " + key + "]");
        for (int index = 0; index < arguments.length; index++) {
            message = message.replace("{" + index + "}", String.valueOf(arguments[index]));
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean hasMessage(String key) {
        return messages.containsKey(key);
    }

    public Map<String, String> getAllMessages() {
        return Map.copyOf(messages);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String language) {
        currentLanguage = normalizeLanguage(language);
        plugin.getConfig().set("language", currentLanguage);
        plugin.saveConfig();
        reload();
    }

    private void load(File file) {
        load(YamlConfiguration.loadConfiguration(file));
    }

    private void loadBundled(String language) {
        try (InputStream resource = plugin.getResource("languages/" + language + ".yml")) {
            if (resource != null) {
                load(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8)));
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Unable to load bundled language " + language + ": " + exception.getMessage());
        }
    }

    private void load(YamlConfiguration configuration) {
        for (String key : configuration.getKeys(true)) {
            if (configuration.isString(key)) {
                messages.put(key, configuration.getString(key));
            }
        }
    }

    private void ensureBundledLanguage(String language) {
        File destination = new File(plugin.getDataFolder(), "languages/" + language + ".yml");
        if (!destination.exists()) {
            plugin.saveResource("languages/" + language + ".yml", false);
        }
    }

    private static String normalizeLanguage(String language) {
        return "zh_cn".equalsIgnoreCase(language) ? "zh_CN" : FALLBACK_LANGUAGE;
    }
}

package gofd.gFMenu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageResourcesTest {
    @Test
    void chineseAndEnglishResourcesHaveMatchingMessageKeys() throws Exception {
        YamlConfiguration chinese = load("languages/zh_CN.yml");
        YamlConfiguration english = load("languages/en_US.yml");
        Set<String> chineseKeys = stringKeys(chinese);
        Set<String> englishKeys = stringKeys(english);

        assertEquals(chineseKeys, englishKeys);
        assertTrue(chineseKeys.contains("plugin.enabled"));
        assertTrue(chineseKeys.contains("command.lang_usage"));
        assertTrue(chineseKeys.contains("editor.panel.title"));
        assertTrue(chineseKeys.contains("update.latest"));
    }

    private static YamlConfiguration load(String resourcePath) throws Exception {
        try (InputStream input = LanguageResourcesTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Missing language resource: " + resourcePath);
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
    }

    private static Set<String> stringKeys(YamlConfiguration configuration) {
        return configuration.getKeys(true).stream()
                .filter(configuration::isString)
                .collect(Collectors.toSet());
    }
}

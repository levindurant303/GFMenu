package gofd.gFMenu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReleaseResourceTest {

    @Test
    void releaseMetadataAndNewAdminMessagesAreBundled() {
        YamlConfiguration plugin = read("plugin.yml");
        assertEquals("R1.3.0", plugin.getString("version"));

        for (String language : new String[]{"zh_CN", "en_US"}) {
            YamlConfiguration messages = read("languages/" + language + ".yml");
            assertNotNull(messages.getString("command.backup_started"));
            assertNotNull(messages.getString("command.validate_ok"));
            assertNotNull(messages.getString("editor.book.shift_left_title"));
            assertNotNull(messages.getString("editor.panel.drop_lore"));
        }
    }

    private static YamlConfiguration read(String resource) {
        InputStream stream = ReleaseResourceTest.class.getClassLoader().getResourceAsStream(resource);
        assertNotNull(stream, "Missing bundled resource " + resource);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception exception) {
            throw new AssertionError("Could not load " + resource, exception);
        }
    }
}

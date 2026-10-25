package gofd.gFMenu.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdateCheckerTest {
    @Test
    void readsReleaseFieldsAndUnescapesMarkdown() {
        String json = "{\"tag_name\":\"R1.2.1\",\"body\":\"First line\\nSecond line with \\\"quotes\\\"\"}";

        assertEquals("R1.2.1", UpdateChecker.findJsonString(json, "tag_name"));
        assertEquals("First line\nSecond line with \"quotes\"", UpdateChecker.findJsonString(json, "body"));
        assertNull(UpdateChecker.findJsonString(json, "html_url"));
    }
}

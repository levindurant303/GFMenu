package gofd.gFMenu.menu.actions;

import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrMenuClickCompatibilityTest {

    @Test
    void shiftClickUsesSpecificActionBeforeBaseAndAllActions() {
        Map<String, List<String>> actions = Map.of(
                "shift_left", List.of("specific"),
                "left", List.of("base"),
                "all", List.of("all"));

        assertEquals(List.of("specific"), ActionEngine.actionsForClick(ClickType.SHIFT_LEFT, actions));
        assertEquals(List.of("base"), ActionEngine.actionsForClick(ClickType.SHIFT_RIGHT,
                Map.of("right", List.of("base"), "all", List.of("all"))));
        assertEquals(List.of("all"), ActionEngine.actionsForClick(ClickType.DROP, actions));
    }

    @Test
    void bracketActionsNormalizeIntoReadableBookActionSyntax() {
        assertEquals("command: warp spawn", ActionEngine.normalizeActionAliases("[player] warp spawn"));
        assertEquals("console: give %player% diamond 1",
                ActionEngine.normalizeActionAliases("[console] give %player% diamond 1"));
        assertEquals("close", ActionEngine.normalizeActionAliases("[close]"));
    }
}

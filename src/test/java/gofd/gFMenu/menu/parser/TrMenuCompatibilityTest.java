package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TrMenuCompatibilityTest {

    @Test
    void repeatedLayoutSymbolsShareOneItemAcrossAllSlots() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                Layout:
                  - "A A A"
                Icons:
                  A:
                    display:
                      material: DIAMOND
                """));

        LayoutMenuData menu = new TrMenuParser().parse("repeated", config);
        LayoutMenuItem item = menu.getItemAtSlot(0);

        assertNotNull(item);
        assertSame(item, menu.getItemAtSlot(2));
        assertSame(item, menu.getItemAtSlot(4));
        assertEquals(List.of(0, 2, 4), menu.getPositionsForIcon('A'));
    }

    @Test
    void explicitStaticSlotsOverrideLayoutAndExpandInventory() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                layout:
                  - "A"
                Icons:
                  A:
                    slots: [10, "12-13"]
                    display:
                      material: DIAMOND
                """));

        LayoutMenuData menu = new TrMenuParser().parse("manual-slots", config);

        assertEquals(18, menu.getInventorySize());
        assertEquals(List.of(10, 12, 13), menu.getPositionsForIcon('A'));
        assertNull(menu.getItemAtSlot(0));
        assertNotNull(menu.getItemAtSlot(10));
    }

    @Test
    void configuredCenterDefaultIsAppliedBeforeSlotCalculation() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                layout:
                  - "ABC"
                Icons:
                  A:
                    display:
                      material: STONE
                  B:
                    display:
                      material: STONE
                  C:
                    display:
                      material: STONE
                """));

        LayoutMenuData menu = new TrMenuParser(true).parse("centered", config);

        assertEquals(List.of(3), menu.getPositionsForIcon('A'));
        assertEquals(List.of(4), menu.getPositionsForIcon('B'));
        assertEquals(List.of(5), menu.getPositionsForIcon('C'));
    }

    @Test
    void hyphenatedClickGroupsNormalizeForTheBookEditor() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                layout:
                  - "A"
                Icons:
                  A:
                    display:
                      material: STONE
                    actions:
                      shift-left:
                        - "message: shifted"
                """));

        LayoutMenuItem item = new TrMenuParser().parse("click-group", config).getItemAtSlot(0);

        assertEquals(List.of("message: shifted"), item.getActions("shift_left"));
    }

    @Test
    void deluxeItemsKeepTheirSingleSlotLookup() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                menu_title: "Sample"
                size: 9
                items:
                  sample:
                    slot: 4
                    material: EMERALD
                """));

        LayoutMenuData menu = new DeluxeMenuParser().parse("deluxe", config);

        assertNotNull(menu.getItemAtSlot(4));
        assertEquals(List.of(4), menu.getPositionsForIcon(menu.getItemAtSlot(4).getIconChar()));
    }

    @Test
    void structuredCatcherTypeIsKeptForBookRoundTrips() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                layout:
                  - "A"
                Icons:
                  A:
                    actions:
                      left:
                        - catcher:
                            input:
                              type: CHAT
                              start: "message: enter"
                """));

        LayoutMenuItem item = new TrMenuParser().parse("catcher", config).getItemAtSlot(0);

        assertEquals(List.of("catcher:input|type=CHAT|start=message: enter"), item.getActions("left"));
    }
}

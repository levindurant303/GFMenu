package gofd.gFMenu.menu.parser;

import gofd.gFMenu.menu.LayoutMenuItem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrMenuParserTest {

    @Test
    void parsesScalarAndStructuredCatcherActions() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader("""
                layout:
                  - 'F'
                Icons:
                  F:
                    display:
                      material: GOLD_NUGGET
                    actions:
                      all: 'sound: BLOCK_GRASS_BREAK'
                      left:
                        - close
                        - catcher:
                            pay:
                              type: CHAT
                              start: 'tell: enter payment'
                              cancel: 'tell: cancelled'
                              end:
                                - condition:
                                  actions:
                                    - 'op: pay %trmenu_meta_input-pay%'
                                    - 'tell: paid %trmenu_meta_input-pay%'
                """));

        LayoutMenuItem item = new TrMenuParser().parse("payment", config).getItemAtSlot(0);

        assertNotNull(item);
        assertEquals(List.of("sound: BLOCK_GRASS_BREAK"), item.getActions("all"));
        assertEquals(List.of(
                "close",
                "catcher:pay|start=tell: enter payment|cancel=tell: cancelled"
                        + "|end=op: pay %trmenu_meta_input-pay%"
                        + "|end=tell: paid %trmenu_meta_input-pay%"
        ), item.getActions("left"));
    }
}

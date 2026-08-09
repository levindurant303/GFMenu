package gofd.gFMenu.menu.actions.actions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatcherSessionTest {

    @Test
    void replacesCurrentTrMenuCatcherInput() {
        String action = "op: pay %trmenu_meta_input-pay%";

        assertEquals("op: pay Kevin 50", CatcherSession.replaceInput(action, "Kevin 50", "pay"));
    }
}

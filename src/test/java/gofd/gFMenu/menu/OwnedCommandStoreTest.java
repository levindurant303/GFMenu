package gofd.gFMenu.menu;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OwnedCommandStoreTest {

    @Test
    void removingOneMenuKeepsOtherMenuCommandsRegistered() {
        OwnedCommandStore<String> store = new OwnedCommandStore<>();
        store.add("shop", "shop", "shop-command");
        store.add("shop", "buy", "buy-command");
        store.add("profile", "profile", "profile-command");

        Collection<String> removed = store.removeOwner("shop");

        assertEquals(Set.of("shop-command", "buy-command"), Set.copyOf(removed));
        assertEquals(Set.of("profile"), store.keys());
        assertEquals(1, store.size());
        assertFalse(store.containsKey("shop"));
        assertTrue(store.containsKey("profile"));
    }
}

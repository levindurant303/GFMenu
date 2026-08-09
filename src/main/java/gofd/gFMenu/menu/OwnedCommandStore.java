package gofd.gFMenu.menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Tracks registered commands by the menu that owns them. */
final class OwnedCommandStore<T> {

    private final Map<String, T> commands = new LinkedHashMap<>();
    private final Map<String, Set<String>> ownerKeys = new LinkedHashMap<>();

    boolean containsKey(String key) {
        return commands.containsKey(key);
    }

    void add(String owner, String key, T command) {
        commands.put(key, command);
        ownerKeys.computeIfAbsent(owner, ignored -> new LinkedHashSet<>()).add(key);
    }

    Collection<T> removeOwner(String owner) {
        Set<String> keys = ownerKeys.remove(owner);
        if (keys == null) {
            return List.of();
        }
        List<T> removed = new ArrayList<>(keys.size());
        for (String key : keys) {
            T command = commands.remove(key);
            if (command != null) {
                removed.add(command);
            }
        }
        return removed;
    }

    Collection<T> removeAll() {
        List<T> removed = new ArrayList<>(commands.values());
        commands.clear();
        ownerKeys.clear();
        return removed;
    }

    int size() {
        return commands.size();
    }

    boolean isEmpty() {
        return commands.isEmpty();
    }

    Set<String> keys() {
        return new LinkedHashSet<>(commands.keySet());
    }
}

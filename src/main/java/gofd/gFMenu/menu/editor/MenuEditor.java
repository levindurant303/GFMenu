package gofd.gFMenu.menu.editor;

import gofd.gFMenu.GFMenu;
import gofd.gFMenu.menu.LayoutMenuData;
import gofd.gFMenu.menu.LayoutMenuItem;
import gofd.gFMenu.menu.MenuManager;
import gofd.gFMenu.menu.actions.ActionEngine;
import gofd.gFMenu.menu.format.MenuFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Provides GUI and command-driven editing while preserving each menu's native YAML format. */
public final class MenuEditor implements Listener {

    private final GFMenu plugin;
    private final MenuManager menuManager;
    private final Map<String, UUID> activeEditors = new HashMap<>();
    private final Map<UUID, ItemEditSession> itemEditSessions = new HashMap<>();
    private final Map<UUID, FieldBookSession> pendingBookEdits = new HashMap<>();
    private final Set<UUID> transitioningInventoryViews = new HashSet<>();

    public MenuEditor(GFMenu plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
    }

    public EditResult openEditor(Player player, String menuName) {
        LayoutMenuData menu = menuManager.getMenu(menuName);
        if (menu == null) {
            return failure("editor.error.menu_not_found");
        }
        String key = menu.getName().toLowerCase(Locale.ROOT);
        UUID currentEditor = activeEditors.get(key);
        if (currentEditor != null) {
            return failure(currentEditor.equals(player.getUniqueId())
                    ? "editor.already_open_self"
                    : "editor.already_open_other");
        }

        activeEditors.put(key, player.getUniqueId());
        openMainEditor(player, menu.getName());
        return success("editor.open_tip");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (transitioningInventoryViews.contains(playerId)) {
            return;
        }
        if (event.getInventory().getHolder() instanceof MenuEditorHolder holder
                && playerId.equals(holder.getOwnerId())) {
            if (pendingBookEdits.containsKey(playerId) || itemEditSessions.containsKey(playerId)) {
                return;
            }
            activeEditors.remove(holder.getMenuName().toLowerCase(Locale.ROOT), holder.getOwnerId());
            send(player, "editor.closed");
            return;
        }

        if (!(event.getInventory().getHolder() instanceof ItemEditHolder holder)
                || !playerId.equals(holder.getOwnerId()) || pendingBookEdits.containsKey(playerId)) {
            return;
        }
        ItemEditSession session = itemEditSessions.get(playerId);
        if (session == null || !session.matches(holder)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && itemEditSessions.get(playerId) == session
                    && !pendingBookEdits.containsKey(playerId)) {
                itemEditSessions.remove(playerId, session);
                openMainEditor(player, session.menuName());
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (topInventory.getHolder() instanceof MenuEditorHolder holder
                && event.getWhoClicked().getUniqueId().equals(holder.getOwnerId())) {
            handleMainEditorClick(event, holder);
            return;
        }
        if (topInventory.getHolder() instanceof ItemEditHolder holder
                && event.getWhoClicked().getUniqueId().equals(holder.getOwnerId())) {
            handleItemPanelClick(event, holder);
        }
    }

    private void handleMainEditorClick(InventoryClickEvent event, MenuEditorHolder holder) {
        Inventory topInventory = event.getView().getTopInventory();
        if (event.getRawSlot() < 0 || event.getRawSlot() >= topInventory.getSize()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (pendingBookEdits.containsKey(player.getUniqueId()) || itemEditSessions.containsKey(player.getUniqueId())) {
            send(player, "editor.finish_book_first");
            return;
        }

        ItemStack selected = isAir(event.getCursor()) ? event.getCurrentItem() : event.getCursor();
        if (isAir(selected)) {
            send(player, "editor.select_item");
            return;
        }
        openItemPanel(player, holder.getMenuName(), event.getRawSlot(), selected.clone());
    }

    private void handleItemPanelClick(InventoryClickEvent event, ItemEditHolder holder) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemEditSession session = itemEditSessions.get(player.getUniqueId());
        if (session == null || !session.matches(holder)) {
            return;
        }
        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        BookField field = BookField.fromPanelSlot(event.getRawSlot());
        if (field != null) {
            openFieldBook(player, session, field);
            return;
        }
        if (event.getRawSlot() != BookField.SAVE_SLOT) {
            return;
        }

        EditResult result = saveItemPanel(session);
        player.sendMessage(LayoutMenuItem.colorize(result.message()));
        if (result.success()) {
            itemEditSessions.remove(player.getUniqueId(), session);
            openMainEditor(player, session.menuName());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if ((topInventory.getHolder() instanceof MenuEditorHolder || topInventory.getHolder() instanceof ItemEditHolder)
                && event.getRawSlots().stream().anyMatch(slot -> slot < topInventory.getSize())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        FieldBookSession session = pendingBookEdits.remove(playerId);
        if (session == null) {
            return;
        }

        transitioningInventoryViews.add(playerId);
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                restoreHeldItem(event.getPlayer(), session);
                ItemEditSession itemSession = itemEditSessions.get(playerId);
                if (itemSession == null || !itemSession.matches(session.menuName(), session.slot())
                        || !event.getPlayer().isOnline()) {
                    return;
                }
                applyBookField(itemSession, session.field(), event.getNewBookMeta().getPages());
                openItemPanelInventory(event.getPlayer(), itemSession);
            } finally {
                transitioningInventoryViews.remove(playerId);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FieldBookSession session = pendingBookEdits.remove(event.getPlayer().getUniqueId());
        if (session != null) {
            restoreHeldItem(event.getPlayer(), session);
        }
        itemEditSessions.remove(event.getPlayer().getUniqueId());
        transitioningInventoryViews.remove(event.getPlayer().getUniqueId());
        activeEditors.entrySet().removeIf(entry -> entry.getValue().equals(event.getPlayer().getUniqueId()));
    }

    public EditResult setTitle(String menuName, String title) {
        if (title == null || title.isBlank()) {
            return failure("editor.error.title_empty");
        }
        return mutate(menuName, config -> {
            if (menuManager.getMenuFormat(menuName) == MenuFormat.DELUXE) {
                config.set("menu_title", title);
            } else {
                config.set("Title", title);
            }
        }, message("editor.title_saved"));
    }

    public EditResult setPermission(String menuName, String permission) {
        return mutate(menuName, config -> {
            String value = permission == null || permission.equalsIgnoreCase("none") ? null : permission;
            if (menuManager.getMenuFormat(menuName) == MenuFormat.DELUXE) {
                config.set("open_permission", value);
            } else {
                config.set("Settings.permission", value);
            }
        }, message("editor.permission_saved"));
    }

    public EditResult setSize(String menuName, int size) {
        if (size < 9 || size > 54 || size % 9 != 0) {
            return failure("editor.error.size_range");
        }
        if (menuManager.getMenuFormat(menuName) != MenuFormat.DELUXE) {
            return failure("editor.error.trmenu_size");
        }
        return mutate(menuName, config -> config.set("size", size), message("editor.size_saved"));
    }

    public EditResult setItem(String menuName, int slot, String materialName, int amount, String displayName) {
        Material material = Material.matchMaterial(materialName == null ? "" : materialName);
        if (material == null || material.isAir()) {
            return failure("editor.error.unknown_material", materialName);
        }
        if (amount < 1 || amount > material.getMaxStackSize()) {
            return failure("editor.error.amount_range", material.getMaxStackSize());
        }

        return mutateItem(menuName, slot, true, (config, item, path, format) -> {
            item.setMaterial(material.name());
            item.setAmount(amount);
            item.setName(displayName == null ? "" : displayName);
            writeItem(config, path, item, format);
        }, message("editor.item_saved"));
    }

    public EditResult removeItem(String menuName, int slot) {
        return mutateItem(menuName, slot, false, (config, item, path, format) -> {
            config.set(path, null);
            if (format == MenuFormat.TRMENU) {
                clearTrMenuLayoutSlot(config, slot);
            }
        }, message("editor.item_removed"));
    }

    public EditResult editLore(String menuName, int slot, String operation, String value) {
        return mutateItem(menuName, slot, false, (config, item, path, format) -> {
            List<String> lore = new ArrayList<>(item.getLore());
            switch (operation.toLowerCase(Locale.ROOT)) {
                case "set" -> {
                    lore.clear();
                    if (value != null && !value.isBlank()) {
                        lore.add(value);
                    }
                }
                case "add" -> {
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException(message("editor.error.lore_required"));
                    }
                    lore.add(value);
                }
                case "remove" -> lore.remove(parseIndex(value, lore.size(), "lore"));
                case "clear" -> lore.clear();
                default -> throw new IllegalArgumentException(message("editor.error.lore_operation"));
            }
            item.setLore(lore);
            writeItem(config, path, item, format);
        }, message("editor.lore_saved"));
    }

    public EditResult editAction(String menuName, int slot, String type, String operation, String value) {
        String normalizedType = normalizeActionType(type);
        if (normalizedType == null) {
            return failure("editor.error.action_type");
        }
        return mutateItem(menuName, slot, false, (config, item, path, format) -> {
            Map<String, List<String>> allActions = new HashMap<>(item.getActions());
            List<String> actions = new ArrayList<>(allActions.getOrDefault(normalizedType, List.of()));
            switch (operation.toLowerCase(Locale.ROOT)) {
                case "set" -> {
                    actions.clear();
                    if (value != null && !value.isBlank()) {
                        actions.add(value);
                    }
                }
                case "add" -> {
                    if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException(message("editor.error.action_required"));
                    }
                    actions.add(value);
                }
                case "remove" -> actions.remove(parseIndex(value, actions.size(), "action"));
                case "clear" -> actions.clear();
                default -> throw new IllegalArgumentException(message("editor.error.action_operation"));
            }
            item.setActions(normalizedType, actions);
            writeItem(config, path, item, format);
        }, message("editor.actions_saved"));
    }

    private void openMainEditor(Player player, String menuName) {
        LayoutMenuData menu = menuManager.getMenu(menuName);
        if (menu == null) {
            activeEditors.remove(menuName.toLowerCase(Locale.ROOT), player.getUniqueId());
            itemEditSessions.remove(player.getUniqueId());
            send(player, "editor.menu_unloaded");
            return;
        }

        MenuEditorHolder holder = new MenuEditorHolder(menu.getName(), player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, menu.getInventorySize(), message("editor.title", menu.getName()));
        holder.setInventory(inventory);
        for (LayoutMenuItem item : menu.getItems().values()) {
            if (item.getSlot() >= 0 && item.getSlot() < inventory.getSize()) {
                inventory.setItem(item.getSlot(), item.toItemStack());
            }
        }
        openInventory(player, inventory);
    }

    private void openItemPanel(Player player, String menuName, int slot, ItemStack selectedItem) {
        LayoutMenuData menu = menuManager.getMenu(menuName);
        if (menu == null) {
            send(player, "editor.menu_unloaded");
            return;
        }

        BookFields fields = BookFields.from(selectedItem);
        LayoutMenuItem existing = menu.getItemAtSlot(slot);
        if (existing != null) {
            existing.getActions().forEach((type, actions) ->
                    fields.actions.put(type, new ArrayList<>(actions)));
        }
        ItemEditSession session = new ItemEditSession(menu.getName(), slot, fields);
        itemEditSessions.put(player.getUniqueId(), session);
        openItemPanelInventory(player, session);
    }

    private void openItemPanelInventory(Player player, ItemEditSession session) {
        ItemEditHolder holder = new ItemEditHolder(session.menuName(), player.getUniqueId(), session.slot());
        Inventory inventory = Bukkit.createInventory(holder, 27, message("editor.panel.title", session.slot()));
        holder.setInventory(inventory);

        ItemStack filler = panelItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int index = 0; index < inventory.getSize(); index++) {
            inventory.setItem(index, filler);
        }
        inventory.setItem(4, previewItem(session.fields()));
        inventory.setItem(BookField.NAME.panelSlot(), panelItem(Material.WRITABLE_BOOK,
                message("editor.panel.name"), message("editor.panel.name_lore")));
        inventory.setItem(BookField.LORE.panelSlot(), panelItem(Material.WRITABLE_BOOK,
                message("editor.panel.lore"), message("editor.panel.lore_lore")));
        inventory.setItem(BookField.LEFT.panelSlot(), panelItem(Material.WRITABLE_BOOK,
                message("editor.panel.left"), message("editor.panel.left_lore")));
        inventory.setItem(BookField.RIGHT.panelSlot(), panelItem(Material.WRITABLE_BOOK,
                message("editor.panel.right"), message("editor.panel.right_lore")));
        inventory.setItem(BookField.ALL.panelSlot(), panelItem(Material.WRITABLE_BOOK,
                message("editor.panel.all"), message("editor.panel.all_lore")));
        inventory.setItem(BookField.SAVE_SLOT, panelItem(Material.EMERALD_BLOCK,
                message("editor.panel.save"), message("editor.panel.save_lore")));
        openInventory(player, inventory);
    }

    private void openInventory(Player player, Inventory inventory) {
        UUID playerId = player.getUniqueId();
        transitioningInventoryViews.add(playerId);
        try {
            player.openInventory(inventory);
        } finally {
            transitioningInventoryViews.remove(playerId);
        }
    }

    private ItemStack panelItem(Material material, String title, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            if (lore != null && !lore.isBlank()) {
                meta.setLore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack previewItem(BookFields fields) {
        LayoutMenuItem preview = new LayoutMenuItem();
        preview.setMaterial(fields.material);
        preview.setAmount(fields.amount);
        preview.setName(fields.name);
        preview.setLore(fields.lore);
        preview.setGlowing(fields.glowing);
        preview.setSkullOwner(fields.skullOwner);
        return preview.toItemStack();
    }

    private void openFieldBook(Player player, ItemEditSession itemSession, BookField field) {
        if (pendingBookEdits.containsKey(player.getUniqueId())) {
            send(player, "editor.finish_book_first");
            return;
        }

        int heldSlot = player.getInventory().getHeldItemSlot();
        ItemStack previousHeldItem = player.getInventory().getItem(heldSlot);
        FieldBookSession session = new FieldBookSession(itemSession.menuName(), itemSession.slot(), field, heldSlot,
                previousHeldItem == null ? null : previousHeldItem.clone());
        pendingBookEdits.put(player.getUniqueId(), session);

        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta instanceof BookMeta bookMeta) {
            bookMeta.setDisplayName(message(field.bookTitleKey()));
            bookMeta.setPages(createFieldBookPages(itemSession.fields(), field));
            book.setItemMeta(bookMeta);
        }
        send(player, "editor.book_input_tip");
        player.getInventory().setItem(heldSlot, book);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (pendingBookEdits.get(player.getUniqueId()) != session || !player.isOnline()) {
                return;
            }
            if (!sendOpenWritableBookPacket(player)) {
                pendingBookEdits.remove(player.getUniqueId(), session);
                restoreHeldItem(player, session);
                send(player, "editor.book_open_failed");
            }
        });
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingBookEdits.remove(player.getUniqueId(), session)) {
                restoreHeldItem(player, session);
                if (player.isOnline() && itemEditSessions.get(player.getUniqueId()) != null) {
                    send(player, "editor.timeout");
                    openItemPanelInventory(player, itemEditSessions.get(player.getUniqueId()));
                }
            }
        }, 2400L);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean sendOpenWritableBookPacket(Player player) {
        try {
            Class<?> handClass = Class.forName("net.minecraft.world.InteractionHand");
            Object mainHand = Enum.valueOf((Class<? extends Enum>) handClass.asSubclass(Enum.class), "MAIN_HAND");
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundOpenBookPacket");
            Object packet = packetClass.getConstructor(handClass).newInstance(mainHand);

            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Field connectionField = findField(handle.getClass(), "connection");
            if (connectionField == null) {
                throw new NoSuchFieldException("connection");
            }
            connectionField.setAccessible(true);
            Object connection = connectionField.get(handle);
            if (connection == null) {
                throw new IllegalStateException("Player connection is unavailable");
            }

            Method sendMethod = null;
            for (Method method : connection.getClass().getMethods()) {
                if (method.getName().equals("send") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(packetClass)) {
                    sendMethod = method;
                    break;
                }
            }
            if (sendMethod == null) {
                throw new NoSuchMethodException("send(Packet)");
            }
            sendMethod.invoke(connection, packet);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().warning("Unable to open writable editor book: " + exception.getMessage());
            return false;
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private List<String> createFieldBookPages(BookFields fields, BookField field) {
        String content = switch (field) {
            case NAME -> fields.name;
            case LORE -> String.join("\n", fields.lore);
            case LEFT, RIGHT, ALL -> String.join("\n", fields.actions
                    .getOrDefault(field.actionType(), List.of()).stream()
                    .map(this::toEditableAction).toList());
        };
        return splitBookPages(content);
    }

    private String toEditableAction(String action) {
        String lower = action.toLowerCase(Locale.ROOT);
        if (lower.startsWith("command:")) {
            return "/" + action.substring(8).trim();
        }
        if (lower.startsWith("console:") || lower.startsWith("op:")) {
            return message("editor.book.console_prefix") + action.substring(action.indexOf(':') + 1).trim();
        }
        if (lower.equals("close")) {
            return message("editor.book.close_action");
        }
        return action;
    }

    private void applyBookField(ItemEditSession session, BookField field, List<String> pages) {
        List<String> lines = bookLines(pages);
        switch (field) {
            case NAME -> session.fields().name = String.join(" ", lines).trim();
            case LORE -> {
                session.fields().lore.clear();
                session.fields().lore.addAll(lines);
            }
            case LEFT, RIGHT, ALL -> session.fields().actions.put(field.actionType(), lines.stream()
                    .map(MenuEditor::normalizeBookAction).toList());
        }
    }

    private static List<String> bookLines(List<String> pages) {
        List<String> lines = new ArrayList<>();
        for (String page : pages) {
            for (String line : page.lines().toList()) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
        }
        return lines;
    }

    private static void restoreHeldItem(Player player, FieldBookSession session) {
        player.getInventory().setItem(session.heldSlot(), session.previousHeldItem());
    }

    private EditResult saveItemPanel(ItemEditSession session) {
        BookFields fields = session.fields();
        return mutateItem(session.menuName(), session.slot(), true, (config, item, path, format) -> {
            Material material = Material.matchMaterial(fields.material);
            if (material == null || material.isAir()) {
                throw new IllegalArgumentException(message("editor.error.unknown_material", fields.material));
            }
            if (fields.amount < 1 || fields.amount > material.getMaxStackSize()) {
                throw new IllegalArgumentException(message("editor.error.amount_range", material.getMaxStackSize()));
            }
            item.setMaterial(material.name());
            item.setAmount(fields.amount);
            item.setName(fields.name);
            item.setLore(fields.lore);
            item.setGlowing(fields.glowing);
            item.setSkullOwner(fields.skullOwner);
            fields.actions.forEach(item::setActions);
            writeItem(config, path, item, format);
        }, message("editor.slot_saved", session.slot()));
    }

    private static List<String> splitBookPages(String content) {
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        for (String line : content.lines().toList()) {
            if (page.length() > 0 && page.length() + line.length() + 1 > 240) {
                pages.add(page.toString());
                page.setLength(0);
            }
            page.append(line).append('\n');
        }
        if (page.length() > 0) {
            pages.add(page.toString());
        }
        return pages.isEmpty() ? List.of("") : pages;
    }

    private static String normalizeBookAction(String value) {
        String action = value.trim();
        String localizedAction = ActionEngine.normalizeActionAliases(action);
        if (!localizedAction.equals(action) || isExplicitAction(localizedAction)) {
            return localizedAction;
        }
        return "command: " + (action.startsWith("/") ? action.substring(1) : action);
    }

    private static boolean isExplicitAction(String action) {
        String lower = action.toLowerCase(Locale.ROOT);
        return lower.equals("close")
                || lower.startsWith("command:")
                || lower.startsWith("console:")
                || lower.startsWith("op:")
                || lower.startsWith("tell:")
                || lower.startsWith("message:")
                || lower.startsWith("chat:")
                || lower.startsWith("menu:")
                || lower.startsWith("sound:")
                || lower.startsWith("catcher:")
                || lower.startsWith("book:");
    }

    private EditResult mutateItem(String menuName, int slot, boolean createWhenMissing, ItemMutation mutation, String successMessage) {
        LayoutMenuData menu = menuManager.getMenu(menuName);
        if (menu == null) {
            return failure("editor.error.menu_not_found");
        }
        if (slot < 0 || slot >= menu.getInventorySize()) {
            return failure("editor.error.slot_range", menu.getInventorySize() - 1);
        }
        return mutate(menuName, config -> {
            MenuFormat format = menuManager.getMenuFormat(menuName);
            if (format == MenuFormat.TRMENU) {
                config.set("layout", materializeTrMenuLayout(menu, slot));
            }
            LayoutMenuItem item = menu.getItemAtSlot(slot);
            String path;
            if (item == null) {
                if (!createWhenMissing) {
                    throw new IllegalArgumentException(message("editor.error.no_item", slot));
                }
                CreatedItem created = createItem(config, menu, format, slot);
                item = created.item();
                path = created.path();
            } else {
                path = item.getSourceKey();
            }
            mutation.apply(config, item, path, format);
        }, successMessage);
    }

    private EditResult mutate(String menuName, ConfigMutation mutation, String successMessage) {
        File file = menuManager.getMenuFile(menuName);
        if (file == null) {
            return failure("editor.error.menu_not_found");
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            mutation.apply(config);
            return menuManager.saveMenuConfiguration(menuName, config)
                    ? EditResult.success(successMessage)
                    : failure("editor.error.write_failed");
        } catch (IllegalArgumentException exception) {
            return new EditResult(false, exception.getMessage());
        }
    }

    private CreatedItem createItem(YamlConfiguration config, LayoutMenuData menu, MenuFormat format, int slot) {
        LayoutMenuItem item = new LayoutMenuItem();
        item.setSlot(slot);
        if (format == MenuFormat.DELUXE) {
            String prefix = config.isConfigurationSection("items") ? "items." : "";
            String path = prefix + "editor_slot_" + slot;
            item.setSourceKey(path);
            return new CreatedItem(item, path);
        }

        char icon = nextIcon(config, menu);
        item.setIconChar(icon);
        String path = "Icons." + icon;
        item.setSourceKey(path);
        setTrMenuLayoutSlot(config, slot, icon);
        return new CreatedItem(item, path);
    }

    private void writeItem(YamlConfiguration config, String path, LayoutMenuItem item, MenuFormat format) {
        String material = item.getMaterial();
        int amount = item.getAmount();
        String name = toConfigText(item.getName());
        List<String> lore = item.getLore().stream().map(MenuEditor::toConfigText).toList();
        if (format == MenuFormat.DELUXE) {
            config.set(path + ".slot", item.getSlot());
            config.set(path + ".material", material);
            config.set(path + ".amount", amount);
            config.set(path + ".display_name", name);
            config.set(path + ".lore", lore);
            config.set(path + ".glow", item.isGlowing());
            config.set(path + ".skull_owner", item.getSkullOwner());
            writeDeluxeActions(config, path, item);
            return;
        }
        String display = path + ".display";
        config.set(display + ".material", material);
        config.set(display + ".amount", amount);
        config.set(display + ".name", name);
        config.set(display + ".lore", lore);
        config.set(display + ".glow", item.isGlowing());
        config.set(display + ".skull_owner", item.getSkullOwner());
        config.set(path + ".actions", null);
        item.getActions().forEach((type, actions) -> config.set(path + ".actions." + type, actions));
    }

    private void writeDeluxeActions(YamlConfiguration config, String path, LayoutMenuItem item) {
        config.set(path + ".left_click_commands", toDeluxeCommands(item.getActions().get("left")));
        config.set(path + ".right_click_commands", toDeluxeCommands(item.getActions().get("right")));
        config.set(path + ".click_commands", toDeluxeCommands(item.getActions().get("all")));
    }

    private List<String> toDeluxeCommands(List<String> actions) {
        if (actions == null) {
            return List.of();
        }
        return actions.stream().map(this::toDeluxeCommand).toList();
    }

    private String toDeluxeCommand(String action) {
        String trimmed = ActionEngine.normalizeActionAliases(action.trim());
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith("command:")) {
            return "[player] " + trimmed.substring(8).trim();
        }
        if (lower.startsWith("console:") || lower.startsWith("op:")) {
            return "[console] " + trimmed.substring(trimmed.indexOf(':') + 1).trim();
        }
        if (lower.equals("close")) {
            return "[close]";
        }
        if (lower.startsWith("menu:")) {
            return "[open] " + trimmed.substring(5).trim();
        }
        if (lower.startsWith("tell:") || lower.startsWith("message:")) {
            return "[message] " + trimmed.substring(trimmed.indexOf(':') + 1).trim();
        }
        return trimmed;
    }

    private char nextIcon(YamlConfiguration config, LayoutMenuData menu) {
        Set<Character> used = new HashSet<>(menu.getLayoutSlots().keySet());
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (char candidate : alphabet.toCharArray()) {
            if (!used.contains(candidate) && !config.contains("Icons." + candidate)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(message("editor.error.no_icon"));
    }

    private static void setTrMenuLayoutSlot(YamlConfiguration config, int slot, char icon) {
        int rowIndex = slot / 9;
        int column = slot % 9;
        List<String> layout = new ArrayList<>(config.getStringList("layout"));
        while (layout.size() <= rowIndex) {
            layout.add("         ");
        }
        String row = layout.get(rowIndex);
        StringBuilder normalized = new StringBuilder(row == null ? "" : row);
        if (normalized.length() > 9) {
            normalized.setLength(9);
        }
        while (normalized.length() < 9) {
            normalized.append(' ');
        }
        normalized.setCharAt(column, icon);
        layout.set(rowIndex, normalized.toString());
        config.set("layout", layout);
    }

    private static List<String> materializeTrMenuLayout(LayoutMenuData menu, int minimumSlot) {
        int rowCount = Math.max(menu.getRawLayout().size(), (minimumSlot / 9) + 1);
        List<StringBuilder> rows = new ArrayList<>();
        for (int row = 0; row < rowCount; row++) {
            rows.add(new StringBuilder("         "));
        }
        menu.getLayoutSlots().forEach((icon, slot) -> {
            if (slot >= 0 && slot < rowCount * 9) {
                rows.get(slot / 9).setCharAt(slot % 9, icon);
            }
        });
        return rows.stream().map(StringBuilder::toString).toList();
    }

    private static void clearTrMenuLayoutSlot(YamlConfiguration config, int slot) {
        List<String> layout = new ArrayList<>(config.getStringList("layout"));
        int rowIndex = slot / 9;
        int column = slot % 9;
        if (rowIndex >= layout.size()) {
            return;
        }
        StringBuilder row = new StringBuilder(layout.get(rowIndex));
        while (row.length() < 9) {
            row.append(' ');
        }
        if (row.length() > 9) {
            row.setLength(9);
        }
        row.setCharAt(column, ' ');
        layout.set(rowIndex, row.toString());
        config.set("layout", layout);
    }

    private static boolean isAir(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }

    private int parseIndex(String value, int size, String type) {
        try {
            int index = Integer.parseInt(value);
            if (index < 0 || index >= size) {
                throw new IllegalArgumentException(message("editor.error." + type + "_index_range"));
            }
            return index;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(message("editor.error." + type + "_index_number"));
        }
    }

    private static String normalizeActionType(String type) {
        if (type == null) {
            return null;
        }
        String normalized = type.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "left", "\u5de6\u952e" -> "left";
            case "right", "\u53f3\u952e" -> "right";
            case "all", "\u5168\u90e8", "\u901a\u7528" -> "all";
            default -> null;
        };
    }

    private static String toConfigText(String value) {
        return value == null ? "" : value.replace(ChatColor.COLOR_CHAR, '&');
    }

    private String message(String key, Object... arguments) {
        return plugin.getLanguageManager().getMessage(key, arguments);
    }

    private EditResult success(String key, Object... arguments) {
        return EditResult.success(message(key, arguments));
    }

    private EditResult failure(String key, Object... arguments) {
        return new EditResult(false, message(key, arguments));
    }

    private void send(Player player, String key, Object... arguments) {
        player.sendMessage(message(key, arguments));
    }

    @FunctionalInterface
    private interface ConfigMutation {
        void apply(YamlConfiguration config);
    }

    @FunctionalInterface
    private interface ItemMutation {
        void apply(YamlConfiguration config, LayoutMenuItem item, String path, MenuFormat format);
    }

    private record CreatedItem(LayoutMenuItem item, String path) {
    }

    private record ItemEditSession(String menuName, int slot, BookFields fields) {
        private boolean matches(ItemEditHolder holder) {
            return menuName.equalsIgnoreCase(holder.getMenuName()) && slot == holder.getSlot();
        }

        private boolean matches(String otherMenuName, int otherSlot) {
            return menuName.equalsIgnoreCase(otherMenuName) && slot == otherSlot;
        }
    }

    private record FieldBookSession(String menuName, int slot, BookField field, int heldSlot,
                                    ItemStack previousHeldItem) {
    }

    private enum BookField {
        NAME(10, null, "editor.book.name_title"),
        LORE(11, null, "editor.book.lore_title"),
        LEFT(12, "left", "editor.book.left_title"),
        RIGHT(13, "right", "editor.book.right_title"),
        ALL(14, "all", "editor.book.all_title");

        private static final int SAVE_SLOT = 22;

        private final int panelSlot;
        private final String actionType;
        private final String bookTitleKey;

        BookField(int panelSlot, String actionType, String bookTitleKey) {
            this.panelSlot = panelSlot;
            this.actionType = actionType;
            this.bookTitleKey = bookTitleKey;
        }

        private int panelSlot() {
            return panelSlot;
        }

        private String actionType() {
            return actionType;
        }

        private String bookTitleKey() {
            return bookTitleKey;
        }

        private static BookField fromPanelSlot(int slot) {
            for (BookField field : values()) {
                if (field.panelSlot == slot) {
                    return field;
                }
            }
            return null;
        }
    }

    private static final class BookFields {
        private String material;
        private int amount = 1;
        private String name = "";
        private final List<String> lore = new ArrayList<>();
        private final Map<String, List<String>> actions = new HashMap<>();
        private boolean glowing;
        private String skullOwner;

        private static BookFields from(ItemStack itemStack) {
            LayoutMenuItem display = new LayoutMenuItem();
            display.updateDisplayFrom(itemStack);

            BookFields fields = new BookFields();
            fields.material = display.getMaterial();
            fields.amount = display.getAmount();
            fields.name = toConfigText(display.getName());
            fields.lore.addAll(display.getLore().stream().map(MenuEditor::toConfigText).toList());
            fields.glowing = display.isGlowing();
            fields.skullOwner = display.getSkullOwner();
            return fields;
        }
    }

    public record EditResult(boolean success, String message) {
        public static EditResult success(String message) {
            return new EditResult(true, message);
        }

        public static EditResult failure(String message) {
            return new EditResult(false, "&c" + message);
        }
    }
}

package gofd.gFMenu.checkin;

import gofd.gFMenu.GFMenu;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the daily check-in flow: per-player state, rewards, and YAML persistence.
 * Runs on the main thread only (menu clicks trigger it), so no locking is required.
 */
public final class CheckinManager {

    private final GFMenu plugin;
    private final File dataFolder;
    private final Map<UUID, CheckinData> cache = new ConcurrentHashMap<>();
    private final Clock clock;
    private CheckinConfig config;

    public CheckinManager(GFMenu plugin) {
        this(plugin, Clock.systemDefaultZone());
    }

    /** Package-private clock injection keeps cross-day logic testable. */
    CheckinManager(GFMenu plugin, Clock clock) {
        this.plugin = plugin;
        this.clock = clock;
        this.dataFolder = new File(plugin.getDataFolder(), "data/players");
        reloadConfig();
    }

    public void reloadConfig() {
        File file = new File(plugin.getDataFolder(), "checkin.yml");
        if (!file.exists()) {
            plugin.saveResource("checkin.yml", false);
        }
        config = CheckinConfig.load(file);
    }

    /** Runs the check-in flow for a player; safe to call repeatedly on the same day. */
    public void checkIn(Player player) {
        UUID id = player.getUniqueId();
        CheckinData current = cache.computeIfAbsent(id, this::load);
        CheckinData.CheckInResult result = current.checkIn(LocalDate.now(clock));
        if (result.alreadyCheckedIn()) {
            player.sendMessage(message("checkin_already"));
            return;
        }
        CheckinData updated = result.updated();
        // Persist before granting rewards: a failed write leaves the cache untouched so the
        // player can retry, and a successful write guarantees the day is never double-claimed.
        if (!save(id, updated)) {
            player.sendMessage(message("checkin_failed"));
            return;
        }
        cache.put(id, updated);
        executeRewards(player, updated.getStreak());
        player.sendMessage(message("checkin_success", updated.getStreak(), updated.getTotal()));
    }

    private void executeRewards(Player player, int streak) {
        plugin.getMenuManager().getActionEngine().executeActions(player, config.dailyRewards());
        if (!config.rewardsForStreak(streak).isEmpty()) {
            plugin.getMenuManager().getActionEngine().executeActions(player, config.rewardsForStreak(streak));
            player.sendMessage(message("checkin_streak_award", streak));
        }
    }

    /** Placeholder support: current consecutive check-in days. */
    public int getStreak(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::load).getStreak();
    }

    /** Placeholder support: total check-ins ever. */
    public int getTotal(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::load).getTotal();
    }

    /** Placeholder support: whether the player can still check in today. */
    public boolean canCheckIn(UUID playerId) {
        return cache.computeIfAbsent(playerId, this::load).canCheckIn(LocalDate.now(clock));
    }

    private CheckinData load(UUID playerId) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile(playerId));
        LocalDate last = null;
        String raw = yaml.getString("last-checkin");
        if (raw != null && !raw.isBlank()) {
            try {
                last = LocalDate.parse(raw);
            } catch (DateTimeParseException exception) {
                plugin.getLogger().warning("Invalid check-in date for " + playerId + ": " + raw);
            }
        }
        return CheckinData.of(last, yaml.getInt("streak", 0), yaml.getInt("total", 0));
    }

    private boolean save(UUID playerId, CheckinData data) {
        try {
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getLogger().warning("Unable to create check-in data folder: " + dataFolder.getAbsolutePath());
                return false;
            }
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.set("last-checkin", data.getLastCheckin() == null ? "" : data.getLastCheckin().toString());
            yaml.set("streak", data.getStreak());
            yaml.set("total", data.getTotal());
            yaml.save(dataFile(playerId));
            return true;
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save check-in data for " + playerId + ": " + exception.getMessage());
            return false;
        }
    }

    private File dataFile(UUID playerId) {
        return new File(dataFolder, playerId + ".yml");
    }

    private String message(String key, Object... arguments) {
        return plugin.getLanguageManager().getMessage(key, arguments);
    }
}

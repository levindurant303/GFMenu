package gofd.gFMenu.checkin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

/** In-memory view of checkin.yml; rewards reuse the existing menu action syntax. */
public final class CheckinConfig {

    private final List<String> dailyRewards;
    private final TreeMap<Integer, List<String>> streakRewards;

    private CheckinConfig(List<String> dailyRewards, TreeMap<Integer, List<String>> streakRewards) {
        this.dailyRewards = List.copyOf(dailyRewards);
        this.streakRewards = new TreeMap<>();
        streakRewards.forEach((day, rewards) -> this.streakRewards.put(day, List.copyOf(rewards)));
    }

    /** Loads the file with safe defaults when keys are missing or malformed. */
    public static CheckinConfig load(File file) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> daily = yaml.getStringList("daily-rewards");
        TreeMap<Integer, List<String>> streak = new TreeMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("streak-rewards");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int day = parseDay(key);
                if (day > 0) {
                    streak.put(day, section.getStringList(key));
                }
            }
        }
        return new CheckinConfig(daily, streak);
    }

    public List<String> dailyRewards() {
        return dailyRewards;
    }

    /** Reward for reaching exactly {@code streak} days, or empty when no milestone is configured. */
    public List<String> rewardsForStreak(int streak) {
        List<String> rewards = streakRewards.get(streak);
        return rewards == null ? List.of() : rewards;
    }

    private static int parseDay(String key) {
        try {
            return Integer.parseInt(key.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}

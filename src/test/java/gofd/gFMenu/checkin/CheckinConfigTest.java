package gofd.gFMenu.checkin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckinConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsDailyAndStreakRewards() throws IOException {
        Path file = tempDir.resolve("checkin.yml");
        Files.writeString(file, """
                daily-rewards:
                  - "console: give %player% diamond 1"
                streak-rewards:
                  7:
                    - "console: give %player% emerald 1"
                  30:
                    - "console: give %player% netherite_ingot 1"
                """);

        CheckinConfig config = CheckinConfig.load(file.toFile());

        assertEquals(List.of("console: give %player% diamond 1"), config.dailyRewards());
        assertTrue(config.rewardsForStreak(3).isEmpty());
        assertEquals(List.of("console: give %player% emerald 1"), config.rewardsForStreak(7));
        assertTrue(config.rewardsForStreak(15).isEmpty());
        assertEquals(List.of("console: give %player% netherite_ingot 1"), config.rewardsForStreak(30));
        assertTrue(config.rewardsForStreak(45).isEmpty());
    }

    @Test
    void milestoneRewardIsDeliveredOnlyOnTheExactDay() throws IOException {
        Path file = tempDir.resolve("checkin.yml");
        Files.writeString(file, """
                daily-rewards:
                  - "console: give %player% diamond 1"
                streak-rewards:
                  7:
                    - "console: give %player% emerald 1"
                """);

        CheckinConfig config = CheckinConfig.load(file.toFile());

        // Day 8..29 must NOT re-grant the day-7 milestone on every check-in.
        assertTrue(config.rewardsForStreak(8).isEmpty());
        assertTrue(config.rewardsForStreak(29).isEmpty());
        assertEquals(List.of("console: give %player% emerald 1"), config.rewardsForStreak(7));
    }

    @Test
    void missingFileYieldsEmptyRewards() throws IOException {
        Path file = tempDir.resolve("nonexistent.yml");

        CheckinConfig config = CheckinConfig.load(file.toFile());

        assertTrue(config.dailyRewards().isEmpty());
        assertTrue(config.rewardsForStreak(10).isEmpty());
    }

    @Test
    void malformedStreakKeysAreIgnored() throws IOException {
        Path file = tempDir.resolve("checkin.yml");
        Files.writeString(file, """
                streak-rewards:
                  7:
                    - "console: give %player% emerald 1"
                  bad: 5
                """);

        CheckinConfig config = CheckinConfig.load(file.toFile());

        assertEquals(List.of("console: give %player% emerald 1"), config.rewardsForStreak(7));
    }
}

package gofd.gFMenu.checkin;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable per-player check-in state. All date/streak logic is pure so it can be
 * unit-tested without a running server; {@link #checkIn} never mutates this instance.
 */
public final class CheckinData {

    private final LocalDate lastCheckin;
    private final int streak;
    private final int total;

    private CheckinData(LocalDate lastCheckin, int streak, int total) {
        this.lastCheckin = lastCheckin;
        this.streak = streak;
        this.total = total;
    }

    /** Fresh state for a player who has never checked in. */
    public static CheckinData empty() {
        return new CheckinData(null, 0, 0);
    }

    /** Rebuilds state from persisted values; clamps negative numbers defensively. */
    public static CheckinData of(LocalDate lastCheckin, int streak, int total) {
        return new CheckinData(lastCheckin, Math.max(0, streak), Math.max(0, total));
    }

    /**
     * Applies a check-in for {@code today}.
     *
     * @return a result carrying the updated state; the state is identical when the
     *         player already checked in today
     */
    public CheckInResult checkIn(LocalDate today) {
        Objects.requireNonNull(today, "today");
        if (lastCheckin != null && lastCheckin.equals(today)) {
            return new CheckInResult(this, true, false);
        }
        boolean continued = lastCheckin != null && lastCheckin.plusDays(1).equals(today);
        CheckinData updated = new CheckinData(today, continued ? streak + 1 : 1, total + 1);
        return new CheckInResult(updated, false, continued);
    }

    /** Whether the player can still check in today. */
    public boolean canCheckIn(LocalDate today) {
        return lastCheckin == null || !lastCheckin.equals(today);
    }

    public LocalDate getLastCheckin() {
        return lastCheckin;
    }

    public int getStreak() {
        return streak;
    }

    public int getTotal() {
        return total;
    }

    /** Outcome of a single check-in attempt; the updated state replaces the old one on success. */
    public record CheckInResult(CheckinData updated, boolean alreadyCheckedIn, boolean continued) {
    }
}

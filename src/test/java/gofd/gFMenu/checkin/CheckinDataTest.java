package gofd.gFMenu.checkin;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckinDataTest {

    private static final LocalDate DAY1 = LocalDate.of(2026, 8, 26);
    private static final LocalDate DAY2 = LocalDate.of(2026, 8, 27);
    private static final LocalDate DAY3 = LocalDate.of(2026, 8, 28);

    @Test
    void firstCheckInStartsStreak() {
        CheckinData.CheckInResult result = CheckinData.empty().checkIn(DAY1);

        assertFalse(result.alreadyCheckedIn());
        assertFalse(result.continued());
        assertEquals(1, result.updated().getStreak());
        assertEquals(1, result.updated().getTotal());
        assertEquals(DAY1, result.updated().getLastCheckin());
    }

    @Test
    void consecutiveDayContinuesStreak() {
        CheckinData data = CheckinData.empty().checkIn(DAY1).updated();

        CheckinData.CheckInResult result = data.checkIn(DAY2);

        assertTrue(result.continued());
        assertEquals(2, result.updated().getStreak());
        assertEquals(2, result.updated().getTotal());
    }

    @Test
    void missingDayResetsStreak() {
        CheckinData data = CheckinData.empty().checkIn(DAY1).updated();

        CheckinData.CheckInResult result = data.checkIn(DAY3);

        assertFalse(result.continued());
        assertEquals(1, result.updated().getStreak());
        assertEquals(2, result.updated().getTotal());
    }

    @Test
    void sameDayIsIdempotent() {
        CheckinData data = CheckinData.empty().checkIn(DAY1).updated();

        CheckinData.CheckInResult result = data.checkIn(DAY1);

        assertTrue(result.alreadyCheckedIn());
        assertEquals(data.getStreak(), result.updated().getStreak());
        assertEquals(data.getTotal(), result.updated().getTotal());
        assertEquals(data.getLastCheckin(), result.updated().getLastCheckin());
    }

    @Test
    void canCheckInReflectsToday() {
        CheckinData data = CheckinData.empty().checkIn(DAY1).updated();

        assertFalse(data.canCheckIn(DAY1));
        assertTrue(data.canCheckIn(DAY2));
        assertTrue(CheckinData.empty().canCheckIn(DAY1));
    }
}

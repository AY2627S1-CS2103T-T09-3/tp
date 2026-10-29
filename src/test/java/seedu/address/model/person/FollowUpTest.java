package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class FollowUpTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);

    @Test
    public void dateValidation_checksCalendarAndExactFormat() {
        for (String date : new String[]{"2028-02-29", "0001-01-01", "9999-12-31"}) {
            assertTrue(FollowUp.isValidDate(date));
        }
        for (String date : new String[]{"", "2026-02-29", "2026-04-31", "2026-13-01", "2026-1-01",
            "0000-01-01", "+2026-01-01", "2026-10-29 extra", " 2026-10-29"}) {
            assertFalse(FollowUp.isValidDate(date));
            assertThrows(IllegalArgumentException.class, () -> new FollowUp(date, "note", false));
        }
    }

    @Test
    public void constructor_validatesNoteAndEmptyState() {
        assertEquals("Call", new FollowUp("2026-10-29", "  Call  ", false).getNote());
        assertTrue(FollowUp.isValidNote("a".repeat(200)));
        for (String note : new String[]{"a".repeat(201), "Call\nLater", "Call\tLater", "\r", "\u0000"}) {
            assertFalse(FollowUp.isValidNote(note));
            assertThrows(IllegalArgumentException.class, () -> new FollowUp("2026-10-29", note, false));
        }
        assertThrows(IllegalArgumentException.class, () -> new FollowUp("", "", true));
        assertThrows(NullPointerException.class, () -> new FollowUp(null, "", false));
        assertThrows(NullPointerException.class, () -> new FollowUp("", null, false));
    }

    @Test
    public void constructor_unicodeLineBreaks_rejectedBeforeTrimming() {
        for (String separator : new String[]{"\u2028", "\u2029"}) {
            for (String note : new String[]{separator, separator + "Call", "Call" + separator,
                "Call" + separator + "Later"}) {
                assertFalse(FollowUp.isValidNote(note));
                assertThrows(IllegalArgumentException.class, () -> new FollowUp("2026-10-29", note, false));
            }
        }
        String multilingualNote = "Call 日本語 — 确认时间";
        assertEquals(multilingualNote, new FollowUp("2026-10-29", multilingualNote, false).getNote());
    }

    @Test
    public void lifecycle_completePreservesDateAndNote() {
        FollowUp pending = new FollowUp("2026-10-28", "Call", false);
        FollowUp completed = pending.complete();
        assertTrue(pending.isOverdue(TODAY));
        assertFalse(pending.isCompleted());
        assertFalse(completed.isOverdue(TODAY));
        assertEquals(pending.getDate(), completed.getDate());
        assertEquals(pending.getNote(), completed.getNote());
        assertTrue(completed.isCompleted());
        assertFalse(FollowUp.empty().isScheduled());
        assertTrue(FollowUp.empty().getDate().isEmpty());
        assertThrows(IllegalStateException.class, () -> FollowUp.empty().complete());
    }

    @Test
    public void format_distinguishesEveryStatus() {
        assertEquals("", FollowUp.empty().format(TODAY));
        assertEquals("Follow-up: 2026-10-28 (overdue) — Call",
                new FollowUp("2026-10-28", "Call", false).format(TODAY));
        assertEquals("Follow-up: 2026-10-29 (today)", new FollowUp("2026-10-29", "", false).format(TODAY));
        assertEquals("Follow-up: 2026-10-30 (upcoming)", new FollowUp("2026-10-30", "", false).format(TODAY));
        assertEquals("Follow-up: 2026-10-28 (completed)", new FollowUp("2026-10-28", "", true).format(TODAY));
    }

    @Test
    public void filters_partitionPendingDatesAndExcludeCompleted() {
        FollowUp overdue = new FollowUp("2026-10-28", "", false);
        FollowUp today = new FollowUp("2026-10-29", "", false);
        FollowUp future = new FollowUp("2026-10-30", "", false);
        for (FollowUpFilter filter : FollowUpFilter.values()) {
            assertFalse(filter.matches(FollowUp.empty(), TODAY));
            assertEquals(filter == FollowUpFilter.ALL || filter == FollowUpFilter.PENDING
                    || filter == FollowUpFilter.OVERDUE, filter.matches(overdue, TODAY));
            assertEquals(filter == FollowUpFilter.ALL || filter == FollowUpFilter.PENDING
                    || filter == FollowUpFilter.TODAY, filter.matches(today, TODAY));
            assertEquals(filter == FollowUpFilter.ALL || filter == FollowUpFilter.PENDING
                    || filter == FollowUpFilter.UPCOMING, filter.matches(future, TODAY));
            assertEquals(filter == FollowUpFilter.ALL || filter == FollowUpFilter.COMPLETED,
                    filter.matches(overdue.complete(), TODAY));
        }
    }

    @Test
    public void equality_includesDateNoteAndCompletion() {
        FollowUp reminder = new FollowUp("2026-10-29", "Call", false);
        FollowUp equal = new FollowUp("2026-10-29", "Call", false);
        assertEquals(reminder, equal);
        assertEquals(reminder.hashCode(), equal.hashCode());
        assertNotEquals(reminder, reminder.complete());
        assertNotEquals(reminder, new FollowUp("2026-10-30", "Call", false));
        assertNotEquals(reminder, new FollowUp("2026-10-29", "Email", false));
        assertNotEquals(reminder, null);
    }
}

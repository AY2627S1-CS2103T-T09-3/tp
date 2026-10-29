package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    @Test
    public void previousAndNext_restoreDraftAndRespectBoundaries() {
        CommandHistory history = new CommandHistory();
        history.record("list");
        history.record("find Alice");

        CommandHistory.NavigationResult firstPrevious = history.previous("draft command");
        assertEquals("find Alice", firstPrevious.getText());
        assertTrue(firstPrevious.hasMoved());

        CommandHistory.NavigationResult secondPrevious = history.previous("find Alice");
        assertEquals("list", secondPrevious.getText());
        assertTrue(secondPrevious.hasMoved());

        CommandHistory.NavigationResult oldest = history.previous("list");
        assertEquals("list", oldest.getText());
        assertTrue(oldest.isAtBoundary());

        CommandHistory.NavigationResult next = history.next();
        assertEquals("find Alice", next.getText());
        assertTrue(next.hasMoved());

        CommandHistory.NavigationResult restoredDraft = history.next();
        assertEquals("draft command", restoredDraft.getText());
        assertTrue(restoredDraft.hasMoved());

        CommandHistory.NavigationResult newest = history.next();
        assertEquals("draft command", newest.getText());
        assertTrue(newest.isAtBoundary());
    }

    @Test
    public void failedCommands_areRetainedForCorrection() {
        CommandHistory history = new CommandHistory();
        history.record("stage 1 s/Unknown");

        assertEquals(1, history.getEntries().size());
        assertEquals("stage 1 s/Unknown", history.getEntries().get(0));
        assertFalse(history.getEntries().isEmpty());
    }
}

package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    @Test
    public void navigation_preservesDraftAndStopsAtBoundaries() {
        CommandHistory history = new CommandHistory();
        history.record("list");
        history.record("view 1");
        assertEquals("view 1", history.previous("unfinished draft"));
        assertEquals("list", history.previous("view 1"));
        assertEquals("list", history.previous("list"));
        assertEquals("view 1", history.next("list"));
        assertEquals("unfinished draft", history.next("view 1"));
        assertEquals("unfinished draft", history.next("unfinished draft"));
    }

    @Test
    public void attempts_areBoundedAndConsecutiveDuplicatesAreIgnored() {
        CommandHistory history = new CommandHistory(2);
        history.record("list");
        history.record("invalid command");
        history.record("invalid command");
        history.record("view 1");
        assertEquals("view 1", history.previous("draft"));
        assertEquals("invalid command", history.previous("view 1"));
        assertEquals("invalid command", history.previous("invalid command"));
        history.record("list");
        assertEquals("list", history.previous("new draft"));
        assertEquals("new draft", history.next("list"));
    }

    @Test
    public void emptyHistoryAndInvalidArguments_areHandled() {
        CommandHistory history = new CommandHistory();
        history.record("  ");
        assertEquals("draft", history.previous("draft"));
        assertEquals("draft", history.next("draft"));
        assertThrows(IllegalArgumentException.class, () -> new CommandHistory(0));
        assertThrows(NullPointerException.class, () -> history.record(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
        assertThrows(NullPointerException.class, () -> history.next(null));
    }
}

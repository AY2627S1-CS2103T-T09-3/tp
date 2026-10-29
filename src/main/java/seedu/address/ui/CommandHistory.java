package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/** Bounded, session-only command history with an independent unfinished draft. */
public class CommandHistory {
    private static final int DEFAULT_CAPACITY = 200;

    private final int capacity;
    private final List<String> entries = new ArrayList<>();
    private int cursor;
    private String draft = "";

    public CommandHistory() {
        this(DEFAULT_CAPACITY);
    }

    /** Creates history with a positive maximum number of entries. */
    public CommandHistory(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("History capacity must be positive");
        }
        this.capacity = capacity;
    }

    /** Records an attempted command, including failures, without consecutive duplicates. */
    public void record(String command) {
        requireNonNull(command);
        if (command.isBlank()) {
            return;
        }
        if (entries.isEmpty() || !entries.get(entries.size() - 1).equals(command)) {
            entries.add(command);
            if (entries.size() > capacity) {
                entries.remove(0);
            }
        }
        cursor = entries.size();
        draft = "";
    }

    /** Returns an older entry, remembering the draft when navigation first starts. */
    public String previous(String currentInput) {
        requireNonNull(currentInput);
        if (entries.isEmpty()) {
            return currentInput;
        }
        if (cursor == entries.size()) {
            draft = currentInput;
        }
        cursor = Math.max(0, cursor - 1);
        assert cursor >= 0 && cursor < entries.size();
        return entries.get(cursor);
    }

    /** Returns a newer entry or the original draft at the end of history. */
    public String next(String currentInput) {
        requireNonNull(currentInput);
        if (cursor == entries.size()) {
            return currentInput;
        }
        cursor++;
        return cursor == entries.size() ? draft : entries.get(cursor);
    }
}

package seedu.address.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores commands entered in the command box and provides predictable history navigation.
 *
 * <p>The class deliberately keeps the unfinished input separate from the stored commands. This
 * lets a user press Down after browsing older commands and recover exactly what was being typed.
 * Failed commands are stored as well because they are often the commands a user most wants to
 * correct.</p>
 */
public final class CommandHistory {

    private final List<String> entries = new ArrayList<>();
    private int cursor;
    private String unfinishedDraft = "";

    /** Creates an empty command history positioned after the newest entry. */
    public CommandHistory() {
        cursor = 0;
    }

    /**
     * Records a non-empty command and moves the navigation cursor to the draft position.
     *
     * @param command command text, including commands that later fail to execute
     */
    public void record(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        entries.add(command);
        cursor = entries.size();
        unfinishedDraft = "";
    }

    /**
     * Moves to the previous command.
     *
     * @param currentText text currently in the command box, captured as the unfinished draft when
     *                    navigation starts
     * @return the navigation result containing the text to display
     */
    public NavigationResult previous(String currentText) {
        if (entries.isEmpty()) {
            return NavigationResult.atBoundary(currentText);
        }
        if (cursor == entries.size()) {
            unfinishedDraft = currentText;
        }
        if (cursor > 0) {
            cursor--;
            return NavigationResult.moved(entries.get(cursor));
        }
        return NavigationResult.atBoundary(entries.get(0));
    }

    /**
     * Moves to the next command, returning the unfinished draft after the newest entry.
     *
     * @return the navigation result containing the text to display
     */
    public NavigationResult next() {
        if (entries.isEmpty()) {
            return NavigationResult.atBoundary(unfinishedDraft);
        }
        if (cursor < entries.size() - 1) {
            cursor++;
            return NavigationResult.moved(entries.get(cursor));
        }
        if (cursor == entries.size() - 1) {
            cursor = entries.size();
            return NavigationResult.moved(unfinishedDraft);
        }
        return NavigationResult.atBoundary(unfinishedDraft);
    }

    /** Returns an immutable snapshot of the commands currently stored. */
    public List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /** Returns true when the cursor is at the oldest stored command. */
    public boolean isAtOldest() {
        return !entries.isEmpty() && cursor == 0;
    }

    /** Returns true when the cursor is after the newest stored command. */
    public boolean isAtNewest() {
        return cursor == entries.size();
    }

    /** Resets navigation to the draft position without deleting stored commands. */
    public void resetNavigation() {
        cursor = entries.size();
        unfinishedDraft = "";
    }

    /** Describes a history navigation result. */
    public static final class NavigationResult {
        private final String text;
        private final boolean moved;
        private final boolean atBoundary;

        private NavigationResult(String text, boolean moved, boolean atBoundary) {
            this.text = text == null ? "" : text;
            this.moved = moved;
            this.atBoundary = atBoundary;
        }

        private static NavigationResult moved(String text) {
            return new NavigationResult(text, true, false);
        }

        private static NavigationResult atBoundary(String text) {
            return new NavigationResult(text, false, true);
        }

        public String getText() {
            return text;
        }

        public boolean hasMoved() {
            return moved;
        }

        public boolean isAtBoundary() {
            return atBoundary;
        }
    }
}

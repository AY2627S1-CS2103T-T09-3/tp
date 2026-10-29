package seedu.address.ui;

import static java.util.Objects.requireNonNull;

/** A completion edits one token range and preserves the remainder of the command verbatim. */
public final class CommandSuggestion {
    private final int start;
    private final int end;
    private final String replacement;
    private final String label;

    /** Creates a completion for a nonnegative, ordered range in the original input. */
    public CommandSuggestion(int start, int end, String replacement, String label) {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid completion range");
        }
        this.start = start;
        this.end = end;
        this.replacement = requireNonNull(replacement);
        this.label = requireNonNull(label);
    }

    /** Applies the token replacement without discarding arguments after the caret. */
    public String apply(String input) {
        requireNonNull(input);
        if (end > input.length()) {
            throw new IllegalArgumentException("Completion exceeds input length");
        }
        return input.substring(0, start) + replacement + input.substring(end);
    }

    public int getCaretPosition() {
        return start + replacement.length();
    }

    @Override
    public String toString() {
        return label;
    }
}

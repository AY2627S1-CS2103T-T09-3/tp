package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Produces command-box suggestions without changing command parsing or execution.
 *
 * <p>Suggestions are intentionally limited to command words and the five valid hiring stages.
 * The replacement range is part of each suggestion so accepting one never destroys arguments
 * that were already typed.</p>
 */
public final class AutocompleteEngine {

    private static final List<String> COMMAND_WORDS = List.of(
            "add", "clear", "delete", "edit", "exit", "find", "findskill", "help", "list", "remark",
            "stage", "summary", "view");
    private static final List<String> STAGE_VALUES = List.of("Applied", "Screened", "Interview", "Offered",
            "Rejected");

    /** Returns suggestions relevant to the input before the caret. */
    public List<Suggestion> suggest(String input, int caretPosition) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        int safeCaret = Math.max(0, Math.min(caretPosition, input.length()));
        int tokenStart = findTokenStart(input, safeCaret);
        String token = input.substring(tokenStart, safeCaret);

        if (tokenStart == 0 && !containsWhitespace(input.substring(0, safeCaret))) {
            return commandSuggestions(token, tokenStart, safeCaret);
        }

        String commandWord = firstWord(input);
        if (isStageAwareCommand(commandWord)) {
            if (token.isEmpty() || token.toLowerCase(Locale.ROOT).startsWith("s/")) {
                String partialStage = token.length() > 2 ? token.substring(2) : "";
                return stageSuggestions(partialStage, tokenStart, findTokenEnd(input, tokenStart));
            }
        }
        return List.of();
    }

    private List<Suggestion> commandSuggestions(String partial, int start, int end) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (String commandWord : COMMAND_WORDS) {
            if (startsWithIgnoreCase(commandWord, partial)) {
                suggestions.add(new Suggestion(commandWord, commandWord, start, end));
            }
        }
        return suggestions;
    }

    private List<Suggestion> stageSuggestions(String partial, int start, int end) {
        List<Suggestion> suggestions = new ArrayList<>();
        for (String stage : STAGE_VALUES) {
            if (startsWithIgnoreCase(stage, partial)) {
                suggestions.add(new Suggestion("s/" + stage, "s/" + stage, start, end));
            }
        }
        return suggestions;
    }

    private static boolean isStageAwareCommand(String commandWord) {
        return "list".equalsIgnoreCase(commandWord) || "stage".equalsIgnoreCase(commandWord);
    }

    private static String firstWord(String input) {
        int firstWhitespace = firstWhitespace(input);
        return firstWhitespace < 0 ? input : input.substring(0, firstWhitespace);
    }

    private static int findTokenStart(String input, int caretPosition) {
        for (int index = caretPosition - 1; index >= 0; index--) {
            if (Character.isWhitespace(input.charAt(index))) {
                return index + 1;
            }
        }
        return 0;
    }

    private static int findTokenEnd(String input, int tokenStart) {
        for (int index = tokenStart; index < input.length(); index++) {
            if (Character.isWhitespace(input.charAt(index))) {
                return index;
            }
        }
        return input.length();
    }

    private static int firstWhitespace(String input) {
        for (int index = 0; index < input.length(); index++) {
            if (Character.isWhitespace(input.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static boolean containsWhitespace(String input) {
        for (int index = 0; index < input.length(); index++) {
            if (Character.isWhitespace(input.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    /** A suggestion and the exact range it replaces in the command field. */
    public static final class Suggestion {
        private final String label;
        private final String replacement;
        private final int start;
        private final int end;

        /** Creates a suggestion with a replacement range in the command text. */
        public Suggestion(String label, String replacement, int start, int end) {
            this.label = label;
            this.replacement = replacement;
            this.start = start;
            this.end = end;
        }

        public String getLabel() {
            return label;
        }

        public String getReplacement() {
            return replacement;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}

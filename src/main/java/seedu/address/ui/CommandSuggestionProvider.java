package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.model.person.HiringStage;

/** Pure completion logic; never invokes a parser or executes a command while the user types. */
public class CommandSuggestionProvider {
    private static final Pattern STAGE_VALUE = Pattern.compile("(?:^|\\s)s/([^\\s]*)$");

    /** Suggests command words or stage values for the token at the caret. */
    public List<CommandSuggestion> suggest(String input, int caret) {
        requireNonNull(input);
        if (caret < 0 || caret > input.length()) {
            throw new IllegalArgumentException("Caret must be within the input");
        }
        int commandStart = 0;
        while (commandStart < input.length() && Character.isWhitespace(input.charAt(commandStart))) {
            commandStart++;
        }
        int commandEnd = tokenEnd(input, commandStart);
        if (caret > commandStart && caret <= commandEnd) {
            String prefix = input.substring(commandStart, caret).toLowerCase(Locale.ROOT);
            int start = commandStart;
            String suffix = commandEnd == input.length() ? " " : "";
            return CommandReference.entries().stream().filter(entry -> entry.getWord().startsWith(prefix))
                    .filter(entry -> !input.substring(start, commandEnd).equals(entry.getWord()))
                    .map(entry -> new CommandSuggestion(start, commandEnd, entry.getWord() + suffix,
                            entry.toString())).toList();
        }
        if (!input.substring(commandStart, commandEnd).equals("stage")) {
            return List.of();
        }
        Matcher matcher = STAGE_VALUE.matcher(input.substring(0, caret));
        if (!matcher.find()) {
            return List.of();
        }
        int start = matcher.start(1);
        int end = tokenEnd(input, caret);
        String prefix = matcher.group(1).toLowerCase(Locale.ROOT);
        return Arrays.stream(HiringStage.values()).map(HiringStage::toString)
                .filter(stage -> stage.toLowerCase(Locale.ROOT).startsWith(prefix))
                .filter(stage -> !stage.equals(input.substring(start, end)))
                .map(stage -> new CommandSuggestion(start, end, stage, stage)).toList();
    }

    private int tokenEnd(String input, int start) {
        int end = start;
        while (end < input.length() && !Character.isWhitespace(input.charAt(end))) {
            end++;
        }
        return end;
    }
}

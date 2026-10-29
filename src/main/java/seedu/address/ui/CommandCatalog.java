package seedu.address.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FindSkillCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.commands.ViewCommand;

/**
 * Offline command metadata shared by the contextual guide, autocomplete, and help window.
 * Usage text comes directly from command definitions so the UI does not drift from the parser.
 */
public final class CommandCatalog {

    private static final String CLEAR_USAGE = "clear: Clears all candidates from the pipeline.\n"
            + "Parameters: none\nExample: clear";
    private static final String EXIT_USAGE = "exit: Exits Scoutly.\nParameters: none\nExample: exit";

    private static final List<CommandInfo> COMMANDS = List.of(
            info(AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE, "add n/John Doe p/98765432 s/Java"),
            info(ClearCommand.COMMAND_WORD, CLEAR_USAGE, "clear"),
            info(DeleteCommand.COMMAND_WORD, DeleteCommand.MESSAGE_USAGE, "delete 1"),
            info(EditCommand.COMMAND_WORD, EditCommand.MESSAGE_USAGE, "edit 1 p/91234567"),
            info(ExitCommand.COMMAND_WORD, EXIT_USAGE, "exit"),
            info(FindCommand.COMMAND_WORD, FindCommand.MESSAGE_USAGE, "find Alice"),
            info(FindSkillCommand.COMMAND_WORD, FindSkillCommand.MESSAGE_USAGE, "findskill Java"),
            info(HelpCommand.COMMAND_WORD, HelpCommand.MESSAGE_USAGE, "help"),
            info(ListCommand.COMMAND_WORD, ListCommand.MESSAGE_USAGE, "list sort/name"),
            info(RemarkCommand.COMMAND_WORD, RemarkCommand.MESSAGE_USAGE, "remark 1 r/Available immediately"),
            info(StageCommand.COMMAND_WORD, StageCommand.MESSAGE_USAGE, "stage 1 s/Interview"),
            info(SummaryCommand.COMMAND_WORD, SummaryCommand.MESSAGE_USAGE, "summary"),
            info(ViewCommand.COMMAND_WORD, ViewCommand.MESSAGE_USAGE, "view 1"));

    private CommandCatalog() {
        // Utility class.
    }

    private static CommandInfo info(String commandWord, String usage, String template) {
        return new CommandInfo(commandWord, usage, template);
    }

    /** Returns all commands in the order displayed in the offline help window. */
    public static List<CommandInfo> getCommands() {
        return COMMANDS;
    }

    /** Finds a command by its case-insensitive command word. */
    public static CommandInfo find(String commandWord) {
        if (commandWord == null) {
            return null;
        }
        return COMMANDS.stream()
                .filter(command -> command.getCommandWord().equalsIgnoreCase(commandWord))
                .findFirst()
                .orElse(null);
    }

    /** Returns commands matching the word, usage, or description. */
    public static List<CommandInfo> search(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            return COMMANDS;
        }
        return COMMANDS.stream()
                .filter(command -> command.matches(normalizedQuery))
                .toList();
    }

    /** Immutable description of a command shown in the UI. */
    public static final class CommandInfo {
        private final String commandWord;
        private final String usage;
        private final String template;
        private final String summary;

        private CommandInfo(String commandWord, String usage, String template) {
            this.commandWord = Objects.requireNonNull(commandWord);
            this.usage = Objects.requireNonNull(usage);
            this.template = Objects.requireNonNull(template);
            this.summary = extractSummary(usage);
        }

        private static String extractSummary(String usage) {
            int lineEnd = usage.indexOf('\n');
            String firstLine = lineEnd < 0 ? usage : usage.substring(0, lineEnd);
            int separator = firstLine.indexOf(':');
            return separator < 0 ? firstLine : firstLine.substring(separator + 1).trim();
        }

        private boolean matches(String query) {
            return commandWord.toLowerCase(Locale.ROOT).contains(query)
                    || usage.toLowerCase(Locale.ROOT).contains(query)
                    || summary.toLowerCase(Locale.ROOT).contains(query);
        }

        public String getCommandWord() {
            return commandWord;
        }

        public String getUsage() {
            return usage;
        }

        public String getTemplate() {
            return template;
        }

        public String getSummary() {
            return summary;
        }

        /** Returns the formatted text shared by the contextual guide and offline help. */
        public String getGuideText() {
            return summary + "\n\n" + usage + "\n\nQuick template:\n" + template;
        }

        @Override
        public String toString() {
            return commandWord;
        }
    }
}

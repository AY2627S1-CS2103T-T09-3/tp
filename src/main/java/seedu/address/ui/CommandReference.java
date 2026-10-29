package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import seedu.address.logic.commands.ViewCommand;

/** Shared offline command catalogue for assistance, completion and help. */
public final class CommandReference {
    private static final List<CommandReference> ENTRIES = List.of(
            new CommandReference(AddCommand.COMMAND_WORD, "Add candidates",
                    AddCommand.MESSAGE_USAGE, "add n/ p/ s/"),
            new CommandReference(EditCommand.COMMAND_WORD, "Edit candidate details",
                    EditCommand.MESSAGE_USAGE, "edit 1 "),
            new CommandReference(DeleteCommand.COMMAND_WORD, "Delete a candidate",
                    DeleteCommand.MESSAGE_USAGE, "delete "),
            new CommandReference(ViewCommand.COMMAND_WORD, "Open a candidate profile",
                    ViewCommand.MESSAGE_USAGE, "view "),
            new CommandReference(FindCommand.COMMAND_WORD, "Find candidates by name",
                    FindCommand.MESSAGE_USAGE, "find "),
            new CommandReference(FindSkillCommand.COMMAND_WORD, "Find candidates by skills",
                    FindSkillCommand.MESSAGE_USAGE, "findskill "),
            new CommandReference(StageCommand.COMMAND_WORD, "Change a hiring stage",
                    StageCommand.MESSAGE_USAGE, "stage 1 s/"),
            new CommandReference(RemarkCommand.COMMAND_WORD, "Update a candidate remark",
                    RemarkCommand.MESSAGE_USAGE, "remark 1 r/"),
            new CommandReference(ListCommand.COMMAND_WORD, "Show all candidates",
                    "list: Shows all candidates.", "list"),
            new CommandReference(ClearCommand.COMMAND_WORD, "Remove all candidates",
                    "clear: Removes all candidates. This action cannot be undone.", "clear"),
            new CommandReference(HelpCommand.COMMAND_WORD, "Open the command reference",
                    HelpCommand.MESSAGE_USAGE, "help"),
            new CommandReference(ExitCommand.COMMAND_WORD, "Close Scoutly",
                    "exit: Closes Scoutly.", "exit"));

    private final String word;
    private final String description;
    private final String usage;
    private final String template;

    private CommandReference(String word, String description, String usage, String template) {
        this.word = requireNonNull(word);
        this.description = requireNonNull(description);
        this.usage = requireNonNull(usage);
        this.template = requireNonNull(template);
    }

    public String getWord() {
        return word;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getTemplate() {
        return template;
    }

    /** Returns immutable entries so consumers cannot modify the shared catalogue. */
    public static List<CommandReference> entries() {
        return ENTRIES;
    }

    /** Finds contextual help only for an exact command word, matching parser case sensitivity. */
    public static Optional<CommandReference> forInput(String input) {
        String trimmed = requireNonNull(input).stripLeading();
        String word = trimmed.split("\\s+", 2)[0];
        return ENTRIES.stream().filter(entry -> entry.word.equals(word)).findFirst();
    }

    /** Searches command names, descriptions and documented usage without needing internet access. */
    public static List<CommandReference> search(String query) {
        String normalized = requireNonNull(query).strip().toLowerCase(Locale.ROOT);
        return ENTRIES.stream().filter(entry -> (entry.word + " " + entry.description + " " + entry.usage)
                .toLowerCase(Locale.ROOT).contains(normalized)).toList();
    }

    @Override
    public String toString() {
        return word + " — " + description;
    }
}

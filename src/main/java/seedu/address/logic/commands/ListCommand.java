package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    private static final String MESSAGE_SUCCESS_FORMAT = "Listed %d candidate%s.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(getSuccessMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the feedback message for the number of listed candidates.
     */
    private static String getSuccessMessage(int candidateCount) {
        String suffix = candidateCount == 1 ? "" : "s";
        return String.format(MESSAGE_SUCCESS_FORMAT, candidateCount, suffix);
    }
}

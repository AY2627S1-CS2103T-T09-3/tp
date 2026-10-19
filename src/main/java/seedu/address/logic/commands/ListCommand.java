package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Comparator;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String SORT_BY_NAME_ARGUMENT = "sort/name";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all candidates, optionally sorted alphabetically by name.\n"
            + "Parameters: [" + SORT_BY_NAME_ARGUMENT + "]\n"
            + "Example: " + COMMAND_WORD + " " + SORT_BY_NAME_ARGUMENT;
    public static final String MESSAGE_EMPTY_LIST = "No candidates in your pipeline yet. Use 'add' to add one.";

    private static final String MESSAGE_SUCCESS_FORMAT = "Listed %d candidate%s.";
    private static final Comparator<Person> NAME_COMPARATOR =
            Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);

    private final boolean sortByName;

    /** Creates a command that lists candidates in their current order. */
    public ListCommand() {
        this(false);
    }

    /** Creates a command that optionally sorts candidates alphabetically by name. */
    public ListCommand(boolean sortByName) {
        this.sortByName = sortByName;
    }


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        if (sortByName) {
            model.sortFilteredPersonList(NAME_COMPARATOR);
        }
        return new CommandResult(getSuccessMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the feedback message for the number of listed candidates.
     */
    private static String getSuccessMessage(int candidateCount) {
        if (candidateCount == 0) {
            return MESSAGE_EMPTY_LIST;
        }
        String suffix = candidateCount == 1 ? "" : "s";
        return String.format(MESSAGE_SUCCESS_FORMAT, candidateCount, suffix);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ListCommand otherListCommand
                && sortByName == otherListCommand.sortByName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("sortByName", sortByName)
                .toString();
    }
}

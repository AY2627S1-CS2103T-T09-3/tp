package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String SORT_BY_NAME_ARGUMENT = "sort/name";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all candidates, optionally sorted alphabetically by name or filtered by hiring stage.\n"
            + "Parameters: [" + SORT_BY_NAME_ARGUMENT + "] OR [s/STAGE]\n"
            + HiringStage.MESSAGE_CONSTRAINTS + "\n"
            + "Examples: " + COMMAND_WORD + " " + SORT_BY_NAME_ARGUMENT + "; "
            + COMMAND_WORD + " s/Interview";
    public static final String MESSAGE_EMPTY_LIST = "No candidates in your pipeline yet. Use 'add' to add one.";
    public static final String MESSAGE_NO_MATCHING_CANDIDATES = "No candidates found at the %s stage.";

    private static final String MESSAGE_SUCCESS_FORMAT = "Listed %d candidate%s.";
    private static final Comparator<Person> NAME_COMPARATOR =
            Comparator.comparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);

    /** Specifies how candidates should be ordered when listed. */
    public enum SortOrder {
        NONE,
        NAME
    }

    private final SortOrder sortOrder;
    private final Optional<HiringStage> stageFilter;

    /**
     * Creates a command that lists candidates in their current order.
     */
    public ListCommand() {
        this.sortOrder = SortOrder.NONE;
        this.stageFilter = Optional.empty();
    }

    /**
     * Creates a command that lists candidates using the given sort order.
     */
    public ListCommand(SortOrder sortOrder) {
        this.sortOrder = requireNonNull(sortOrder);
        this.stageFilter = Optional.empty();
    }

    /**
     * Creates a command that lists candidates at the given hiring stage.
     */
    public ListCommand(HiringStage stage) {
        this.sortOrder = SortOrder.NONE;
        this.stageFilter = Optional.of(requireNonNull(stage));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Predicate<Person> predicate = stageFilter.isPresent()
                ? person -> person.getStage() == stageFilter.get()
                : PREDICATE_SHOW_ALL_PERSONS;
        model.updateFilteredPersonList(predicate);
        if (sortOrder == SortOrder.NAME) {
            model.sortFilteredPersonList(NAME_COMPARATOR);
        }
        return new CommandResult(getSuccessMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns the feedback message for the number of listed candidates.
     */
    private static String getCountMessage(int candidateCount) {
        if (candidateCount == 0) {
            return MESSAGE_EMPTY_LIST;
        }
        String suffix = candidateCount == 1 ? "" : "s";
        return String.format(MESSAGE_SUCCESS_FORMAT, candidateCount, suffix);
    }

    private String getSuccessMessage(int candidateCount) {
        if (candidateCount == 0 && stageFilter.isPresent()) {
            return String.format(MESSAGE_NO_MATCHING_CANDIDATES, stageFilter.get());
        }
        return getCountMessage(candidateCount);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ListCommand otherCommand
                && sortOrder == otherCommand.sortOrder
                && stageFilter.equals(otherCommand.stageFilter));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("sortOrder", sortOrder)
                .add("stageFilter", stageFilter)
                .toString();
    }
}

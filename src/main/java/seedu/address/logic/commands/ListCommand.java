package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

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
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all candidates, optionally filtered by hiring stage.\n"
            + "Parameters: [s/STAGE]\n"
            + HiringStage.MESSAGE_CONSTRAINTS + "\n"
            + "Example: " + COMMAND_WORD + " s/Interview";
    public static final String MESSAGE_EMPTY_LIST = "No candidates in your pipeline yet. Use 'add' to add one.";
    public static final String MESSAGE_NO_MATCHING_CANDIDATES = "No candidates found at the %s stage.";

    private static final String MESSAGE_SUCCESS_FORMAT = "Listed %d candidate%s.";

    private final Optional<HiringStage> stageFilter;

    /** Creates a command that lists all candidates. */
    public ListCommand() {
        stageFilter = Optional.empty();
    }

    /** Creates a command that lists candidates at the given hiring stage. */
    public ListCommand(HiringStage stage) {
        requireNonNull(stage);
        stageFilter = Optional.of(stage);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Predicate<Person> predicate = stageFilter.isPresent()
                ? person -> person.getStage() == stageFilter.get()
                : PREDICATE_SHOW_ALL_PERSONS;
        model.updateFilteredPersonList(predicate);
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
                && stageFilter.equals(otherCommand.stageFilter));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("stageFilter", stageFilter)
                .toString();
    }
}

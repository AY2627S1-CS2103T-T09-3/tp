package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Deletes candidates identified using displayed indexes or a full name from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Permanently deletes candidates by displayed indexes or one full name.\n"
            + "Parameters: INDEX[,INDEX]... (unique positive integers) or FULL_NAME (case-insensitive)\n"
            + "Separate multiple indexes with commas. All indexes are checked before any deletion.\n"
            + "The full name must match exactly one displayed candidate. For multiple matches, use delete INDEX.\n"
            + "All-digit input is treated as an index.\n"
            + "Examples: " + COMMAND_WORD + " 1; " + COMMAND_WORD + " 1,3,5; " + COMMAND_WORD + " Alice Pauline";

    public static final String MESSAGE_DELETE_CANDIDATE_SUCCESS = "Deleted candidate: %1$s";
    public static final String MESSAGE_DELETE_PERSONS_SUCCESS = "Deleted %1$d candidates:\n%2$s";
    public static final String MESSAGE_DUPLICATE_INDEX =
            "Duplicate indexes are not allowed. Specify each displayed index only once. No candidates were deleted.";
    public static final String MESSAGE_NAME_NOT_FOUND =
            "No candidate with this full name exists in the displayed list.";
    public static final String MESSAGE_AMBIGUOUS_NAME =
            "Multiple candidates match \"%1$s\":\n%2$s\nUse delete INDEX to choose a candidate.";

    private final List<Index> targetIndexes;
    private final Name targetName;

    /**
     * Creates a command to delete the person at {@code targetIndex} in the displayed list.
     */
    public DeleteCommand(Index targetIndex) {
        this(List.of(requireNonNull(targetIndex)));
    }

    /**
     * Creates a command to delete candidates at the given displayed indexes, in the supplied order.
     *
     * @throws IllegalArgumentException if the indexes are empty or contain duplicates
     */
    public DeleteCommand(List<Index> targetIndexes) {
        this.targetIndexes = List.copyOf(targetIndexes);
        checkArgument(!this.targetIndexes.isEmpty(), "At least one index is required.");
        checkArgument(this.targetIndexes.stream().map(Index::getZeroBased).distinct().count()
                == this.targetIndexes.size(), MESSAGE_DUPLICATE_INDEX);
        this.targetName = null;
    }

    /**
     * Creates a command to delete the unique person matching {@code targetName} in the displayed list.
     */
    public DeleteCommand(Name targetName) {
        this.targetIndexes = List.of();
        this.targetName = requireNonNull(targetName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = List.copyOf(model.getFilteredPersonList());

        // Resolve every target before mutating the live list, which changes indexes after each removal.
        List<Person> personsToDelete = targetName == null
                ? getPersonsByIndexes(lastShownList) : List.of(getPersonByName(lastShownList));
        personsToDelete.forEach(model::deletePerson);
        if (personsToDelete.size() == 1) {
            return new CommandResult(String.format(MESSAGE_DELETE_CANDIDATE_SUCCESS,
                    Messages.format(personsToDelete.get(0))));
        }
        String deletedPersons = personsToDelete.stream().map(Messages::format).collect(Collectors.joining("\n"));
        return new CommandResult(String.format(MESSAGE_DELETE_PERSONS_SUCCESS, personsToDelete.size(), deletedPersons));
    }

    /**
     * Returns the candidates at the target indexes in the displayed-list snapshot.
     * @throws CommandException if any index is outside the displayed list
     */
    private List<Person> getPersonsByIndexes(List<Person> lastShownList) throws CommandException {
        List<Person> persons = new ArrayList<>();
        for (Index index : targetIndexes) {
            if (index.getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            persons.add(lastShownList.get(index.getZeroBased()));
        }
        return persons;
    }

    /**
     * Returns the unique person whose full name matches, ignoring case, in the displayed list.
     * @throws CommandException if no person or more than one person matches.
     */
    private Person getPersonByName(List<Person> lastShownList) throws CommandException {
        return PersonLookup.findByName(lastShownList, targetName, MESSAGE_NAME_NOT_FOUND,
                matches -> formatAmbiguousName(lastShownList, matches));
    }

    /**
     * Formats matching candidates with their indexes in the currently displayed list, names, and emails.
     */
    private String formatAmbiguousName(List<Person> lastShownList, List<Person> matches) {
        String candidates = matches.stream()
                .map(person -> (lastShownList.indexOf(person) + 1) + ". " + person.getName()
                        + " — " + person.getEmail())
                .collect(Collectors.joining("\n"));
        return String.format(MESSAGE_AMBIGUOUS_NAME, targetName, candidates);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return targetIndexes.equals(otherDeleteCommand.targetIndexes)
                && Objects.equals(targetName, otherDeleteCommand.targetName);
    }

    @Override
    public String toString() {
        if (targetName != null) {
            return new ToStringBuilder(this)
                    .add("targetName", targetName)
                    .toString();
        }
        if (targetIndexes.size() == 1) {
            return new ToStringBuilder(this)
                    .add("targetIndex", targetIndexes.get(0))
                    .toString();
        }
        return new ToStringBuilder(this)
                .add("targetIndexes", targetIndexes)
                .toString();
    }
}

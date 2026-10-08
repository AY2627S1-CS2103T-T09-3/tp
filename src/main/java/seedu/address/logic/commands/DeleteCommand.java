package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using their displayed index or full name from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Permanently deletes a candidate by index or full name from the displayed list.\n"
            + "Parameters: INDEX (must be a positive integer) or FULL_NAME (case-insensitive)\n"
            + "The full name must match exactly one displayed candidate. For multiple matches, use delete INDEX.\n"
            + "All-digit input is treated as an index.\n"
            + "Examples: " + COMMAND_WORD + " 1; " + COMMAND_WORD + " Alice Pauline";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_NAME_NOT_FOUND =
            "No candidate with this full name exists in the displayed list.";
    public static final String MESSAGE_AMBIGUOUS_NAME =
            "Multiple candidates with this full name exist in the displayed list. Use delete INDEX instead.";

    private final Index targetIndex;
    private final Name targetName;

    /**
     * Creates a command to delete the person at {@code targetIndex} in the displayed list.
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.targetName = null;
    }

    /**
     * Creates a command to delete the unique person matching {@code targetName} in the displayed list.
     */
    public DeleteCommand(Name targetName) {
        this.targetIndex = null;
        this.targetName = requireNonNull(targetName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        Person personToDelete = targetName == null
                ? getPersonByIndex(lastShownList) : getPersonByName(lastShownList);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    /**
     * Returns the person at the target index in the displayed list.
     * @throws CommandException if the index is outside the displayed list.
     */
    private Person getPersonByIndex(List<Person> lastShownList) throws CommandException {
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        return lastShownList.get(targetIndex.getZeroBased());
    }

    /**
     * Returns the unique person whose full name matches, ignoring case, in the displayed list.
     * @throws CommandException if no person or more than one person matches.
     */
    private Person getPersonByName(List<Person> lastShownList) throws CommandException {
        List<Person> matches = lastShownList.stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(targetName.fullName))
                .toList();
        if (matches.isEmpty()) {
            throw new CommandException(MESSAGE_NAME_NOT_FOUND);
        }
        if (matches.size() > 1) {
            throw new CommandException(MESSAGE_AMBIGUOUS_NAME);
        }
        return matches.get(0);
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

        return Objects.equals(targetIndex, otherDeleteCommand.targetIndex)
                && Objects.equals(targetName, otherDeleteCommand.targetName);
    }

    @Override
    public String toString() {
        if (targetName != null) {
            return new ToStringBuilder(this)
                    .add("targetName", targetName)
                    .toString();
        }
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}

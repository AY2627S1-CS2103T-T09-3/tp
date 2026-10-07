package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/**
 * Updates the hiring stage of a candidate in the displayed list.
 */
public class StageCommand extends Command {

    public static final String COMMAND_WORD = "stage";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the hiring stage of the candidate at the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) s/STAGE\n"
            + HiringStage.MESSAGE_CONSTRAINTS + "\n"
            + "Example: " + COMMAND_WORD + " 1 s/Interview";
    public static final String MESSAGE_SUCCESS = "Updated hiring stage of %1$s to %2$s.";
    public static final String MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX =
            "The candidate index provided is invalid.";

    private final Index targetIndex;
    private final HiringStage stage;

    /**
     * Creates a command to set the stage of the candidate at the displayed index.
     */
    public StageCommand(Index targetIndex, HiringStage stage) {
        requireAllNonNull(targetIndex, stage);
        this.targetIndex = targetIndex;
        this.stage = stage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        }

        Person person = displayedPersons.get(targetIndex.getZeroBased());
        Person updatedPerson = new Person(person.getName(), person.getPhone(), person.getEmail(),
                person.getAddress(), person.getRemark(), stage, person.getTags());
        model.setPerson(person, updatedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), stage));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof StageCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex) && stage == otherCommand.stage);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("stage", stage)
                .toString();
    }
}

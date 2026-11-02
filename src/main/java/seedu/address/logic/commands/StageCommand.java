package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            + ": Updates the hiring stage of one or more candidates at displayed indexes.\n"
            + "Parameters: INDEX[,INDEX]... (positive integers) s/STAGE\n"
            + HiringStage.MESSAGE_CONSTRAINTS + "\n"
            + "Examples: " + COMMAND_WORD + " 1 s/Interview; " + COMMAND_WORD + " 1,2 s/Interview";
    public static final String MESSAGE_SUCCESS = "Updated %1$s's stage to %2$s.";
    public static final String MESSAGE_ALREADY_AT_STAGE = "%1$s is already at %2$s.";
    public static final String MESSAGE_BATCH_SUCCESS = "Updated %1$d candidates to %2$s.";
    public static final String MESSAGE_BATCH_NO_CHANGE = "Selected candidates are already at %1$s.";
    public static final String MESSAGE_DUPLICATE_INDEX = "Duplicate candidate indexes are not allowed.";
    public static final String MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX =
            "The candidate index provided is invalid.";

    private final List<Index> targetIndexes;
    private final HiringStage stage;

    /**
     * Creates a command to set the stage of the candidate at the displayed index.
     */
    public StageCommand(Index targetIndex, HiringStage stage) {
        this(List.of(requireNonNull(targetIndex)), stage);
    }

    /** Creates a command to update several displayed candidates atomically. */
    public StageCommand(List<Index> targetIndexes, HiringStage stage) {
        requireNonNull(targetIndexes);
        requireNonNull(stage);
        checkArgument(!targetIndexes.isEmpty(), "At least one index is required.");
        checkArgument(targetIndexes.stream().noneMatch(java.util.Objects::isNull),
                "Candidate indexes cannot be null.");
        Set<Integer> distinctIndexes = new HashSet<>();
        checkArgument(targetIndexes.stream().map(Index::getZeroBased).allMatch(distinctIndexes::add),
                MESSAGE_DUPLICATE_INDEX);
        this.targetIndexes = List.copyOf(targetIndexes);
        this.stage = stage;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = List.copyOf(model.getFilteredPersonList());
        List<Person> selectedPersons = getSelectedPersons(displayedPersons);
        if (selectedPersons.size() == 1) {
            Person person = selectedPersons.get(0);
            if (person.getStage() == stage) {
                return new CommandResult(String.format(MESSAGE_ALREADY_AT_STAGE, person.getName(), stage));
            }
            model.setPerson(person, withStage(person, stage));
            return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), stage));
        }

        int updatedCount = 0;
        for (Person person : selectedPersons) {
            if (person.getStage() != stage) {
                model.setPerson(person, withStage(person, stage));
                updatedCount++;
            }
        }
        if (updatedCount == 0) {
            return new CommandResult(String.format(MESSAGE_BATCH_NO_CHANGE, stage));
        }
        return new CommandResult(String.format(MESSAGE_BATCH_SUCCESS, updatedCount, stage));
    }

    private List<Person> getSelectedPersons(List<Person> displayedPersons) throws CommandException {
        List<Person> selectedPersons = new ArrayList<>();
        for (Index targetIndex : targetIndexes) {
            if (targetIndex.getZeroBased() >= displayedPersons.size()) {
                throw new CommandException(MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
            }
            selectedPersons.add(displayedPersons.get(targetIndex.getZeroBased()));
        }
        return selectedPersons;
    }

    private Person withStage(Person person, HiringStage newStage) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getRemark(), newStage, person.getTags(), person.getSkills(), person.getFollowUp());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof StageCommand otherCommand
                && targetIndexes.equals(otherCommand.targetIndexes) && stage == otherCommand.stage);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        if (targetIndexes.size() == 1) {
            return builder.add("targetIndex", targetIndexes.get(0)).add("stage", stage).toString();
        }
        return builder.add("targetIndexes", targetIndexes).add("stage", stage).toString();
    }
}

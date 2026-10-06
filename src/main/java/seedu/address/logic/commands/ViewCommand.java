package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays the profile of a candidate identified by the index in the displayed list.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views the candidate profile at the index in the displayed candidate list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX =
            "The candidate index provided is invalid.";
    public static final String MESSAGE_VIEW_CANDIDATE_SUCCESS = "Candidate profile:\n"
            + "Name: %1$s\nPhone: %2$s\nEmail: %3$s\nAddress: %4$s\nTags: %5$s\nRemark: %6$s";

    private final Index targetIndex;

    /**
     * Creates a ViewCommand to view the candidate at the specified displayed index.
     */
    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        }

        Person candidate = lastShownList.get(targetIndex.getZeroBased());
        String tags = candidate.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
        String remark = candidate.getRemark().value;
        return new CommandResult(String.format(MESSAGE_VIEW_CANDIDATE_SUCCESS,
                candidate.getName(), candidate.getPhone(), candidate.getEmail(), candidate.getAddress(),
                tags.isEmpty() ? "None" : tags, remark.isEmpty() ? "None" : remark));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}

package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;

/**
 * Displays the profile of a candidate identified by the displayed index or full name.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views a candidate profile by index or full name in the displayed list.\n"
            + "Parameters: INDEX (must be a positive integer) or FULL_NAME (case-insensitive)\n"
            + "The full name must match exactly one displayed candidate. For multiple matches, use view INDEX.\n"
            + "All-digit input is treated as an index.\n"
            + "Examples: " + COMMAND_WORD + " 1; " + COMMAND_WORD + " Alice Pauline";

    public static final String MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX =
            "The candidate index provided is invalid.";
    public static final String MESSAGE_VIEW_CANDIDATE_SUCCESS = "Candidate profile:\n"
            + "Name: %1$s\nPhone: %2$s\nEmail: %3$s\nAddress: %4$s\nTags: %5$s\nRemark: %6$s\n"
            + "Hiring stage: %7$s\nSkills: %8$s";

    public static final String MESSAGE_NAME_NOT_FOUND =
            "No candidate with this full name exists in the displayed list. Use list to show all candidates.";
    public static final String MESSAGE_AMBIGUOUS_NAME =
            "Multiple candidates with this full name exist in the displayed list. Use view INDEX instead.";

    private final Index targetIndex;
    private final Name targetName;

    /**
     * Creates a ViewCommand to view the candidate at the specified displayed index.
     */
    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.targetName = null;
    }

    /**
     * Creates a ViewCommand to view the unique candidate matching the specified full name.
     */
    public ViewCommand(Name targetName) {
        this.targetIndex = null;
        this.targetName = requireNonNull(targetName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        Person candidate = targetName == null
                ? getPersonByIndex(lastShownList) : getPersonByName(lastShownList);
        String tags = candidate.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
        String remark = candidate.getRemark().value;
        String skills = candidate.getSkills().stream()
                .sorted(Skill.DISPLAY_ORDER)
                .map(skill -> skill.value)
                .collect(Collectors.joining(", "));
        return new CommandResult(String.format(MESSAGE_VIEW_CANDIDATE_SUCCESS,
                candidate.getName(), candidate.getPhone(), candidate.getEmail(), candidate.getAddress(),
                tags.isEmpty() ? "None" : tags, remark.isEmpty() ? "None" : remark, candidate.getStage(),
                skills.isEmpty() ? "None" : skills));
    }

    /**
     * Returns the candidate at the target index in the displayed list.
     * @throws CommandException if the index is outside the displayed list
     */
    private Person getPersonByIndex(List<Person> lastShownList) throws CommandException {
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        }
        return lastShownList.get(targetIndex.getZeroBased());
    }

    /**
     * Returns the unique candidate whose full name matches, ignoring case, in the displayed list.
     * @throws CommandException if no candidate or more than one candidate matches
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

        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherViewCommand.targetIndex)
                && Objects.equals(targetName, otherViewCommand.targetName);
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

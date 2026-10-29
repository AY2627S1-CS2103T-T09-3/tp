package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Schedules, completes or clears a follow-up for a displayed candidate.
 */
public class FollowUpCommand extends Command {
    public static final String COMMAND_WORD = "followup";
    public static final String MESSAGE_USAGE = "followup: Sets, completes or clears a candidate follow-up.\n"
            + "Parameters: INDEX d/YYYY-MM-DD [r/NOTE] | INDEX done | INDEX clear\n"
            + "Example: followup 1 d/2026-11-05 r/Call about interview availability";
    public static final String MESSAGE_NO_FOLLOW_UP = "This candidate has no follow-up scheduled.";
    public static final String MESSAGE_ALREADY_COMPLETED = "This follow-up is already completed.";

    /**
     * Operations supported by the followup command.
     */
    public enum Action {
        SET, COMPLETE, CLEAR
    }

    private final Index index;
    private final Action action;
    private final FollowUp followUp;

    /**
     * Creates an operation. SET requires a scheduled, pending reminder.
     */
    public FollowUpCommand(Index index, Action action, FollowUp followUp) {
        requireAllNonNull(index, action, followUp);
        if (action == Action.SET && (!followUp.isScheduled() || followUp.isCompleted())) {
            throw new IllegalArgumentException("A new follow-up must have a date and be pending.");
        }
        this.index = index;
        this.action = action;
        this.followUp = action == Action.SET ? followUp : FollowUp.empty();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        List<Person> displayed = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayed.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person person = displayed.get(index.getZeroBased());
        FollowUp updated = switch (action) {
            case SET -> followUp;
            case CLEAR -> FollowUp.empty();
            case COMPLETE -> complete(person.getFollowUp());
        };
        Person edited = person.withFollowUp(updated);
        model.setPerson(person, edited);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String verb = switch (action) {
            case SET -> "Scheduled";
            case CLEAR -> "Cleared";
            case COMPLETE -> "Completed";
        };
        return new CommandResult(verb + " follow-up for " + person.getName()
                + (action == Action.CLEAR ? "." : ": " + updated));
    }

    private FollowUp complete(FollowUp current) throws CommandException {
        if (!current.isScheduled()) {
            throw new CommandException(MESSAGE_NO_FOLLOW_UP);
        }
        if (current.isCompleted()) {
            throw new CommandException(MESSAGE_ALREADY_COMPLETED);
        }
        return current.complete();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FollowUpCommand command
                && index.equals(command.index) && action == command.action && followUp.equals(command.followUp));
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, action, followUp);
    }
}

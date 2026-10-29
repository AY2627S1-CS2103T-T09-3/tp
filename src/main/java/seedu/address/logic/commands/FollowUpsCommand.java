package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import seedu.address.model.Model;
import seedu.address.model.person.FollowUpFilter;
import seedu.address.model.person.Person;

/**
 * Displays candidates whose follow-ups match a requested view.
 */
public class FollowUpsCommand extends Command {
    public static final String COMMAND_WORD = "followups";
    public static final String MESSAGE_USAGE = "followups: Lists candidate follow-ups.\n"
            + "Parameters: [pending|overdue|today|upcoming|completed|all]\n"
            + "Example: followups overdue\nDefault: pending (all unfinished follow-ups).";

    private final FollowUpFilter filter;

    public FollowUpsCommand(FollowUpFilter filter) {
        this.filter = requireNonNull(filter);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now();
        model.updateFilteredPersonList(filter.predicate(today));
        List<Person> people = model.getFilteredPersonList();
        long overdue = people.stream().filter(person -> person.getFollowUp().isOverdue(today)).count();
        long dueToday = people.stream().filter(FollowUpFilter.TODAY.predicate(today)).count();
        return new CommandResult(String.format("%d candidate(s) with %s follow-ups as of %s. "
                + "In this view: %d overdue, %d due today.", people.size(),
                filter.name().toLowerCase(Locale.ROOT), today, overdue, dueToday));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FollowUpsCommand command && filter == command.filter);
    }

    @Override
    public int hashCode() {
        return filter.hashCode();
    }
}

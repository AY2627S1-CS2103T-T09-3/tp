package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUpFilter;

/**
 * Parses the optional follow-up view name.
 */
public class FollowUpsCommandParser implements Parser<FollowUpsCommand> {
    @Override
    public FollowUpsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String view = args.strip();
        if (view.isEmpty()) {
            return new FollowUpsCommand(FollowUpFilter.PENDING);
        }
        try {
            return new FollowUpsCommand(FollowUpFilter.fromString(view));
        } catch (IllegalArgumentException e) {
            throw new ParseException(FollowUpFilter.MESSAGE_CONSTRAINTS + "\n" + FollowUpsCommand.MESSAGE_USAGE, e);
        }
    }
}

package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand.Action;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses follow-up operations, rejecting ambiguous or incomplete updates.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_FOLLOW_UP_DATE, PREFIX_REMARK);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_FOLLOW_UP_DATE, PREFIX_REMARK);
        String[] preamble = arguments.getPreamble().split("\\s+");
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble[0]);
        } catch (ParseException e) {
            throw usageError();
        }
        boolean hasDate = arguments.getValue(PREFIX_FOLLOW_UP_DATE).isPresent();
        boolean hasNote = arguments.getValue(PREFIX_REMARK).isPresent();
        if (preamble.length == 2 && !hasDate && !hasNote) {
            Action action = switch (preamble[1]) {
                case "done" -> Action.COMPLETE;
                case "clear" -> Action.CLEAR;
                default -> throw usageError();
            };
            return new FollowUpCommand(index, action, FollowUp.empty());
        }
        if (preamble.length != 1 || !hasDate) {
            throw usageError();
        }
        String date = arguments.getValue(PREFIX_FOLLOW_UP_DATE).orElseThrow();
        if (!FollowUp.isValidDate(date)) {
            throw new ParseException(FollowUp.MESSAGE_DATE_CONSTRAINTS);
        }
        String note = arguments.getValue(PREFIX_REMARK).orElse("");
        if (!FollowUp.isValidNote(note)) {
            throw new ParseException(FollowUp.MESSAGE_NOTE_CONSTRAINTS);
        }
        return new FollowUpCommand(index, Action.SET, new FollowUp(date, note, false));
    }

    private ParseException usageError() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
    }
}

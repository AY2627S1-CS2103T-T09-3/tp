package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HiringStage;

/**
 * Parses a displayed index and hiring stage into a StageCommand.
 */
public class StageCommandParser implements Parser<StageCommand> {

    @Override
    public StageCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(parts[0]);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), e);
        }

        try {
            return new StageCommand(index, HiringStage.fromString(parts[1]));
        } catch (IllegalArgumentException e) {
            throw new ParseException(HiringStage.MESSAGE_CONSTRAINTS, e);
        }
    }
}

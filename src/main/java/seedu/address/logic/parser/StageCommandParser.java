package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;

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
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_STAGE);
        if (argMultimap.getValue(PREFIX_STAGE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), e);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STAGE);
        try {
            return new StageCommand(index, HiringStage.fromString(argMultimap.getValue(PREFIX_STAGE).get()));
        } catch (IllegalArgumentException e) {
            throw new ParseException(HiringStage.MESSAGE_CONSTRAINTS, e);
        }
    }
}

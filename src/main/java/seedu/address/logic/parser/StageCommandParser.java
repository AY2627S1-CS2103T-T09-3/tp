package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<Index> indexes;
        try {
            indexes = parseIndexes(argMultimap.getPreamble());
        } catch (ParseException e) {
            if (StageCommand.MESSAGE_DUPLICATE_INDEX.equals(e.getMessage())) {
                throw e;
            }
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE), e);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STAGE);
        try {
            return new StageCommand(indexes, HiringStage.fromString(argMultimap.getValue(PREFIX_STAGE).get()));
        } catch (IllegalArgumentException e) {
            if (StageCommand.MESSAGE_DUPLICATE_INDEX.equals(e.getMessage())) {
                throw new ParseException(e.getMessage(), e);
            }
            throw new ParseException(HiringStage.MESSAGE_CONSTRAINTS, e);
        }
    }

    private List<Index> parseIndexes(String preamble) throws ParseException {
        String trimmedPreamble = preamble.trim();
        if (trimmedPreamble.isEmpty()) {
            throw new ParseException("Missing candidate index");
        }
        List<Index> indexes = new ArrayList<>();
        Set<Integer> seenIndexes = new HashSet<>();
        for (String value : trimmedPreamble.split(",", -1)) {
            Index index = ParserUtil.parseIndex(value.trim());
            if (!seenIndexes.add(index.getZeroBased())) {
                throw new ParseException(StageCommand.MESSAGE_DUPLICATE_INDEX);
            }
            indexes.add(index);
        }
        return indexes;
    }
}

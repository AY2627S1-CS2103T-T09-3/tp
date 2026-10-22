package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        if (args.contains(",")) {
            return parseIndexes(args);
        }
        try {
            return ParserUtil.parseIndexOrName(args, DeleteCommand::new, DeleteCommand::new);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Parses comma-separated indexes, rejecting empty entries and repeated numeric values.
     */
    private DeleteCommand parseIndexes(String args) throws ParseException {
        List<Index> indexes = new ArrayList<>();
        Set<Integer> seenIndexes = new HashSet<>();
        for (String value : args.split(",", -1)) {
            Index index;
            try {
                index = ParserUtil.parseIndex(value);
            } catch (ParseException pe) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
            }
            if (!seenIndexes.add(index.getZeroBased())) {
                throw new ParseException(DeleteCommand.MESSAGE_DUPLICATE_INDEX);
            }
            indexes.add(index);
        }
        return new DeleteCommand(indexes);
    }

}

package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ViewCommand object.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses the given arguments and returns a ViewCommand for execution.
     *
     * @throws ParseException if the input does not contain a valid index or full name
     */
    public ViewCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        try {
            // All-digit input remains an index, including zero and overflowing values.
            if (trimmedArgs.matches("[0-9]+")) {
                return new ViewCommand(ParserUtil.parseIndex(trimmedArgs));
            }
            return new ViewCommand(ParserUtil.parseName(trimmedArgs));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE), pe);
        }
    }
}

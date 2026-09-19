package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        String userInput = "1 " + PREFIX_REMARK + "Likes swimming";
        assertParseSuccess(parser, userInput,
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")));
    }

    @Test
    public void parse_emptyRemark_returnsRemarkCommand() {
        assertParseSuccess(parser, "1 " + PREFIX_REMARK, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "0 " + PREFIX_REMARK + "Likes swimming", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a " + PREFIX_REMARK + "Likes swimming", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertParseFailure(parser, PREFIX_REMARK + "Likes swimming", MESSAGE_INVALID_FORMAT);
    }
}

package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.person.Name;

/**
 * Tests parsing of displayed indexes and full names for deletion.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " 1 ", new DeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_validName_returnsDeleteCommand() {
        assertParseSuccess(parser, "Alice Pauline", new DeleteCommand(new Name("Alice Pauline")));
        assertParseSuccess(parser, "  aLiCe PaUlInE  ", new DeleteCommand(new Name("aLiCe PaUlInE")));
        assertParseSuccess(parser, "Alice", new DeleteCommand(new Name("Alice")));
        assertParseSuccess(parser, "1 Alice", new DeleteCommand(new Name("1 Alice")));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, "0", expectedMessage);
        assertParseFailure(parser, "-1", expectedMessage);
        assertParseFailure(parser, "1.5", expectedMessage);
        assertParseFailure(parser, "2147483648", expectedMessage);
        assertParseFailure(parser, "999999999999999999999999999999", expectedMessage);
        assertParseFailure(parser, "Alice!", expectedMessage);
    }
}

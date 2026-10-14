package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.person.Name;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validArgs_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2147483647", new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_fullName_returnsNameCommand() {
        assertParseSuccess(parser, "Alice Pauline", new ViewCommand(new Name("Alice Pauline")));
        assertParseSuccess(parser, "  alice pauline  ", new ViewCommand(new Name("alice pauline")));
        assertParseSuccess(parser, "a", new ViewCommand(new Name("a")));
        assertParseSuccess(parser, "123 Alex", new ViewCommand(new Name("123 Alex")));
    }

    @Test
    public void parse_nameWithWhitespace_normalizesName() {
        assertParseSuccess(parser, "  Alice \t Pauline  ", new ViewCommand(new Name("Alice Pauline")));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        for (String input : new String[] {"", " ", "0", "-1", "1.5", "Alice!",
            "2147483648", "999999999999999999999999999999"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }
    }
}

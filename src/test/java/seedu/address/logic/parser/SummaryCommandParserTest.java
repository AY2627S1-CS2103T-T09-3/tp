package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SummaryCommand;

public class SummaryCommandParserTest {

    private final SummaryCommandParser parser = new SummaryCommandParser();

    @Test
    public void parse_emptyOrWhitespace_returnsSummaryCommand() throws Exception {
        assertTrue(parser.parse("") instanceof SummaryCommand);
        assertTrue(parser.parse(" \t\n ") instanceof SummaryCommand);
    }

    @Test
    public void parse_parameters_showsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SummaryCommand.MESSAGE_USAGE);
        for (String input : new String[]{"all", "1", "s/Interview"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

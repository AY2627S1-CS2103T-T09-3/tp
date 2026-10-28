package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_success() throws Exception {
        RemarkCommand expected = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball"));
        assertParseSuccess(parser, "1 r/Likes baseball", expected);
        assertEquals(expected, new AddressBookParser().parseCommand("remark 1 r/Likes baseball"));
    }

    @Test
    public void parse_repeatedRemarkPrefixes_rejectsWithoutLosingExistingNote() throws Exception {
        ModelManager model = new ModelManager();
        AddressBookParser commandParser = new AddressBookParser();
        commandParser.parseCommand("add n/Alice p/91234567 s/Java").execute(model);
        commandParser.parseCommand("remark 1 r/Keep this note").execute(model);

        for (String command : new String[]{"remark 1 r/Contacted r/Interview tomorrow", "remark 1 r/Contacted r/"}) {
            ParseException error = assertThrows(ParseException.class, () -> commandParser.parseCommand(command));
            assertEquals("Multiple values specified for the following single-valued field(s): r/", error.getMessage());
            assertEquals("Keep this note", model.getFilteredPersonList().getFirst().getRemark().value);
        }
    }

    @Test
    public void parse_emptyRemark_clearsRemark() {
        RemarkCommand expected = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", expected);
        assertParseSuccess(parser, "1", expected);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String args : new String[]{"", "0 r/Note", "-1 r/Note", "abc r/Note", "1.5 r/Note"}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
        }
    }
}

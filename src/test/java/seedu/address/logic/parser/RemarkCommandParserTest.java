package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
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
    public void parse_duplicateRemarkPrefixes_failure() {
        for (String args : List.of("1 r/First note r/Second note", "1 r/Same r/Same",
                "1 r/ r/Second note", "1 r/First note r/", "1 r/ r/", "1 r/First r/Second r/Third")) {
            assertParseFailure(parser, args, MESSAGE_DUPLICATE_FIELDS + "r/");
        }
    }

    @Test
    public void parse_slashesWithinRemarkText_success() {
        String remark = "Read https://example.com/r/jobs and folder/r/notes";
        assertParseSuccess(parser, "1 r/" + remark, new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remark)));
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

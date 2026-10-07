package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StageCommand;
import seedu.address.model.person.HiringStage;

public class StageCommandParserTest {

    private final StageCommandParser parser = new StageCommandParser();

    @Test
    public void parse_validArgs_returnsStageCommand() {
        for (HiringStage stage : HiringStage.values()) {
            assertParseSuccess(parser, "1 " + stage, new StageCommand(INDEX_FIRST_PERSON, stage));
        }
        assertParseSuccess(parser, " \t1 \t iNtErViEw \n",
                new StageCommand(INDEX_FIRST_PERSON, HiringStage.INTERVIEW));
    }

    @Test
    public void parse_missingArgsOrInvalidIndex_showsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);
        for (String input : new String[]{"", " ", "1", "1 ", "Interview", "0 Applied", "-1 Applied",
            "+1 Applied", "1.5 Applied", "2147483648 Applied", "one Applied"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_invalidStage_showsAllowedStages() {
        for (String input : new String[]{"1 Hired", "1 Applied Screened", "1 Interview extra", "1 s/Applied"}) {
            assertParseFailure(parser, input, HiringStage.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

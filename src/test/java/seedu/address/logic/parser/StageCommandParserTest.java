package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.StageCommand;
import seedu.address.model.person.HiringStage;

public class StageCommandParserTest {

    private final StageCommandParser parser = new StageCommandParser();

    @Test
    public void parse_validArgs_returnsStageCommand() {
        for (HiringStage stage : HiringStage.values()) {
            assertParseSuccess(parser, "1 s/" + stage, new StageCommand(INDEX_FIRST_PERSON, stage));
        }
        assertParseSuccess(parser, " \t1 \t s/ iNtErViEw \n",
                new StageCommand(INDEX_FIRST_PERSON, HiringStage.INTERVIEW));
    }

    @Test
    public void parse_missingArgsOrInvalidIndex_showsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, StageCommand.MESSAGE_USAGE);
        for (String input : new String[]{"", " ", "1", "1 ", " s/Interview", "0 s/Applied", "-1 s/Applied",
            "+1 s/Applied", "1.5 s/Applied", "2147483648 s/Applied", "one s/Applied",
            "1 Interview", "1 S/Interview", "1s/Interview", "1 2 s/Interview"}) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    public void parse_invalidStage_showsAllowedStages() {
        for (String input : new String[]{"1 s/", "1 s/ ", "1 s/Hired", "1 s/Applied Screened",
            "1 s/Interview extra", "1 s/Interview t/Java"}) {
            assertParseFailure(parser, input, HiringStage.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicateStagePrefix_rejectsRepeatedValues() {
        String expected = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STAGE);
        assertParseFailure(parser, "1 s/Applied s/Interview", expected);
        assertParseFailure(parser, "1 s/Interview s/Interview", expected);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}

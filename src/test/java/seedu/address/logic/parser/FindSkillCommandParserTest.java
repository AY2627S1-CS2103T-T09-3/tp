package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindSkillCommand;
import seedu.address.model.person.Skill;

public class FindSkillCommandParserTest {

    private final FindSkillCommandParser parser = new FindSkillCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindSkillCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "     ", expectedMessage);
        assertParseFailure(parser, "any", expectedMessage);
        assertParseFailure(parser, "all", expectedMessage);
    }

    @Test
    public void parse_singleSkill_returnsFindSkillCommand() {
        assertParseSuccess(parser, "Java", new FindSkillCommand(new Skill("Java")));
        assertParseSuccess(parser, "  java  ", new FindSkillCommand(new Skill("java")));
    }

    @Test
    public void parse_anyMode_returnsDeduplicatedFindSkillCommand() throws Exception {
        FindSkillCommand expected = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("Python")));
        assertParseSuccess(parser, "any s/Java s/java s/Python", expected);
        assertParseSuccess(parser, "ANY s/Java s/Python", expected);
        assertEquals("seedu.address.logic.commands.FindSkillCommand{searchMode=ANY, skills=[Java, Python]}",
                parser.parse("any s/Java s/java s/Python").toString());
    }

    @Test
    public void parse_allMode_returnsFindSkillCommand() {
        FindSkillCommand expected = new FindSkillCommand(FindSkillCommand.SearchMode.ALL,
                List.of(new Skill("Java"), new Skill("SQL")));
        assertParseSuccess(parser, "all s/Java s/SQL", expected);
    }

    @Test
    public void parse_invalidMode_throwsParseException() {
        assertParseFailure(parser, "xor s/Java", Skill.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_malformedModeArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindSkillCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "any Java", expectedMessage);
        assertParseFailure(parser, "all x/Java", expectedMessage);
    }

    @Test
    public void parse_invalidSkillArguments_throwsParseException() {
        assertParseFailure(parser, "Java SQL", Skill.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "Java-8", Skill.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "any s/Java s/", Skill.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "all s/Java SQL", Skill.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "all s/Java s/C++", Skill.MESSAGE_CONSTRAINTS);
    }
}

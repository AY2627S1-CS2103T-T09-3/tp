package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILL;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Skill;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_singleSkill_success() {
        Person expectedPerson = new PersonBuilder(BOB).withEmail("").withAddress("").withTags()
                .withSkills("Java").build();

        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_multipleSkills_deduplicatesCaseInsensitively() {
        Person expectedPerson = new PersonBuilder(BOB).withEmail("").withAddress("").withTags()
                .withSkills("Java", "SQL").build();

        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java"
                        + " " + PREFIX_SKILL + "java" + " " + PREFIX_SKILL + "SQL"
                        + " " + PREFIX_SKILL + "sql",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameWithNumbersHyphensAndApostrophes_success() {
        Person expectedPerson = new PersonBuilder(BOB).withName("Anne-Marie O'Connor 2")
                .withEmail("").withAddress("").withTags().withSkills("Java").build();

        assertParseSuccess(parser, " n/Anne-Marie O'Connor 2" + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_duplicateNameOrPhone_failure() {
        String validInput = NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java";

        assertParseFailure(parser, validInput + " " + PREFIX_NAME + "Other Name",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, validInput + " " + PREFIX_PHONE + "99999999",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
    }

    @Test
    public void parse_missingRequiredFields_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java", expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + " " + PREFIX_SKILL + "Java", expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB, AddCommand.MESSAGE_NO_SKILL);
    }

    @Test
    public void parse_unknownPrefixOrUnexpectedText_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "unexpected " + NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java x/foo",
                String.format(AddCommand.MESSAGE_UNKNOWN_FIELD, "x/"));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java unexpected",
                Skill.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownField_failure() {
        String expectedMessage = String.format(AddCommand.MESSAGE_UNKNOWN_FIELD, "x/");
        String validInput = NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java";

        assertParseFailure(parser, "x/foo " + validInput, expectedMessage);
        assertParseFailure(parser, validInput + " x/foo", expectedMessage);
        assertParseFailure(parser, NAME_DESC_BOB + " x/foo" + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, " n/James&" + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/" + "A".repeat(101) + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java",
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + " p/123456" + " " + PREFIX_SKILL + "Java",
                Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL + "Java Script",
                Skill.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_SKILL,
                Skill.MESSAGE_CONSTRAINTS);
    }
}

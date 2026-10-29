package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand.Action;
import seedu.address.logic.commands.FollowUpsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.FollowUpFilter;

public class FollowUpCommandParserTest {
    private final FollowUpCommandParser parser = new FollowUpCommandParser();

    @Test
    public void parse_scheduleAndLifecycle_dispatchesThroughMainParser() throws Exception {
        FollowUpCommand expected = new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET,
                new FollowUp("2026-11-05", "Call candidate", false));
        assertParseSuccess(parser, " 1 d/2026-11-05 r/Call candidate ", expected);
        assertParseSuccess(parser, " 1 r/Call candidate d/2026-11-05", expected);
        assertEquals(expected, new AddressBookParser().parseCommand("followup 1 d/2026-11-05 r/Call candidate"));
        assertParseSuccess(parser, "1 d/2026-11-05", new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET,
                new FollowUp("2026-11-05", "", false)));
        assertParseSuccess(parser, "1 done",
                new FollowUpCommand(INDEX_FIRST_PERSON, Action.COMPLETE, FollowUp.empty()));
        assertParseSuccess(parser, "1 clear", new FollowUpCommand(INDEX_FIRST_PERSON, Action.CLEAR, FollowUp.empty()));
    }

    @Test
    public void parse_invalidOrAmbiguousOperations_rejected() {
        for (String input : new String[]{"", "0 d/2026-11-05", "-1 done", "2147483648 done", "1", "1 unknown",
            "1 r/Call", "1 done d/2026-11-05", "1 clear r/Call", "1 done extra", "1 2 d/2026-11-05"}) {
            assertParseFailure(parser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
        }
        for (String input : new String[]{"1 d/", "1 d/2026-02-29", "1 d/2026-1-01", "1 d/tomorrow"}) {
            assertParseFailure(parser, input, FollowUp.MESSAGE_DATE_CONSTRAINTS);
        }
        assertParseFailure(parser, "1 d/2026-11-05 r/" + "a".repeat(201), FollowUp.MESSAGE_NOTE_CONSTRAINTS);
        assertThrows(ParseException.class, () -> parser.parse("1 d/2026-11-05 d/2026-11-06"));
        assertThrows(ParseException.class, () -> parser.parse("1 d/2026-11-05 r/first r/second"));
    }

    @Test
    public void parse_views_defaultsToPendingAndRejectsExtraArguments() throws Exception {
        FollowUpsCommandParser views = new FollowUpsCommandParser();
        assertParseSuccess(views, " ", new FollowUpsCommand(FollowUpFilter.PENDING));
        for (FollowUpFilter filter : FollowUpFilter.values()) {
            assertParseSuccess(views, filter.name(), new FollowUpsCommand(filter));
        }
        assertEquals(new FollowUpsCommand(FollowUpFilter.OVERDUE),
                new AddressBookParser().parseCommand("followups overdue"));
        assertThrows(ParseException.class, () -> views.parse("unknown"));
        assertThrows(ParseException.class, () -> views.parse("today extra"));
    }
}

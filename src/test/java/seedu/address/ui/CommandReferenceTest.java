package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.StageCommand;
import seedu.address.logic.parser.AddressBookParser;

public class CommandReferenceTest {
    @Test
    public void catalogue_coversAllCommandsAndUsesAuthoritativeSyntax() {
        Set<String> words = CommandReference.entries().stream().map(CommandReference::getWord)
                .collect(Collectors.toSet());
        assertEquals(Set.of("add", "edit", "delete", "view", "find", "findskill", "stage", "remark",
                "list", "clear", "help", "exit"), words);
        assertEquals(StageCommand.MESSAGE_USAGE, CommandReference.forInput("stage 1 s/Interview")
                .orElseThrow().getUsage());
        assertThrows(UnsupportedOperationException.class, () -> CommandReference.entries().clear());
    }

    @Test
    public void search_isCaseInsensitiveAndContextRequiresAnExactWord() {
        assertEquals("stage", CommandReference.forInput("  stage 1").orElseThrow().getWord());
        assertTrue(CommandReference.forInput("sta").isEmpty());
        assertTrue(CommandReference.forInput("STAGE").isEmpty());
        assertTrue(CommandReference.forInput("").isEmpty());
        assertTrue(CommandReference.search("SKILL").stream().anyMatch(entry -> entry.getWord().equals("findskill")));
        assertTrue(CommandReference.search("nonexistentword").isEmpty());
        assertEquals(12, CommandReference.search("  ").size());
    }

    @Test
    public void eachCatalogueWord_isRecognizedByTheParser() {
        for (CommandReference entry : CommandReference.entries()) {
            try {
                new AddressBookParser().parseCommand(entry.getWord());
            } catch (seedu.address.logic.parser.exceptions.ParseException exception) {
                assertTrue(!exception.getMessage().equals(seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND));
            }
        }
    }
}

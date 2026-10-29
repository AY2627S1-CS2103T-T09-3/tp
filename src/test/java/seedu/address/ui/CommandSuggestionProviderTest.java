package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandSuggestionProviderTest {
    private final CommandSuggestionProvider provider = new CommandSuggestionProvider();

    @Test
    public void wordCompletion_preservesArgumentsAndWhitespace() {
        String input = "  vi  Amy Bee";
        CommandSuggestion completion = provider.suggest(input, 4).get(0);
        assertEquals("  view  Amy Bee", completion.apply(input));
        assertEquals(6, completion.getCaretPosition());
        assertEquals("view ", provider.suggest("vi", 2).get(0).apply("vi"));
        assertEquals("view 1", provider.suggest("viex 1", 2).get(0).apply("viex 1"));
    }

    @Test
    public void stageCompletion_isContextualAndPreservesSuffix() {
        String input = "stage Amy Bee s/in r/note";
        int caret = input.indexOf(" r/");
        CommandSuggestion completion = provider.suggest(input, caret).get(0);
        assertEquals("stage Amy Bee s/Interview r/note", completion.apply(input));
        assertEquals(5, provider.suggest("stage 1 s/", 10).size());
        assertTrue(provider.suggest("add n/Amy s/in", 14).isEmpty());
        assertTrue(provider.suggest("findskill s/in", 14).isEmpty());
        assertTrue(provider.suggest("stage 1 s/Interview", 19).isEmpty());
    }

    @Test
    public void blankExactAndUnknownInputs_doNotOfferUnrelatedCompletions() {
        assertTrue(provider.suggest("", 0).isEmpty());
        assertTrue(provider.suggest("   ", 3).isEmpty());
        assertTrue(provider.suggest("list", 4).isEmpty());
        assertTrue(provider.suggest("unknown", 7).isEmpty());
        assertTrue(provider.suggest("view Alice", 10).isEmpty());
    }

    @Test
    public void upstreamCommandsAndStageFilters_offerCurrentCompletions() {
        assertTrue(provider.suggest("fol", 3).stream().anyMatch(suggestion -> suggestion.apply("fol")
                .equals("followup ")));
        assertTrue(provider.suggest("fol", 3).stream().anyMatch(suggestion -> suggestion.apply("fol")
                .equals("followups ")));
        assertEquals("summary ", provider.suggest("sum", 3).get(0).apply("sum"));
        assertEquals("list s/Interview", provider.suggest("list s/in", 9).get(0).apply("list s/in"));
    }

    @Test
    public void invalidRanges_failEarly() {
        assertThrows(NullPointerException.class, () -> provider.suggest(null, 0));
        assertThrows(IllegalArgumentException.class, () -> provider.suggest("view", -1));
        assertThrows(IllegalArgumentException.class, () -> provider.suggest("view", 5));
        assertThrows(IllegalArgumentException.class, () -> new CommandSuggestion(-1, 0, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new CommandSuggestion(2, 1, "", ""));
        assertThrows(IllegalArgumentException.class, () -> new CommandSuggestion(0, 5, "", "").apply("a"));
    }
}

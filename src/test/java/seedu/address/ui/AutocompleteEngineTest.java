package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class AutocompleteEngineTest {

    private final AutocompleteEngine engine = new AutocompleteEngine();

    @Test
    public void commandPrefix_returnsMatchingCommands() {
        List<AutocompleteEngine.Suggestion> suggestions = engine.suggest("li", 2);

        assertEquals(1, suggestions.size());
        assertEquals("list", suggestions.get(0).getReplacement());
        assertEquals(0, suggestions.get(0).getStart());
        assertEquals(2, suggestions.get(0).getEnd());
    }

    @Test
    public void stagePrefix_returnsValidStagesWithoutRemovingArguments() {
        List<AutocompleteEngine.Suggestion> suggestions = engine.suggest("list s/Int", 10);

        assertEquals(1, suggestions.size());
        assertEquals("s/Interview", suggestions.get(0).getReplacement());
        assertEquals(5, suggestions.get(0).getStart());
        assertEquals(10, suggestions.get(0).getEnd());

        List<AutocompleteEngine.Suggestion> afterIndex = engine.suggest("stage 2 s/", 10);
        assertTrue(afterIndex.stream().anyMatch(suggestion -> suggestion.getLabel().equals("s/Applied")));
    }

    @Test
    public void unsupportedArgument_returnsNoSuggestions() {
        assertTrue(engine.suggest("find Al", 7).isEmpty());
        assertTrue(engine.suggest("list sort/", 10).isEmpty());
    }
}

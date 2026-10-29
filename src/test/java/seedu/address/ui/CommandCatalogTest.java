package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandCatalogTest {

    @Test
    public void catalog_usesExistingUsageDefinitions() {
        CommandCatalog.CommandInfo list = CommandCatalog.find("LIST");

        assertNotNull(list);
        assertTrue(list.getUsage().contains("sort/name"));
        assertTrue(list.getGuideText().contains("Quick template:"));
        assertEquals("list sort/name", list.getTemplate());
    }

    @Test
    public void search_matchesCommandsAndExamples() {
        assertEquals("findskill", CommandCatalog.search("findskill").get(0).getCommandWord());
        assertEquals("stage", CommandCatalog.search("Updates the hiring stage").get(0).getCommandWord());
        assertTrue(CommandCatalog.search("does-not-exist").isEmpty());
    }
}

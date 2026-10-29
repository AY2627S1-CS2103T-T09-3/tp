package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedFollowUpTest {
    @Test
    public void saveAndRead_allFollowUpStates_surviveRestart(@TempDir Path directory) throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Pending").build()
                .withFollowUp(new FollowUp("2026-11-05", "Call 日本語", false)));
        book.addPerson(new PersonBuilder().withName("Completed").build()
                .withFollowUp(new FollowUp("2026-10-01", "Sent offer", true)));
        book.addPerson(new PersonBuilder().withName("Empty").build());
        Path file = directory.resolve("followups.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(book), file);
        assertEquals(book, JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).orElseThrow().toModelType());
    }

    @Test
    public void toModelType_oldCandidateWithoutFollowUp_defaultsToEmpty() throws Exception {
        Person original = new PersonBuilder().build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode node =
                (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(json);
        node.remove("followUp");
        assertEquals(original, JsonUtil.fromJsonString(node.toString(), JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_invalidStoredData_rejected() {
        for (JsonAdaptedFollowUp stored : new JsonAdaptedFollowUp[]{
            new JsonAdaptedFollowUp(null, "", false), new JsonAdaptedFollowUp("2026-11-05", null, false),
            new JsonAdaptedFollowUp("2026-11-05", "", null), new JsonAdaptedFollowUp("invalid", "", false),
            new JsonAdaptedFollowUp("2026-02-29", "", false), new JsonAdaptedFollowUp("", "note", false),
            new JsonAdaptedFollowUp("", "", true), new JsonAdaptedFollowUp("2026-11-05", "a".repeat(201), false)}) {
            assertThrows(IllegalValueException.class, stored::toModelType);
        }
    }
}

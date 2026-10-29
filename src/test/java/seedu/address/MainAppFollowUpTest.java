package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.person.FollowUp;
import seedu.address.storage.DataFolderLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

public class MainAppFollowUpTest {
    @Test
    public void init_invalidFollowUp_abortsPreservesCandidatesAndAllowsRecovery(@TempDir Path directory)
            throws Exception {
        Path file = directory.resolve("data/addressbook.json");
        Path prefs = directory.resolve("preferences.json");
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("Alice").build()
                .withFollowUp(new FollowUp("2026-11-05", "Call candidate", false)));
        original.addPerson(new PersonBuilder().withName("Bob").build());
        new JsonAddressBookStorage(file).saveAddressBook(original);
        byte[] validBytes = Files.readAllBytes(file);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(validBytes);
        ObjectNode candidate = (ObjectNode) root.path("persons").get(0);

        for (String reminder : List.of(
                "{\"date\":\"2026-02-30\",\"note\":\"Call\",\"completed\":false}",
                "{\"note\":\"Call\",\"completed\":false}",
                "{\"date\":\"2026-11-05\",\"completed\":false}",
                "{\"date\":\"2026-11-05\",\"note\":\"Call\"}",
                "{\"date\":\"2026-11-05\",\"note\":\"Call\\u2028later\",\"completed\":false}",
                "{\"date\":\"2026-11-05\",\"note\":\"Call\\u2029later\",\"completed\":false}")) {
            candidate.set("followUp", mapper.readTree(reminder));
            Files.writeString(file, root.toString());
            byte[] malformedBytes = Files.readAllBytes(file);
            MainApp app = new MainApp(file, prefs);
            try {
                IllegalStateException exception = assertThrows(IllegalStateException.class, app::init);
                assertTrue(exception.getMessage().contains("Startup was aborted to prevent overwriting it"));
                assertTrue(exception.getCause() instanceof DataLoadingException);
                assertNull(app.model);
                assertNull(app.logic);
                assertNull(app.ui);
                assertArrayEquals(malformedBytes, Files.readAllBytes(file));
                // Failed initialization must release the lock before stop() is called.
                try (DataFolderLock lock = DataFolderLock.tryAcquire(file.getParent()).orElseThrow()) {
                    assertTrue(DataFolderLock.tryAcquire(file.getParent()).isEmpty());
                }
            } finally {
                app.stop();
            }
            assertArrayEquals(malformedBytes, Files.readAllBytes(file));
        }

        Files.write(file, validBytes);
        MainApp recovered = new MainApp(file, prefs);
        try {
            recovered.init();
            assertEquals(original, recovered.model.getAddressBook());
            recovered.logic.execute("list");
            assertEquals(original, new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
            assertArrayEquals(validBytes, Files.readAllBytes(file));
        } finally {
            recovered.stop();
        }
    }
}

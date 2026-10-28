package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;

public class MainAppTest {

    @Test
    public void version_matchesCurrentRelease() {
        assertTrue("V1.5".equals(MainApp.VERSION));
    }

    @Test
    public void initModelManager_legacyIdentityCollision_abortsWithoutChangingData(@TempDir Path directory)
            throws Exception {
        Path addressBookFile = directory.resolve("addressbook.json");
        String conflictingData = "{\n"
                + "  \"persons\": [\n"
                + "    {\"name\": \"John  Doe\", \"phone\": \"123456\","
                + " \"email\": \"\", \"address\": \"\"},\n"
                + "    {\"name\": \"John Doe\", \"phone\": \"123456\","
                + " \"email\": \"\", \"address\": \"\"}\n"
                + "  ]\n"
                + "}\n";
        Files.writeString(addressBookFile, conflictingData, StandardCharsets.UTF_8);
        byte[] originalData = Files.readAllBytes(addressBookFile);

        Storage storage = new StorageManager(new JsonAddressBookStorage(addressBookFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        Method initModelManager = MainApp.class.getDeclaredMethod("initModelManager", Storage.class,
                ReadOnlyUserPrefs.class);
        initModelManager.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                initModelManager.invoke(new MainApp(), storage, new UserPrefs()));
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Startup was aborted to prevent overwriting it"));

        assertArrayEquals(originalData, Files.readAllBytes(addressBookFile));
    }

    @Test
    public void initModelManager_nullCandidate_abortsWithoutChangingData(@TempDir Path directory) throws Exception {
        Path addressBookFile = directory.resolve("addressbook.json");
        String malformedData = "{\n"
                + "  \"persons\": [null]\n"
                + "}\n";
        Files.writeString(addressBookFile, malformedData, StandardCharsets.UTF_8);
        byte[] originalData = Files.readAllBytes(addressBookFile);

        Storage storage = new StorageManager(new JsonAddressBookStorage(addressBookFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        Method initModelManager = MainApp.class.getDeclaredMethod("initModelManager", Storage.class,
                ReadOnlyUserPrefs.class);
        initModelManager.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                initModelManager.invoke(new MainApp(), storage, new UserPrefs()));
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Startup was aborted to prevent overwriting it"));
        assertTrue(cause.getCause() instanceof DataLoadingException);
        assertTrue(cause.getCause().getCause() instanceof IllegalValueException);

        assertArrayEquals(originalData, Files.readAllBytes(addressBookFile));
    }

    @Test
    public void initModelManager_nullPersons_abortsWithoutChangingData(@TempDir Path directory) throws Exception {
        assertMalformedAddressBookAborts("{\n  \"persons\": null\n}\n", directory);
    }

    @Test
    public void initModelManager_missingPersons_abortsWithoutChangingData(@TempDir Path directory) throws Exception {
        assertMalformedAddressBookAborts("{\n}\n", directory);
    }

    @Test
    public void initModelManager_nullJson_abortsWithoutChangingData(@TempDir Path directory) throws Exception {
        assertMalformedAddressBookAborts("null\n", directory);
    }

    private void assertMalformedAddressBookAborts(String malformedData, Path directory) throws Exception {
        Path addressBookFile = directory.resolve("addressbook.json");
        Files.writeString(addressBookFile, malformedData, StandardCharsets.UTF_8);
        byte[] originalData = Files.readAllBytes(addressBookFile);

        Storage storage = new StorageManager(new JsonAddressBookStorage(addressBookFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        Method initModelManager = MainApp.class.getDeclaredMethod("initModelManager", Storage.class,
                ReadOnlyUserPrefs.class);
        initModelManager.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                initModelManager.invoke(new MainApp(), storage, new UserPrefs()));
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Startup was aborted to prevent overwriting it"));
        assertTrue(cause.getCause() instanceof DataLoadingException);
        assertTrue(cause.getCause().getCause() instanceof IllegalValueException);

        assertArrayEquals(originalData, Files.readAllBytes(addressBookFile));
    }

    @Test
    public void initModelManager_nullTag_abortsWithoutChangingData(@TempDir Path directory) throws Exception {
        Path addressBookFile = directory.resolve("addressbook.json");
        String malformedData = "{\n"
                + "  \"persons\": [ {\n"
                + "    \"name\": \"Null Tag Candidate\",\n"
                + "    \"phone\": \"1234567\",\n"
                + "    \"email\": \"\",\n"
                + "    \"address\": \"\",\n"
                + "    \"tags\": [null]\n"
                + "  } ]\n"
                + "}\n";
        Files.writeString(addressBookFile, malformedData, StandardCharsets.UTF_8);
        byte[] originalData = Files.readAllBytes(addressBookFile);

        Storage storage = new StorageManager(new JsonAddressBookStorage(addressBookFile),
                new JsonUserPrefsStorage(directory.resolve("preferences.json")));
        Method initModelManager = MainApp.class.getDeclaredMethod("initModelManager", Storage.class,
                ReadOnlyUserPrefs.class);
        initModelManager.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, () ->
                initModelManager.invoke(new MainApp(), storage, new UserPrefs()));
        Throwable cause = exception.getCause();
        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Startup was aborted to prevent overwriting it"));
        assertTrue(cause.getCause() instanceof DataLoadingException);
        assertTrue(cause.getCause().getCause() instanceof IllegalValueException);

        assertArrayEquals(originalData, Files.readAllBytes(addressBookFile));
    }
}

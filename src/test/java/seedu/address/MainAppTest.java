package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.DataFolderLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.TestProcess;

public class MainAppTest {

    @Test
    public void version_matchesCurrentRelease() {
        assertTrue("V1.5".equals(MainApp.VERSION));
    }

    @Test
    public void start_ownedFolder_startsUi(@TempDir Path directory) throws Exception {
        MainApp app = new MainApp(directory.resolve("data/addressbook.json"), directory.resolve("preferences.json"));
        try {
            app.init();
            AtomicBoolean started = new AtomicBoolean();
            app.ui = stage -> started.set(true);
            app.start(null);
            assertTrue(started.get());
        } finally {
            app.stop();
        }
    }

    @Test
    public void start_competingProcess_warnsAndExitsWithoutChangingFiles(@TempDir Path directory) throws Exception {
        Path dataFolder = directory.resolve("data");
        Files.createDirectories(dataFolder);
        Path dataFile = dataFolder.resolve("addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        Files.writeString(dataFile, "{\"persons\":[]}");
        Files.writeString(prefsFile, "{}");
        byte[] originalData = Files.readAllBytes(dataFile);
        byte[] originalPrefs = Files.readAllBytes(prefsFile);
        try (DataFolderLock owner = DataFolderLock.tryAcquire(dataFolder).orElseThrow()) {
            Process contender = TestProcess.start(MainAppTestProcess.class, dataFile.toString(), prefsFile.toString());
            try {
                String message = CompletableFuture.supplyAsync(() -> {
                    try {
                        return contender.inputReader(StandardCharsets.UTF_8).readLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).get(20, TimeUnit.SECONDS);
                assertEquals(MainApp.MESSAGE_DATA_FOLDER_IN_USE, message);
                assertTrue(contender.waitFor(10, TimeUnit.SECONDS));
                assertEquals(0, contender.exitValue());
                assertArrayEquals(originalData, Files.readAllBytes(dataFile));
                assertArrayEquals(originalPrefs, Files.readAllBytes(prefsFile));
            } finally {
                contender.destroyForcibly();
                assertTrue(contender.waitFor(10, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    public void init_secondInstance_doesNotReadOrWriteFiles(@TempDir Path directory) throws Exception {
        Path dataFolder = directory.resolve("data");
        Files.createDirectories(dataFolder);
        Path dataFile = dataFolder.resolve("addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        Files.writeString(dataFile, "invalid candidate data that must not be read or changed");
        Files.writeString(prefsFile, "invalid preferences that must not be read or changed");
        byte[] originalData = Files.readAllBytes(dataFile);
        byte[] originalPrefs = Files.readAllBytes(prefsFile);

        try (DataFolderLock owner = DataFolderLock.tryAcquire(dataFolder).orElseThrow()) {
            MainApp blocked = new MainApp(dataFile, prefsFile);
            blocked.init();
            blocked.stop();

            assertNull(blocked.model);
            assertNull(blocked.storage);
            assertNull(blocked.ui);
            assertArrayEquals(originalData, Files.readAllBytes(dataFile));
            assertArrayEquals(originalPrefs, Files.readAllBytes(prefsFile));
            assertTrue(DataFolderLock.tryAcquire(dataFolder).isEmpty());
        }
    }

    @Test
    public void init_rejectedSecondInstance_preservesBothCandidatesAfterRestart(@TempDir Path directory)
            throws Exception {
        Path dataFile = directory.resolve("data/addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        MainApp first = new MainApp(dataFile, prefsFile);
        try {
            first.init();
            first.logic.execute("clear");
            first.logic.execute("add n/Maya Rao p/91234567 s/Java");
            byte[] originalData = Files.readAllBytes(dataFile);
            Files.writeString(prefsFile, Files.readString(prefsFile) + "\n");
            byte[] originalPrefs = Files.readAllBytes(prefsFile);

            MainApp second = new MainApp(dataFile, prefsFile);
            second.init();
            second.stop();
            assertNull(second.model);
            assertArrayEquals(originalData, Files.readAllBytes(dataFile));
            assertArrayEquals(originalPrefs, Files.readAllBytes(prefsFile));

            first.logic.execute("add n/Leo Tan p/81234567 s/SQL");
        } finally {
            first.stop();
        }

        MainApp reopened = new MainApp(dataFile, prefsFile);
        try {
            reopened.init();
            assertEquals(List.of("Maya Rao", "Leo Tan"), reopened.model.getAddressBook().getPersonList().stream()
                    .map(person -> person.getName().fullName).toList());
        } finally {
            reopened.stop();
        }
    }

    @Test
    public void init_invalidData_releasesLockForCorrectedStartup(@TempDir Path directory) throws Exception {
        Path dataFile = directory.resolve("data/addressbook.json");
        Files.createDirectories(dataFile.getParent());
        Files.writeString(dataFile, "invalid JSON");
        Path prefsFile = directory.resolve("preferences.json");
        MainApp failed = new MainApp(dataFile, prefsFile);

        assertThrows(IllegalStateException.class, failed::init);
        failed.stop();
        assertEquals("invalid JSON", Files.readString(dataFile));

        Files.writeString(dataFile, "{\"persons\":[]}");
        MainApp reopened = new MainApp(dataFile, prefsFile);
        try {
            reopened.init();
            assertTrue(reopened.model.getAddressBook().getPersonList().isEmpty());
        } finally {
            reopened.stop();
        }
    }

    @Test
    public void init_trailingJson_abortsAndPreservesData(@TempDir Path directory) throws Exception {
        Path dataFile = directory.resolve("data/addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        AddressBook original = getTypicalAddressBook();
        storage.saveAddressBook(original);
        String validData = Files.readString(dataFile);
        for (String suffix : List.of("{invalid-json", "{\"persons\":[]}", "null")) {
            Files.writeString(dataFile, validData + "\n" + suffix);
            byte[] originalBytes = Files.readAllBytes(dataFile);
            MainApp app = new MainApp(dataFile, prefsFile);
            try {
                IllegalStateException exception = assertThrows(IllegalStateException.class, app::init);
                assertTrue(exception.getMessage().contains("Startup was aborted to prevent overwriting it"));
                assertTrue(exception.getCause() instanceof DataLoadingException);
                assertNull(app.model);
            } finally {
                app.stop();
            }
            assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
            try (DataFolderLock lock = DataFolderLock.tryAcquire(dataFile.getParent()).orElseThrow()) {
                assertTrue(DataFolderLock.tryAcquire(dataFile.getParent()).isEmpty());
            }
        }

        Files.writeString(dataFile, validData);
        MainApp reopened = new MainApp(dataFile, prefsFile);
        try {
            reopened.init();
            assertEquals(original, reopened.model.getAddressBook());
        } finally {
            reopened.stop();
        }
    }

    @Test
    public void init_nullGuiSettings_recoversAndPreservesCandidatesAcrossRestart(@TempDir Path directory)
            throws Exception {
        Path dataFile = directory.resolve("data/addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        AddressBook original = getTypicalAddressBook();
        new JsonAddressBookStorage(dataFile).saveAddressBook(original);
        byte[] originalBytes = Files.readAllBytes(dataFile);
        Files.writeString(prefsFile, "{\"guiSettings\":null}");

        MainApp app = new MainApp(dataFile, prefsFile);
        try {
            app.init();
            assertEquals(new UserPrefs(), app.model.getUserPrefs());
            assertEquals(original, app.model.getAddressBook());
        } finally {
            app.stop();
        }

        MainApp reopened = new MainApp(dataFile, prefsFile);
        try {
            reopened.init();
            assertEquals(new UserPrefs(), reopened.model.getUserPrefs());
            assertEquals(original, reopened.model.getAddressBook());
        } finally {
            reopened.stop();
        }
        assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
    }

    @Test
    public void stop_preferencesSaveFails_releasesLock(@TempDir Path directory) throws Exception {
        Path dataFile = directory.resolve("data/addressbook.json");
        Path prefsFile = directory.resolve("preferences.json");
        MainApp app = new MainApp(dataFile, prefsFile);
        try {
            app.init();
            Files.delete(prefsFile);
            Files.createDirectory(prefsFile);
        } finally {
            app.stop();
        }

        try (DataFolderLock reopened = DataFolderLock.tryAcquire(dataFile.getParent()).orElseThrow()) {
            app.stop();
            assertTrue(DataFolderLock.tryAcquire(dataFile.getParent()).isEmpty());
        }
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

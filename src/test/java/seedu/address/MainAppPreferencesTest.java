package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

public class MainAppPreferencesTest {

    @Test
    public void initPrefs_nullJson_recoversAndSavesDefaults(@TempDir Path directory) throws Exception {
        Path preferencesFile = directory.resolve("preferences.json");
        Files.writeString(preferencesFile, "null", StandardCharsets.UTF_8);
        JsonUserPrefsStorage preferencesStorage = new JsonUserPrefsStorage(preferencesFile);

        UserPrefs preferences = new MainApp().initPrefs(preferencesStorage);

        assertEquals(new UserPrefs(), preferences);
        assertEquals(preferences, preferencesStorage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_nullGuiSettings_recoversAndSavesDefaults(@TempDir Path directory) throws Exception {
        assertInvalidPreferencesRecover("{\"guiSettings\":null}", directory);
    }

    @Test
    public void initPrefs_trailingContent_recoversAndSavesDefaults(@TempDir Path directory) throws Exception {
        assertInvalidPreferencesRecover("{\"guiSettings\":{\"windowWidth\":1000}}\n{}", directory);
    }

    private void assertInvalidPreferencesRecover(String invalidPreferences, Path directory) throws Exception {
        Path file = directory.resolve("preferences.json");
        Files.writeString(file, invalidPreferences);
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(file);

        UserPrefs preferences = new MainApp().initPrefs(storage);

        assertEquals(new UserPrefs(), preferences);
        assertEquals(preferences, storage.readUserPrefs().orElseThrow());
    }
}

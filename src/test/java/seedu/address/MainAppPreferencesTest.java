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
}

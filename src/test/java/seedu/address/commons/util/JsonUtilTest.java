package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_nullJson_throwsDataLoadingException(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("null.json");
        String contents = " \n null \t ";
        Files.writeString(file, contents);

        DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(file, SerializableTestClass.class));

        assertTrue(exception.getCause() instanceof IllegalValueException);
        assertEquals(contents, Files.readString(file));
    }

    @Test
    public void fromJsonString_trailingValue_rejected() {
        for (String suffix : List.of("{}", "[]", "null", "true", "123", "\"extra\"")) {
            String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "\n" + suffix;
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class), suffix);
        }
    }

    @Test
    public void fromJsonString_trailingMalformedContent_rejected() {
        for (String suffix : List.of("{invalid-json", "garbage", "]")) {
            String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "\n" + suffix;
            assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class), suffix);
        }
    }

    @Test
    public void fromJsonString_trailingWhitespace_readsCompleteObject() throws IOException {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + " \t\r\n ";

        SerializableTestClass actual = JsonUtil.fromJsonString(json, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), actual.getName());
        assertEquals(SerializableTestClass.getListTestValues(), actual.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), actual.getMapOfIntegerToString());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}

package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.HiringStage;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void saveAndRead_remark_preserved(@TempDir Path directory) throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withRemark("Likes baseball, 日本語").build());
        Path file = directory.resolve("remarks.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(book), file);
        assertEquals(book, JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).get().toModelType());
    }

    @Test
    public void saveAndRead_allHiringStages_preserved(@TempDir Path directory) throws Exception {
        AddressBook book = new AddressBook();
        for (HiringStage stage : HiringStage.values()) {
            book.addPerson(new PersonBuilder().withName("Candidate " + stage)
                    .withStage(stage).withRemark("Notes").build());
        }
        Path file = directory.resolve("stages.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(book), file);
        assertEquals(book, JsonUtil.readJsonFile(file, JsonSerializableAddressBook.class).orElseThrow().toModelType());
    }

    @Test
    public void toModelType_stageFixture_readsNonDefaultStage() throws Exception {
        JsonSerializableAddressBook data = JsonUtil.readJsonFile(
                TEST_DATA_FOLDER.resolve("personWithStageAddressBook.json"),
                JsonSerializableAddressBook.class).orElseThrow();
        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder(TypicalPersons.ALICE).withStage(HiringStage.INTERVIEW).build());
        assertEquals(expected, data.toModelType());
    }

    @Test
    public void toModelType_invalidStageFixture_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook data = JsonUtil.readJsonFile(
                TEST_DATA_FOLDER.resolve("invalidStageAddressBook.json"),
                JsonSerializableAddressBook.class).orElseThrow();
        assertThrows(IllegalValueException.class, HiringStage.MESSAGE_CONSTRAINTS, data::toModelType);
    }

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

}

package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_EMPTY_LIST, model);
    }

    @Test
    public void execute_invalidAdd_doesNotModifyModel() {
        assertParseException(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY,
                AddCommand.MESSAGE_NO_SKILL);
    }

    @Test
    public void execute_summaryAfterFind_countsEntirePipelineAndPreservesResults() throws Exception {
        Person first = new PersonBuilder(AMY).withStage(HiringStage.SCREENED).build();
        Person second = new PersonBuilder().withName("Other Candidate").withStage(HiringStage.INTERVIEW).build();
        model.addPerson(first);
        model.addPerson(second);
        logic.execute("find Amy");

        CommandResult result = logic.execute("summary");

        assertEquals("Hiring-stage summary — all candidates\n"
                + "Applied: 0\nScreened: 1\nInterview: 1\nOffered: 0\nRejected: 0\nTotal: 2",
                result.getFeedbackToUser());
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(List.of(first, second), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_stageCommand_persistsThroughStorageAndAppearsInProfile() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withTags().withSkills().build());
        assertEquals(HiringStage.APPLIED, model.getFilteredPersonList().get(0).getStage());

        CommandResult result = logic.execute("stage 1 s/iNtErViEw");
        assertEquals("Updated Amy Bee's stage to Interview.", result.getFeedbackToUser());
        assertEquals(HiringStage.INTERVIEW, model.getFilteredPersonList().get(0).getStage());
        assertEquals("Amy Bee is already at Interview.",
                logic.execute("stage 1 s/interview").getFeedbackToUser());

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        Logic reloadedLogic = new LogicManager(reloaded, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("reloadedPrefs.json"))));
        assertEquals("Candidate profile:\nName: Amy Bee\nPhone: 11111111\nEmail: amy@example.com\n"
                + "Address: Block 312, Amy Street 1\nTags: None\nRemark: None\n"
                + "Hiring stage: Interview\nSkills: None",
                reloadedLogic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_failedDelete_retryDeletesOriginalTargetOnly() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failNextSave(DUMMY_IO_EXCEPTION);

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertEquals(List.of(AMY, BOB), model.getFilteredPersonList());
        assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));

        logic.execute("delete 1");
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_failedDelete_listDoesNotPersistDeletion() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        storage.failNextSave(DUMMY_AD_EXCEPTION);

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        logic.execute("list");

        assertEquals(List.of(AMY, BOB), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_failedSave_restoresDataAndSearchPredicate() throws Exception {
        for (String command : List.of("edit 1 n/Changed", "clear", "remark 1 r/Changed",
                "stage 1 s/Offered", "add n/Carol p/99999999 s/Java", "list", "find Bob")) {
            model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            FailOnceAddressBookStorage storage = useFailOnceStorage();
            logic.execute("find Amy");
            AddressBook originalData = new AddressBook(model.getAddressBook());
            storage.failNextSave(DUMMY_IO_EXCEPTION);

            assertThrows(CommandException.class, () -> logic.execute(command));
            assertEquals(originalData, model.getAddressBook(), command);
            assertEquals(List.of(AMY), model.getFilteredPersonList(), command);
            assertEquals(originalData, storage.readAddressBook().orElseThrow(), command);

            // The restored filter must keep matching even when the person's other details change.
            logic.execute("stage 1 s/Interview");
            assertEquals(1, model.getFilteredPersonList().size(), command);
            assertEquals(HiringStage.INTERVIEW, model.getFilteredPersonList().get(0).getStage(), command);
        }
    }

    private FailOnceAddressBookStorage useFailOnceStorage() throws IOException {
        FailOnceAddressBookStorage storage = new FailOnceAddressBookStorage(
                temporaryFolder.resolve("rollback.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("rollbackPrefs.json"))));
        return storage;
    }

    /** A real file-backed store that can fail one save before writing anything. */
    private static class FailOnceAddressBookStorage extends JsonAddressBookStorage {
        private IOException nextFailure;

        FailOnceAddressBookStorage(Path path) {
            super(path);
        }

        void failNextSave(IOException failure) {
            nextFailure = failure;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (nextFailure != null) {
                IOException failure = nextFailure;
                nextFailure = null;
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + " s/Java";
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}

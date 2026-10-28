package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

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
import seedu.address.model.person.Skill;
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
    public void execute_readOnlyCommands_doNotAttemptToSave() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failNextSave(DUMMY_AD_EXCEPTION);

        for (String command : List.of("view 1", "summary", "help", "exit", "list sort/name", "list",
                "find Amy", "findskill Java", "list s/Applied")) {
            logic.execute(command);
            assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()), command);
            assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList(), command);
        }

        // The pending failure must still be present: no earlier command called the save method.
        logic.execute("list");
        assertThrows(CommandException.class, () -> logic.execute("add n/Carol p/99999999 s/Java"));
    }

    @Test
    public void execute_unchangedCandidate_doesNotAttemptToSave() throws Exception {
        model.addPerson(AMY);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failNextSave(DUMMY_IO_EXCEPTION);

        for (String command : List.of("stage 1 s/Applied", "edit 1 n/Amy Bee", "remark 1")) {
            logic.execute(command);
            assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()), command);
        }
        assertThrows(CommandException.class, () -> logic.execute("stage 1 s/Interview"));
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sameRemark_preservesSavedFileAndSort() throws Exception {
        assertUnchangedRemarkIsNotSaved("Available", "remark 1 r/Available", "Remark unchanged for Amy Bee.");
    }

    @Test
    public void execute_emptyRemark_preservesSavedFileAndSort() throws Exception {
        assertUnchangedRemarkIsNotSaved("", "remark 1", "Amy Bee has no remark to remove.");
    }

    private void assertUnchangedRemarkIsNotSaved(String remark, String command, String expectedMessage)
            throws Exception {
        Person target = new PersonBuilder(AMY).withRemark(remark).build();
        model.addPerson(target);
        model.addPerson(BOB);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
        logic.execute("list sort/name");
        var comparator = model.getFilteredPersonListComparator();
        List<Person> originalView = List.copyOf(model.getFilteredPersonList());
        storage.failNextSave(DUMMY_AD_EXCEPTION);

        assertEquals(expectedMessage, logic.execute(command).getFeedbackToUser());

        assertEquals(originalView, model.getFilteredPersonList());
        assertSame(target, model.getFilteredPersonList().get(0));
        assertSame(comparator, model.getFilteredPersonListComparator());
        assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));
        // A real change must still attempt to save and receive the pending storage error.
        assertThrows(CommandException.class, () -> logic.execute("remark 1 r/Changed"));
        assertEquals(List.of(target, BOB), model.getAddressBook().getPersonList());
        assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_readOnlyCommand_doesNotCreateDataFile() throws Exception {
        logic.execute("list");
        logic.execute("summary");
        assertFalse(Files.exists(temporaryFolder.resolve("addressBook.json")));
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
    public void execute_editEmptySkill_doesNotModifyModel() {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandFailure("edit 1 n/Updated s/", ParseException.class,
                Skill.MESSAGE_CONSTRAINTS, expectedModel);
    }

    @Test
    public void execute_editMixedValidAndInvalidSkills_doesNotModifyModel() {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandFailure("edit 1 n/Updated s/Java s/", ParseException.class,
                Skill.MESSAGE_CONSTRAINTS, expectedModel);
    }

    @Test
    public void execute_editSkills_persists() throws Exception {
        model.addPerson(new PersonBuilder(AMY).withSkills("Java", "SQL").build());

        logic.execute("edit 1 s/Python s/python s/SQL");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());

        assertEquals(Set.of(new Skill("Python"), new Skill("SQL")),
                reloaded.getFilteredPersonList().get(0).getSkills());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
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
    public void execute_batchDeleteAfterSkillSearch_persistsOnlySelectedDeletions() throws Exception {
        logic.execute("add n/Amy p/11111111 s/Java");
        logic.execute("add n/Bob p/22222222 s/Python");
        logic.execute("add n/Carl p/33333333 s/Java");
        logic.execute("add n/Daniel p/44444444 s/Java");
        List<Person> original = List.copyOf(model.getAddressBook().getPersonList());
        logic.execute("findskill Java");

        logic.execute("delete 3, 1");

        assertEquals(List.of(original.get(2)), model.getFilteredPersonList());
        assertEquals(List.of(original.get(1), original.get(2)), model.getAddressBook().getPersonList());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
    }

    @Test
    public void execute_invalidBatchDelete_preservesDataFilterAndFile() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        model.addPerson(CARL);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        logic.execute("find Amy Carl");
        AddressBook originalData = new AddressBook(model.getAddressBook());
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());

        for (String command : List.of("delete 1,1", "delete 1,", "delete 1,2147483648", "delete 1,Amy")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(originalData, model.getAddressBook());
            assertEquals(List.of(AMY, CARL), model.getFilteredPersonList());
            assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));
        }
        assertThrows(CommandException.class, () -> logic.execute("delete 1,3"));
        assertEquals(originalData, model.getAddressBook());
        assertEquals(List.of(AMY, CARL), model.getFilteredPersonList());
        assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_failedBatchDelete_restoresWholeBatchAndAllowsRetry() throws Exception {
        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BOB);
            model.addPerson(CARL);
            FailOnceAddressBookStorage storage = useFailOnceStorage();
            logic.execute("find Amy Carl");
            byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
            storage.failNextSave(failure);

            assertThrows(CommandException.class, () -> logic.execute("delete 2,1"));

            assertEquals(List.of(AMY, BOB, CARL), model.getAddressBook().getPersonList());
            assertEquals(List.of(AMY, CARL), model.getFilteredPersonList());
            assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));

            logic.execute("delete 2,1");
            assertEquals(List.of(), model.getFilteredPersonList());
            assertEquals(List.of(BOB), storage.readAddressBook().orElseThrow().getPersonList());
        }
    }

    @Test
    public void execute_failedDelete_preservesSortedOrderForRetry() throws Exception {
        model.addPerson(BOB);
        model.addPerson(AMY);
        FailOnceAddressBookStorage storage = useFailOnceStorage();

        logic.execute("list sort/name");
        assertEquals(List.of(AMY, BOB), model.getFilteredPersonList());
        storage.failNextSave(DUMMY_IO_EXCEPTION);

        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertEquals(List.of(AMY, BOB), model.getFilteredPersonList());

        logic.execute("delete 1");
        assertEquals(List.of(BOB), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_failedSaveAfterSkillEdit_restoresDataFilterAndFile() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);
        FailOnceAddressBookStorage storage = useFailOnceStorage();
        logic.execute("find Amy");
        AddressBook originalData = new AddressBook(model.getAddressBook());
        byte[] originalFile = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failNextSave(DUMMY_IO_EXCEPTION);

        assertThrows(CommandException.class, () -> logic.execute("edit 1 s/Java"));

        assertEquals(originalData, model.getAddressBook());
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        assertArrayEquals(originalFile, Files.readAllBytes(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_failedSave_restoresDataAndSearchPredicate() throws Exception {
        for (String command : List.of("edit 1 n/Changed", "clear", "remark 1 r/Changed",
                "stage 1 s/Offered", "add n/Carol p/99999999 s/Java")) {
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

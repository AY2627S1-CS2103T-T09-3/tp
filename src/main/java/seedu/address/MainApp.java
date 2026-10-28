package seedu.address;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.DataFolderLock;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";
    public static final String MESSAGE_DATA_FOLDER_IN_USE =
            "Scoutly is already running with this data folder. Please use the existing window.";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    private final Path addressBookFilePath;
    private final Path userPrefsFilePath;
    private DataFolderLock dataFolderLock;
    private boolean dataFolderInUse;

    /**
     * Creates an application using the files in the current working folder.
     */
    public MainApp() {
        this(ADDRESS_BOOK_FILE_PATH, USER_PREFS_FILE_PATH);
    }

    MainApp(Path addressBookFilePath, Path userPrefsFilePath) {
        requireAllNonNull(addressBookFilePath, userPrefsFilePath);
        this.addressBookFilePath = addressBookFilePath;
        this.userPrefsFilePath = userPrefsFilePath;
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        Optional<DataFolderLock> acquiredLock =
                DataFolderLock.tryAcquire(addressBookFilePath.toAbsolutePath().getParent());
        if (acquiredLock.isEmpty()) {
            dataFolderInUse = true;
            return;
        }
        dataFolderLock = acquiredLock.get();
        try {
            initializeComponents();
        } catch (RuntimeException | Error e) {
            closeResources();
            throw e;
        }
    }

    private void initializeComponents() {
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsFilePath);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookFilePath);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getAddressBookFilePath());
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * An invalid address-book file prevents startup so that it cannot be overwritten with empty data.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            String message = "Data file at " + storage.getAddressBookFilePath()
                    + " could not be loaded. Startup was aborted to prevent overwriting it.";
            logger.severe(message + " " + StringUtil.getDetails(e));
            throw new IllegalStateException(message, e);
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        if (dataFolderInUse) {
            showDataFolderInUseMessage();
            Platform.exit();
            return;
        }
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    private void showDataFolderInUseMessage() {
        logger.warning(MESSAGE_DATA_FOLDER_IN_USE);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Scoutly is already running");
        alert.setHeaderText(null);
        alert.setContentText(MESSAGE_DATA_FOLDER_IN_USE);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping AddressBook ] =============================");
        if (dataFolderLock == null) {
            return;
        }
        closeResources();
    }

    private void closeResources() {
        try (DataFolderLock lock = dataFolderLock) {
            if (model != null) {
                storage.saveUserPrefs(model.getUserPrefs());
            }
        } catch (IOException e) {
            logger.severe("Failed to save preferences or release the data folder lock " + StringUtil.getDetails(e));
        } finally {
            dataFolderLock = null;
        }
    }
}

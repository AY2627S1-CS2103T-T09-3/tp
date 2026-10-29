package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private final Stage primaryStage;
    private final Logic logic;
    private final Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private final HelpWindow helpWindow;
    private CandidateSelectionModel candidateSelection;
    private CommandBox commandBox;
    private CandidateWorkspaceToolbar workspaceToolbar;

    @FXML
    private StackPane dashboardPlaceholder;

    @FXML
    private StackPane workspaceToolbarPlaceholder;

    @FXML
    private StackPane profilePlaceholder;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, requireNonNull(primaryStage));

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = requireNonNull(logic);
        this.dataFilePath = requireNonNull(dataFilePath);

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow(template -> commandBox.insertTemplate(template));
        helpWindow.getRoot().setOnHidden(event -> {
            if (commandBox != null) {
                commandBox.requestFocus();
            }
        });
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.L && event.isShortcutDown() && commandBox != null) {
                commandBox.requestFocus();
                event.consume();
            }
        });
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        candidateSelection = new CandidateSelectionModel(logic.getAllPersons(), logic.getFilteredPersonList());
        workspaceToolbar = new CandidateWorkspaceToolbar(logic, this::showUiFeedback,
                candidateSelection::reconcileVisibility);
        workspaceToolbarPlaceholder.getChildren().add(workspaceToolbar.getRoot());
        dashboardPlaceholder.getChildren().add(
                new PipelineDashboard(logic.getAllPersons(), logic.getFilteredPersonList(),
                        workspaceToolbar::selectStage).getRoot());
        profilePlaceholder.getChildren().add(new CandidateProfilePanel(candidateSelection,
                this::handleQuickAction).getRoot());
        personListPanel = new PersonListPanel(
                logic.getAllPersons(), logic.getFilteredPersonList(), candidateSelection, this::executeCommand);
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        if (primaryStage.isShowing()) {
            commandBox.requestFocus();
        }
    }

    private void showUiFeedback(String message) {
        resultDisplay.setFeedbackToUser(message);
    }

    private void handleQuickAction(CandidateProfilePanel.QuickAction action, Person candidate) {
        int index = logic.getFilteredPersonList().indexOf(candidate) + 1;
        if (index <= 0) {
            showUiFeedback("Select a candidate that is currently displayed first.");
            return;
        }
        switch (action) {
            case EDIT:
                commandBox.insertTemplate("edit " + index + " n/");
                showUiFeedback("Edit template inserted. Complete the fields and press Enter.");
                break;
            case UPDATE_STAGE:
                showStageDialog(index);
                break;
            case ADD_REMARK:
                showRemarkDialog(index);
                break;
            case DELETE:
                confirmDelete(index, candidate);
                break;
            default:
                throw new IllegalStateException("Unknown quick action: " + action);
        }
    }

    private void showStageDialog(int index) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(seedu.address.model.person.HiringStage.APPLIED.toString(),
                Arrays.stream(seedu.address.model.person.HiringStage.values())
                        .map(seedu.address.model.person.HiringStage::toString).toList());
        dialog.setTitle("Update candidate stage");
        dialog.setHeaderText("Choose a new hiring stage");
        dialog.showAndWait().ifPresent(stage -> runQuickCommand("stage " + index + " s/" + stage));
    }

    private void showRemarkDialog(int index) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add candidate remark");
        dialog.setHeaderText("Enter a remark for this candidate");
        dialog.showAndWait().filter(remark -> !remark.isBlank())
                .ifPresent(remark -> runQuickCommand("remark " + index + " r/" + remark));
    }

    private void confirmDelete(int index, Person candidate) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete candidate");
        confirmation.setHeaderText("Delete " + candidate.getName().fullName + "?");
        confirmation.setContentText("This action cannot be undone.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            runQuickCommand("delete " + index);
        }
    }

    private void runQuickCommand(String command) {
        try {
            executeCommand(command);
        } catch (CommandException | ParseException exception) {
            // executeCommand has already displayed the command failure to the user.
        }
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
        if (commandBox != null) {
            commandBox.requestFocus();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        Person previousSelection = candidateSelection.selectedPersonProperty().get();
        try {
            CommandResult commandResult = logic.execute(commandText);
            if (workspaceToolbar != null && commandOwnsDisplayedList(commandText)) {
                workspaceToolbar.clearForCommand();
            }
            commandResult.getProfileTarget().ifPresent(candidateSelection::select);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getProfileTarget()
                    .map(candidate -> "Profile opened: " + candidate.getName())
                    .orElse(commandResult.getFeedbackToUser()));

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            // A failed save rolls back the model. Restore a profile cleared by a temporary
            // deletion/clear as well, rather than leaving the UI in the failed command's state.
            if (previousSelection != null && logic.getFilteredPersonList().contains(previousSelection)) {
                candidateSelection.select(previousSelection);
            }
            resultDisplay.showFailure(e.getMessage());
            throw e;
        } finally {
            candidateSelection.reconcileVisibility();
            personListPanel.refreshSelection();
            if (workspaceToolbar != null) {
                workspaceToolbar.refreshFromModel();
            }
        }
    }

    /** Returns whether a command intentionally replaces the current displayed-list predicate. */
    private boolean commandOwnsDisplayedList(String commandText) {
        String trimmed = commandText.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String commandWord = trimmed.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        return commandWord.equals("find") || commandWord.equals("findskill")
                || commandWord.equals("list") || commandWord.equals("followups");
    }
}

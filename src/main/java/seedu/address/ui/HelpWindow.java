package seedu.address.ui;

import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t09-3.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide: " + USERGUIDE_URL;

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final ObservableList<CommandCatalog.CommandInfo> allCommands =
            FXCollections.observableArrayList(CommandCatalog.getCommands());
    private final FilteredList<CommandCatalog.CommandInfo> filteredCommands = new FilteredList<>(allCommands);
    private Consumer<String> templateInserter = ignored -> { };
    private Runnable returnFocusHandler = () -> { };

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<CommandCatalog.CommandInfo> commandList;

    @FXML
    private TextArea commandDetails;

    @FXML
    private Button insertButton;

    @FXML
    private Button closeButton;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        configureCommandList();
        configureSearch();
        configureActions();
        helpMessage.setText("Offline command reference");
        getRoot().setOnHidden(event -> returnFocusHandler.run());
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
        searchField.requestFocus();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /** Supplies the callback used by the Insert template button. */
    public void setTemplateInserter(Consumer<String> templateInserter) {
        this.templateInserter = templateInserter == null ? ignored -> { } : templateInserter;
    }

    /** Supplies the callback used when the help window closes. */
    public void setReturnFocusHandler(Runnable returnFocusHandler) {
        this.returnFocusHandler = returnFocusHandler == null ? () -> { } : returnFocusHandler;
    }

    /** Returns the currently selected command, if any. */
    public CommandCatalog.CommandInfo getSelectedCommand() {
        return commandList.getSelectionModel().getSelectedItem();
    }

    private void configureCommandList() {
        commandList.setItems(filteredCommands);
        commandList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CommandCatalog.CommandInfo command, boolean empty) {
                super.updateItem(command, empty);
                setText(empty || command == null ? null : command.getCommandWord());
            }
        });
        commandList.getSelectionModel().selectedItemProperty().addListener((unused, oldCommand, newCommand) -> {
            commandDetails.setText(newCommand == null ? "Select a command to see its syntax and examples."
                    : newCommand.getGuideText());
            insertButton.setDisable(newCommand == null);
        });
        if (!allCommands.isEmpty()) {
            commandList.getSelectionModel().selectFirst();
        }
    }

    private void configureSearch() {
        searchField.textProperty().addListener((unused, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            filteredCommands.setPredicate(command -> query.isEmpty()
                    || command.getCommandWord().toLowerCase().contains(query)
                    || command.getSummary().toLowerCase().contains(query)
                    || command.getUsage().toLowerCase().contains(query));
            commandList.getSelectionModel().selectFirst();
        });
    }

    private void configureActions() {
        insertButton.setOnAction(event -> insertSelectedTemplate());
        closeButton.setOnAction(event -> hide());
    }

    private void insertSelectedTemplate() {
        CommandCatalog.CommandInfo selectedCommand = getSelectedCommand();
        if (selectedCommand != null) {
            templateInserter.accept(selectedCommand.getTemplate());
            hide();
        }
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        searchField.requestFocus();
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }
}

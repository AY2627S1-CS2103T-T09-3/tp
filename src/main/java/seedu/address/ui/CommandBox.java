package seedu.address.ui;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    private static final int MAX_SUGGESTIONS = 7;

    private final CommandExecutor commandExecutor;
    private final CommandHistory commandHistory = new CommandHistory();
    private final AutocompleteEngine autocompleteEngine = new AutocompleteEngine();
    private final ContextMenu suggestionPopup = new ContextMenu();
    private final ListView<AutocompleteEngine.Suggestion> suggestionList = new ListView<>();
    private final Consumer<String> commandTextListener;

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        this(commandExecutor, ignored -> { });
    }

    /** Creates a command box with a listener for contextual guide updates. */
    public CommandBox(CommandExecutor commandExecutor, Consumer<String> commandTextListener) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        this.commandTextListener = commandTextListener == null ? ignored -> { } : commandTextListener;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, newText) -> {
            setStyleToDefault();
            commandTextListener.accept(newText);
            refreshSuggestions();
        });
        commandTextField.caretPositionProperty().addListener((unused1, unused2, unused3) -> refreshSuggestions());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        configureSuggestionPopup();
        commandTextField.requestFocus();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.trim().isEmpty()) {
            return;
        }

        commandHistory.record(commandText);
        hideSuggestions();
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
            commandHistory.resetNavigation();
            requestFocus();
        } catch (CommandException | ParseException e) {
            // Keep the failed command visible so the user can correct it directly.
            setStyleToIndicateCommandFailure();
            requestFocus();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            if (suggestionPopup.isShowing()) {
                moveSuggestionSelection(-1);
            } else {
                applyHistoryResult(commandHistory.previous(commandTextField.getText()));
            }
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            if (suggestionPopup.isShowing()) {
                moveSuggestionSelection(1);
            } else {
                applyHistoryResult(commandHistory.next());
            }
            event.consume();
        } else if (event.getCode() == KeyCode.TAB && suggestionPopup.isShowing()) {
            acceptSelectedSuggestion();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            hideSuggestions();
            requestFocus();
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            hideSuggestions();
        }
    }

    private void configureSuggestionPopup() {
        suggestionList.setPrefHeight(150);
        suggestionList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(AutocompleteEngine.Suggestion suggestion, boolean empty) {
                super.updateItem(suggestion, empty);
                setText(empty || suggestion == null ? null : suggestion.getLabel());
            }
        });
        suggestionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                acceptSelectedSuggestion();
            }
        });
        CustomMenuItem menuItem = new CustomMenuItem(suggestionList, false);
        suggestionPopup.getItems().add(menuItem);
        suggestionPopup.setAutoHide(false);
        suggestionPopup.setOnHidden(event -> suggestionList.getSelectionModel().clearSelection());
    }

    private void refreshSuggestions() {
        if (commandTextField == null || commandTextField.getScene() == null) {
            return;
        }
        List<AutocompleteEngine.Suggestion> suggestions = autocompleteEngine.suggest(commandTextField.getText(),
                commandTextField.getCaretPosition());
        if (suggestions.isEmpty()) {
            hideSuggestions();
            return;
        }
        suggestionList.getItems().setAll(suggestions.subList(0, Math.min(MAX_SUGGESTIONS, suggestions.size())));
        suggestionList.getSelectionModel().selectFirst();
        if (!suggestionPopup.isShowing()) {
            Window owner = commandTextField.getScene().getWindow();
            suggestionPopup.show(commandTextField, Side.BOTTOM, 0, 0);
            if (owner != null) {
                owner.requestFocus();
            }
        }
    }

    private void moveSuggestionSelection(int delta) {
        int currentIndex = suggestionList.getSelectionModel().getSelectedIndex();
        int lastIndex = suggestionList.getItems().size() - 1;
        int nextIndex = Math.max(0, Math.min(lastIndex, currentIndex + delta));
        suggestionList.getSelectionModel().select(nextIndex);
        suggestionList.scrollTo(nextIndex);
    }

    private void acceptSelectedSuggestion() {
        AutocompleteEngine.Suggestion suggestion = suggestionList.getSelectionModel().getSelectedItem();
        if (suggestion == null) {
            return;
        }
        commandTextField.replaceText(suggestion.getStart(), suggestion.getEnd(), suggestion.getReplacement());
        commandTextField.positionCaret(suggestion.getStart() + suggestion.getReplacement().length());
        hideSuggestions();
        requestFocus();
    }

    private void applyHistoryResult(CommandHistory.NavigationResult result) {
        if (result.hasMoved()) {
            commandTextField.setText(result.getText());
            commandTextField.positionCaret(commandTextField.getText().length());
        }
        requestFocus();
    }

    private void hideSuggestions() {
        if (suggestionPopup.isShowing()) {
            suggestionPopup.hide();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /** Moves focus to the command field. */
    public void requestFocus() {
        commandTextField.requestFocus();
    }

    /** Replaces the current command with a template selected from offline help. */
    public void insertTemplate(String template) {
        if (template == null) {
            return;
        }
        commandTextField.setText(template);
        commandTextField.positionCaret(template.length());
        requestFocus();
    }

    /** Returns the command currently visible in the input field. */
    public String getCommandText() {
        return commandTextField.getText();
    }

    /** Returns the recorded history for diagnostics and UI tests. */
    public List<String> getCommandHistory() {
        return commandHistory.getEntries();
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}

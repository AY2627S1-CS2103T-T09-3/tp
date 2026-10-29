package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/** Command entry with draft-preserving history, contextual help and token-aware completion. */
public class CommandBox extends UiPart<Region> {
    public static final String ERROR_STYLE_CLASS = "error";

    private final CommandExecutor commandExecutor;
    private final CommandHistory history = new CommandHistory();
    private final CommandSuggestionProvider suggestionProvider = new CommandSuggestionProvider();
    private final CommandAssistPanel assistance;
    private boolean changingInput;
    private boolean browsingHistory;
    private String dismissedInput;

    @FXML
    private TextField commandTextField;
    @FXML
    private StackPane assistancePlaceholder;

    /** Creates an input bound to the same executor used by every documented CLI command. */
    public CommandBox(CommandExecutor commandExecutor) {
        super("CommandBox.fxml");
        this.commandExecutor = requireNonNull(commandExecutor);
        assistance = new CommandAssistPanel(this::acceptSuggestion);
        assistancePlaceholder.getChildren().add(assistance.getRoot());
        commandTextField.textProperty().addListener((observable, previous, current) -> {
            commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
            if (!changingInput) {
                browsingHistory = false;
                dismissedInput = null;
            }
            refreshAssistance();
        });
        commandTextField.caretPositionProperty().addListener((observable, previous, current) -> refreshAssistance());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        refreshAssistance();
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.ESCAPE) {
            dismissedInput = commandTextField.getText();
            assistance.dismiss();
            event.consume();
        } else if (code == KeyCode.TAB && !event.isShiftDown() && assistance.hasSuggestions()) {
            acceptSuggestion(assistance.getSelectedSuggestion());
            event.consume();
        } else if (code == KeyCode.UP || code == KeyCode.DOWN) {
            if (assistance.hasSuggestions() && !event.isControlDown()) {
                assistance.moveSelection(code == KeyCode.UP ? -1 : 1);
            } else {
                browsingHistory = true;
                String input = commandTextField.getText();
                replaceInput(code == KeyCode.UP ? history.previous(input) : history.next(input), -1);
            }
            event.consume();
        }
    }

    private void refreshAssistance() {
        String input = commandTextField.getText();
        int caret = Math.min(input.length(), commandTextField.getCaretPosition());
        List<CommandSuggestion> completions = browsingHistory || input.equals(dismissedInput)
                ? List.of() : suggestionProvider.suggest(input, caret);
        assistance.update(input, completions);
    }

    private void acceptSuggestion(CommandSuggestion suggestion) {
        if (suggestion == null) {
            return;
        }
        String completed = suggestion.apply(commandTextField.getText());
        browsingHistory = false;
        replaceInput(completed, suggestion.getCaretPosition());
        requestFocus();
    }

    private void replaceInput(String input, int caret) {
        changingInput = true;
        try {
            commandTextField.setText(input);
            commandTextField.positionCaret(caret < 0 ? input.length() : caret);
        } finally {
            changingInput = false;
        }
        refreshAssistance();
    }

    /** Inserts a help template for review; inserting never executes it. */
    public void insertTemplate(String template) {
        requireNonNull(template);
        browsingHistory = false;
        dismissedInput = null;
        int nameField = template.indexOf("n/");
        replaceInput(template, nameField < 0 ? template.length() : nameField + 2);
        requestFocus();
    }

    /** Focuses command entry without changing the user's draft or caret position. */
    public void requestFocus() {
        int anchor = commandTextField.getAnchor();
        int caret = commandTextField.getCaretPosition();
        commandTextField.requestFocus();
        // JavaFX may select all on focus gain. Preserve the draft's intended insertion point.
        commandTextField.selectRange(anchor, caret);
    }

    @FXML
    private void handleCommandEntered() {
        String input = commandTextField.getText();
        if (input.isBlank()) {
            return;
        }
        history.record(input);
        browsingHistory = false;
        try {
            commandExecutor.execute(input);
            replaceInput("", 0);
        } catch (CommandException | ParseException exception) {
            // Keep invalid input intact so the recruiter can correct or recall it.
            if (!commandTextField.getStyleClass().contains(ERROR_STYLE_CLASS)) {
                commandTextField.getStyleClass().add(ERROR_STYLE_CLASS);
            }
        }
        requestFocus();
    }

    /** Executes a command and returns feedback through the application's logic boundary. */
    @FunctionalInterface
    public interface CommandExecutor {
        /** Executes input, preserving existing parse and command failure semantics. */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }
}

package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/** Displays syntax and examples for the command currently being typed. */
public class ContextualCommandGuide extends UiPart<Region> {

    private static final String FXML = "ContextualCommandGuide.fxml";
    private static final String DEFAULT_TITLE = "Command guide";
    private static final String DEFAULT_TEXT = "Start typing a command to see its syntax and an example here.";

    @FXML
    private Label guideTitle;

    @FXML
    private TextArea guideText;

    /** Creates a contextual guide with a neutral initial message. */
    public ContextualCommandGuide() {
        super(FXML);
        guideText.setText(DEFAULT_TEXT);
    }

    /** Updates the guide based on the first command word in the input. */
    public void showForInput(String input) {
        String trimmedInput = input == null ? "" : input.trim();
        if (trimmedInput.isEmpty()) {
            showDefault();
            return;
        }

        String commandWord = trimmedInput.split("\\s+", 2)[0];
        CommandCatalog.CommandInfo command = CommandCatalog.find(commandWord);
        if (command == null) {
            guideTitle.setText("Unknown command");
            guideText.setText("No command guide is available for '" + commandWord
                    + "'. Try one of the suggestions in the command box or press F1 for offline help.");
            return;
        }

        guideTitle.setText(command.getCommandWord() + " — " + command.getSummary());
        guideText.setText(command.getGuideText());
        guideText.positionCaret(0);
    }

    /** Shows the neutral guide state. */
    public void showDefault() {
        guideTitle.setText(DEFAULT_TITLE);
        guideText.setText(DEFAULT_TEXT);
        guideText.positionCaret(0);
    }
}

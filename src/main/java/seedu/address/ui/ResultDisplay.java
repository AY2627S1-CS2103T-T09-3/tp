package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final PseudoClass FAILURE = PseudoClass.getPseudoClass("failure");
    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.pseudoClassStateChanged(FAILURE, false);
        resultDisplay.setText("Success: " + feedbackToUser);
    }

    /** Displays errors with both a text label and a distinct appearance. */
    public void showFailure(String message) {
        requireNonNull(message);
        resultDisplay.pseudoClassStateChanged(FAILURE, true);
        resultDisplay.setText("Error: " + message);
    }
}

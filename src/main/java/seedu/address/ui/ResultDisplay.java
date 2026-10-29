package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String SUCCESS_STYLE_CLASS = "success";
    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        setFeedback(feedbackToUser, FeedbackStyle.SUCCESS);
    }

    /** Displays successful command feedback using the success colour scheme. */
    public void setSuccessFeedback(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        setFeedback(feedbackToUser, FeedbackStyle.SUCCESS);
    }

    /** Displays command errors using the error colour scheme. */
    public void setErrorFeedback(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        setFeedback(feedbackToUser, FeedbackStyle.ERROR);
    }

    /** Returns the current feedback text for UI integration tests and accessibility tooling. */
    public String getFeedbackText() {
        return resultDisplay.getText();
    }

    private void setFeedback(String feedbackToUser, FeedbackStyle style) {
        resultDisplay.setText(feedbackToUser);
        resultDisplay.getStyleClass().removeAll(SUCCESS_STYLE_CLASS, ERROR_STYLE_CLASS);
        resultDisplay.getStyleClass().add(style == FeedbackStyle.SUCCESS
                ? SUCCESS_STYLE_CLASS : ERROR_STYLE_CLASS);
        resultDisplay.positionCaret(0);
    }

}

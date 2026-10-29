package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;

/** Read-only, scrollable profile for the currently selected candidate. */
public class CandidateProfilePanel extends UiPart<Region> {
    @FXML
    private Label emptyProfile;
    @FXML
    private ScrollPane profileContent;
    @FXML
    private Label profileName;
    @FXML
    private Label profileStage;
    @FXML
    private Label profileEmail;
    @FXML
    private Label profilePhone;
    @FXML
    private Label profileAddress;
    @FXML
    private Label profileRemark;
    @FXML
    private FlowPane profileTags;
    @FXML
    private FlowPane profileSkills;
    @FXML
    private Button copyEmail;
    @FXML
    private Button copyPhone;
    @FXML
    private Label copyFeedback;

    private Person candidate;

    /** Observes selection rather than retaining an obsolete copy of a candidate. */
    public CandidateProfilePanel(CandidateSelectionModel selection) {
        super("CandidateProfilePanel.fxml");
        requireNonNull(selection).selectedPersonProperty()
                .addListener((observable, previous, current) -> show(current));
        show(selection.selectedPersonProperty().get());
    }

    private void show(Person person) {
        candidate = person;
        boolean hasSelection = person != null;
        profileContent.setVisible(hasSelection);
        profileContent.setManaged(hasSelection);
        emptyProfile.setVisible(!hasSelection);
        emptyProfile.setManaged(!hasSelection);
        copyFeedback.setText("");
        copyFeedback.setVisible(false);
        copyFeedback.setManaged(false);
        if (!hasSelection) {
            return;
        }
        profileContent.setVvalue(0);
        profileName.setText(person.getName().fullName);
        profileStage.setText(person.getStage().toString());
        profileEmail.setText(displayOptional(person.getEmail().value));
        profilePhone.setText(person.getPhone().value);
        profileAddress.setText(displayOptional(person.getAddress().value));
        profileRemark.setText(displayOptional(person.getRemark().value));
        copyEmail.setDisable(person.getEmail().value.isBlank());
        copyPhone.setDisable(person.getPhone().value.isBlank());
        showChips(profileTags, person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName)).map(tag -> tag.tagName).toList());
        showChips(profileSkills, person.getSkills().stream()
                .sorted(Skill.DISPLAY_ORDER).map(skill -> skill.value).toList());
    }

    private String displayOptional(String value) {
        return value.isBlank() ? "Not provided" : value;
    }

    private void showChips(FlowPane pane, List<String> values) {
        pane.getChildren().clear();
        if (values.isEmpty()) {
            pane.getChildren().add(new Label("None"));
            return;
        }
        values.forEach(value -> {
            Label chip = new Label(value);
            chip.getStyleClass().add("chip");
            pane.getChildren().add(chip);
        });
    }

    @FXML
    private void handleCopyEmail() {
        if (candidate != null && !candidate.getEmail().value.isBlank()) {
            copy(candidate.getEmail().value, "Email copied");
        }
    }

    @FXML
    private void handleCopyPhone() {
        if (candidate != null && !candidate.getPhone().value.isBlank()) {
            copy(candidate.getPhone().value, "Phone copied");
        }
    }

    private void copy(String value, String message) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        copyFeedback.setText(Clipboard.getSystemClipboard().setContent(content) ? message : "Copy unavailable");
        copyFeedback.setVisible(true);
        copyFeedback.setManaged(true);
    }
}

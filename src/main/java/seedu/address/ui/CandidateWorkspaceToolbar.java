package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import seedu.address.logic.Logic;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/** Search, filter, export and accessibility controls shared by the candidate workspace. */
public class CandidateWorkspaceToolbar extends UiPart<Region> {
    private static final String ALL_STAGES = "All stages";

    private final Logic logic;
    private final Consumer<String> feedback;
    private boolean toolbarFilterActive;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> stageFilter;
    @FXML
    private Button clearFilters;
    @FXML
    private Button refreshResults;
    @FXML
    private Button exportCsv;
    @FXML
    private Button exportJson;
    @FXML
    private ToggleButton highContrast;
    @FXML
    private Label filterStatus;

    /** Creates workspace controls bound to the shared Logic model. */
    public CandidateWorkspaceToolbar(Logic logic, Consumer<String> feedback) {
        super("CandidateWorkspaceToolbar.fxml");
        this.logic = requireNonNull(logic);
        this.feedback = requireNonNull(feedback);
        stageFilter.getItems().add(ALL_STAGES);
        for (HiringStage stage : HiringStage.values()) {
            stageFilter.getItems().add(stage.toString());
        }
        stageFilter.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((observable, previous, current) -> applyFilter(true));
        stageFilter.valueProperty().addListener((observable, previous, current) -> applyFilter(true));
        updateStatus();
    }

    /** Selects a stage when the user clicks a dashboard tile. */
    public void selectStage(HiringStage stage) {
        requireNonNull(stage);
        stageFilter.getSelectionModel().select(stage.toString());
    }

    /** Re-applies the toolbar filter after a command changes candidate data. */
    public void refreshFromModel() {
        if (toolbarFilterActive) {
            applyFilter(false);
        } else {
            updateStatus();
        }
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        stageFilter.getSelectionModel().selectFirst();
        applyFilter(true);
    }

    @FXML
    private void handleRefresh() {
        refreshFromModel();
        feedback.accept("Candidate list refreshed.");
    }

    @FXML
    private void handleExportCsv() {
        export("Scoutly-candidates.csv", "CSV files", CandidateExportService.toCsv(logic.getFilteredPersonList()));
    }

    @FXML
    private void handleExportJson() {
        export("Scoutly-candidates.json", "JSON files", CandidateExportService.toJson(logic.getFilteredPersonList()));
    }

    @FXML
    private void handleContrastToggle() {
        if (getRoot().getScene() == null) {
            return;
        }
        if (highContrast.isSelected()) {
            getRoot().getScene().getRoot().getStyleClass().add("high-contrast");
            feedback.accept("High contrast mode enabled.");
        } else {
            getRoot().getScene().getRoot().getStyleClass().remove("high-contrast");
            feedback.accept("High contrast mode disabled.");
        }
    }

    private void applyFilter(boolean announce) {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStage = stageFilter.getValue();
        HiringStage stage = ALL_STAGES.equals(selectedStage) ? null : HiringStage.fromString(selectedStage);
        toolbarFilterActive = !query.isEmpty() || stage != null;
        Predicate<Person> predicate = candidate -> matches(candidate, query, stage);
        logic.updateFilteredPersonList(predicate);
        updateStatus();
        if (announce) {
            feedback.accept(filterStatus.getText());
        }
    }

    private boolean matches(Person candidate, String query, HiringStage stage) {
        if (stage != null && candidate.getStage() != stage) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        return candidate.getName().fullName.toLowerCase(Locale.ROOT).contains(query)
                || candidate.getSkills().stream()
                        .anyMatch(skill -> skill.value.toLowerCase(Locale.ROOT).contains(query));
    }

    private void updateStatus() {
        filterStatus.setText(logic.getFilteredPersonList().size() + " candidates displayed");
    }

    private void export(String fileName, String description, String content) {
        if (getRoot().getScene() == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export candidates");
        chooser.setInitialFileName(fileName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, "*." + extension(fileName)));
        java.io.File selected = chooser.showSaveDialog(getRoot().getScene().getWindow());
        if (selected == null) {
            return;
        }
        try {
            Files.writeString(selected.toPath(), content, StandardCharsets.UTF_8);
            feedback.accept("Exported " + logic.getFilteredPersonList().size() + " candidates to "
                    + selected.getName() + ".");
        } catch (IOException exception) {
            feedback.accept("Could not export candidates: " + exception.getMessage());
        }
    }

    private String extension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}

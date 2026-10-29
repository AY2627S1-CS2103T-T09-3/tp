package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.time.LocalDate;
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
import seedu.address.commons.util.FileUtil;
import seedu.address.logic.Logic;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/** Search, filter, export and accessibility controls shared by the candidate workspace. */
public class CandidateWorkspaceToolbar extends UiPart<Region> {
    private static final String ALL_STAGES = "All stages";

    private final Logic logic;
    private final Consumer<String> feedback;
    private final Runnable filterChanged;
    private boolean toolbarFilterActive;
    private boolean suppressFilterChanges;

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
    private ToggleButton dueFollowUps;
    @FXML
    private ToggleButton missingContact;
    @FXML
    private Label filterStatus;

    /** Creates workspace controls bound to the shared Logic model. */
    public CandidateWorkspaceToolbar(Logic logic, Consumer<String> feedback) {
        this(logic, feedback, () -> { });
    }

    /** Creates workspace controls and notifies the owner when visibility may have changed. */
    public CandidateWorkspaceToolbar(Logic logic, Consumer<String> feedback, Runnable filterChanged) {
        super("CandidateWorkspaceToolbar.fxml");
        this.logic = requireNonNull(logic);
        this.feedback = requireNonNull(feedback);
        this.filterChanged = requireNonNull(filterChanged);
        stageFilter.getItems().add(ALL_STAGES);
        for (HiringStage stage : HiringStage.values()) {
            stageFilter.getItems().add(stage.toString());
        }
        stageFilter.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((observable, previous, current) -> applyFilter(true));
        stageFilter.valueProperty().addListener((observable, previous, current) -> applyFilter(true));
        dueFollowUps.selectedProperty().addListener((observable, previous, current) -> applyFilter(true));
        missingContact.selectedProperty().addListener((observable, previous, current) -> applyFilter(true));
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

    /**
     * Gives an explicit list/search command ownership of the displayed predicate.
     *
     * The toolbar controls are cleared without firing intermediate filters, so the command's
     * result remains visible and the controls do not display stale search criteria.
     */
    public void clearForCommand() {
        suppressFilterChanges = true;
        try {
            searchField.clear();
            stageFilter.getSelectionModel().selectFirst();
            dueFollowUps.setSelected(false);
            missingContact.setSelected(false);
        } finally {
            suppressFilterChanges = false;
        }
        toolbarFilterActive = false;
        updateStatus();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        stageFilter.getSelectionModel().selectFirst();
        dueFollowUps.setSelected(false);
        missingContact.setSelected(false);
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
        if (suppressFilterChanges) {
            return;
        }
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStage = stageFilter.getValue();
        HiringStage stage = ALL_STAGES.equals(selectedStage) ? null : HiringStage.fromString(selectedStage);
        toolbarFilterActive = !query.isEmpty() || stage != null || dueFollowUps.isSelected()
                || missingContact.isSelected();
        Predicate<Person> predicate = candidate -> matches(candidate, query, stage, dueFollowUps.isSelected(),
                missingContact.isSelected());
        logic.updateFilteredPersonList(predicate);
        filterChanged.run();
        updateStatus();
        if (announce) {
            feedback.accept(filterStatus.getText());
        }
    }

    private boolean matches(Person candidate, String query, HiringStage stage, boolean dueOnly,
            boolean missingContactOnly) {
        if (stage != null && candidate.getStage() != stage) {
            return false;
        }
        if (dueOnly && !isDueFollowUp(candidate)) {
            return false;
        }
        if (missingContactOnly && !hasMissingContact(candidate)) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        return candidate.getName().fullName.toLowerCase(Locale.ROOT).contains(query)
                || candidate.getSkills().stream()
                        .anyMatch(skill -> skill.value.toLowerCase(Locale.ROOT).contains(query));
    }

    private boolean isDueFollowUp(Person candidate) {
        LocalDate today = LocalDate.now();
        return !candidate.getFollowUp().isCompleted()
                && (candidate.getFollowUp().isOverdue(today)
                || candidate.getFollowUp().getDate().filter(today::equals).isPresent());
    }

    private boolean hasMissingContact(Person candidate) {
        return candidate.getEmail().value.isBlank() || candidate.getPhone().value.isBlank();
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
            FileUtil.writeToFile(selected.toPath(), content);
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

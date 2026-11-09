package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/** Candidate results with model-aligned indexes and explicit profile selection. */
public class PersonListPanel extends UiPart<Region> {
    private final ObservableList<Person> allPersons;
    private final ObservableList<Person> visiblePersons;
    private final CandidateSelectionModel selection;

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label resultCount;
    @FXML
    private Label bulkSelectionCount;
    @FXML
    private Button bulkStage;
    @FXML
    private Button bulkDelete;
    @FXML
    private Button clearSelection;

    private final CommandBox.CommandExecutor commandExecutor;

    /** The candidates the user has chosen for bulk actions, kept independent of list positions. */
    private List<Person> bulkSelection = new ArrayList<>();
    private boolean adjustingSelection;

    /** Creates the list without introducing a second sort or filter. */
    public PersonListPanel(ObservableList<Person> allPersons, ObservableList<Person> visiblePersons,
            CandidateSelectionModel selection) {
        this(allPersons, visiblePersons, selection, null);
    }

    /** Creates the list with optional command execution support for bulk actions. */
    public PersonListPanel(ObservableList<Person> allPersons, ObservableList<Person> visiblePersons,
            CandidateSelectionModel selection, CommandBox.CommandExecutor commandExecutor) {
        super("PersonListPanel.fxml");
        this.allPersons = requireNonNull(allPersons);
        this.visiblePersons = requireNonNull(visiblePersons);
        this.selection = requireNonNull(selection);
        this.commandExecutor = commandExecutor;
        // Registered before the ListView's own listener: JavaFX shifts selected indexes when rows are
        // removed, which can select a candidate the user never chose. Freeze the remembered selection
        // until the list change has been applied, then restore it by candidate rather than by index.
        visiblePersons.addListener((ListChangeListener<Person>) change -> adjustingSelection = true);
        personListView.setItems(visiblePersons);
        visiblePersons.addListener(this::restoreBulkSelection);
        personListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // ListView changes its selection automatically during replacement/removal. Only user gestures
        // feed back into profile state, so deleting a candidate never opens the next candidate by accident.
        personListView.setOnMouseClicked(event -> selectCurrent());
        personListView.setOnKeyReleased(event -> {
            if (isNavigationKey(event.getCode())) {
                selectCurrent();
            }
        });
        selection.selectedPersonProperty().addListener((observable, previous, current) -> showSelection(current));
        visiblePersons.addListener((ListChangeListener<Person>) change -> refreshResults());
        allPersons.addListener((ListChangeListener<Person>) change -> refreshResults());
        personListView.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<Person>) change -> {
                    if (!adjustingSelection) {
                        bulkSelection = new ArrayList<>(personListView.getSelectionModel().getSelectedItems());
                    }
                    refreshBulkControls();
                });
        bulkStage.setDisable(commandExecutor == null);
        bulkDelete.setDisable(commandExecutor == null);
        refreshResults();
        refreshBulkControls();
    }

    /**
     * Re-selects only candidates that were selected before the displayed list changed. A candidate
     * replaced by an edit stays selected; removed or hidden candidates drop out of the selection.
     */
    private void restoreBulkSelection(ListChangeListener.Change<? extends Person> change) {
        Map<Person, Person> replacements = new IdentityHashMap<>();
        while (change.next()) {
            if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                for (int i = 0; i < change.getRemovedSize(); i++) {
                    replacements.put(change.getRemoved().get(i), change.getAddedSubList().get(i));
                }
            }
        }
        List<Person> retained = bulkSelection.stream()
                .map(candidate -> replacements.getOrDefault(candidate, candidate))
                .filter(visiblePersons::contains)
                .toList();
        try {
            personListView.getSelectionModel().clearSelection();
            retained.forEach(candidate -> personListView.getSelectionModel().select(candidate));
        } finally {
            adjustingSelection = false;
        }
        bulkSelection = new ArrayList<>(retained);
        refreshBulkControls();
    }

    private boolean isNavigationKey(KeyCode code) {
        return code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.HOME || code == KeyCode.END
                || code == KeyCode.PAGE_UP || code == KeyCode.PAGE_DOWN || code == KeyCode.ENTER;
    }

    private void selectCurrent() {
        selection.select(personListView.getSelectionModel().getSelectedItem());
    }

    /** Restores the highlight after observable list changes finish notifying their listeners. */
    public void refreshSelection() {
        showSelection(selection.selectedPersonProperty().get());
    }

    /** Clears bulk selection before a command opens a single profile. */
    public void clearBulkSelection() {
        personListView.getSelectionModel().clearSelection();
    }

    private void showSelection(Person candidate) {
        if (candidate == null) {
            personListView.getSelectionModel().clearSelection();
        } else {
            personListView.getSelectionModel().select(candidate);
            personListView.scrollTo(candidate);
        }
    }

    private void refreshResults() {
        resultCount.setText(visiblePersons.size() + " displayed / " + allPersons.size() + " total");
        Label placeholder = new Label(allPersons.isEmpty()
                ? "Your pipeline is empty. Use add to create your first candidate."
                : "No candidates match. Use list to show all candidates.");
        placeholder.setWrapText(true);
        placeholder.getStyleClass().add("muted");
        personListView.setPlaceholder(placeholder);
        refreshBulkControls();
    }

    @FXML
    private void handleBulkStage() {
        if (commandExecutor == null || personListView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(HiringStage.APPLIED.toString(),
                java.util.Arrays.stream(HiringStage.values()).map(HiringStage::toString).toList());
        dialog.setTitle("Update candidate stages");
        dialog.setHeaderText("Choose the stage for the selected candidates");
        Optional<String> chosen = dialog.showAndWait();
        chosen.ifPresent(this::executeBulkStage);
    }

    void executeBulkStage(String stage) {
        if (commandExecutor == null || personListView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        HiringStage targetStage = HiringStage.fromString(requireNonNull(stage));
        List<Person> selected = new ArrayList<>(personListView.getSelectionModel().getSelectedItems());
        String indexes = selected.stream().map(this::displayedIndex)
                .sorted().map(String::valueOf).collect(Collectors.joining(","));
        if (!executeCommand("stage " + indexes + " s/" + targetStage)) {
            return;
        }
        personListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBulkDelete() {
        if (commandExecutor == null || personListView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        javafx.scene.control.Alert confirmation = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete selected candidates");
        confirmation.setHeaderText("Delete " + personListView.getSelectionModel().getSelectedItems().size()
                + " candidates?");
        confirmation.setContentText("This action cannot be undone.");
        if (confirmation.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) {
            return;
        }
        executeBulkDelete();
    }

    /** Executes the selected deletion as one command so LogicManager can save or roll back atomically. */
    void executeBulkDelete() {
        if (commandExecutor == null || personListView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }
        List<Integer> indexes = personListView.getSelectionModel().getSelectedItems().stream()
                .map(this::displayedIndex)
                .sorted()
                .toList();
        if (executeCommand("delete " + indexes.stream().map(String::valueOf)
                .collect(Collectors.joining(",")))) {
            personListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleClearSelection() {
        personListView.getSelectionModel().clearSelection();
        selection.select(null);
    }

    private boolean executeCommand(String command) {
        try {
            commandExecutor.execute(command);
            return true;
        } catch (CommandException | ParseException exception) {
            // The shared MainWindow executor has already displayed the failure feedback.
            return false;
        }
    }

    private int displayedIndex(Person candidate) {
        return visiblePersons.indexOf(candidate) + 1;
    }

    private void refreshBulkControls() {
        int selected = personListView == null ? 0 : personListView.getSelectionModel().getSelectedItems().size();
        bulkSelectionCount.setText(selected + " selected");
        if (commandExecutor != null) {
            bulkStage.setDisable(selected == 0);
            bulkDelete.setDisable(selected == 0);
        }
    }

    /** Reuses JavaFX cells while rebuilding their summary when the candidate or index changes. */
    private class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);
            render();
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            render();
        }

        private void render() {
            setText(null);
            if (isEmpty() || getItem() == null || getIndex() < 0) {
                setGraphic(null);
                return;
            }
            Region card = new PersonCard(getItem(), getIndex() + 1).getRoot();
            // Reserve space for the vertical scrollbar; long names must not widen the list.
            card.setMinWidth(0);
            card.prefWidthProperty().bind(personListView.widthProperty().subtract(24));
            card.maxWidthProperty().bind(card.prefWidthProperty());
            setGraphic(card);
        }
    }
}

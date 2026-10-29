package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
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

    /** Creates the list without introducing a second sort or filter. */
    public PersonListPanel(ObservableList<Person> allPersons, ObservableList<Person> visiblePersons,
            CandidateSelectionModel selection) {
        super("PersonListPanel.fxml");
        this.allPersons = requireNonNull(allPersons);
        this.visiblePersons = requireNonNull(visiblePersons);
        this.selection = requireNonNull(selection);
        personListView.setItems(visiblePersons);
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
        refreshResults();
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

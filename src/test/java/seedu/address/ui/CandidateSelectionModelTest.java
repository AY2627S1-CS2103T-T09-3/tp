package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CandidateSelectionModelTest {
    private final Person first = new PersonBuilder().build();
    private final Person second = new PersonBuilder().withName("Other Candidate").withPhone("99999999").build();
    private final ObservableList<Person> all = FXCollections.observableArrayList(first, second);
    private final FilteredList<Person> visible = new FilteredList<>(all);
    private final CandidateSelectionModel selection = new CandidateSelectionModel(all, visible);

    @Test
    public void replacement_tracksEditedIdentity() {
        selection.select(first);
        Person renamed = new PersonBuilder(first).withName("Renamed Candidate").withPhone("88888888").build();
        all.set(0, renamed);
        assertEquals(renamed, selection.selectedPersonProperty().get());
    }

    @Test
    public void deletion_clearsProfileWithoutSelectingNextCandidate() {
        selection.select(first);
        all.remove(first);
        assertNull(selection.selectedPersonProperty().get());
    }

    @Test
    public void filtering_clearsHiddenProfileAndRejectsHiddenSelection() {
        selection.select(first);
        visible.setPredicate(second::equals);
        selection.reconcileVisibility();
        assertNull(selection.selectedPersonProperty().get());
        assertThrows(IllegalArgumentException.class, () -> selection.select(first));
    }

    @Test
    public void unrelatedChanges_preserveSelectionAndRollbackRestoresProfile() {
        selection.select(first);
        all.set(1, new PersonBuilder(second).withRemark("Updated").build());
        assertEquals(first, selection.selectedPersonProperty().get());
        Person edited = new PersonBuilder(first).withRemark("Unsaved").build();
        all.set(0, edited);
        all.setAll(first, second);
        assertEquals(first, selection.selectedPersonProperty().get());
    }

    @Test
    public void emptySource_andNullArguments_areHandled() {
        selection.select(first);
        all.clear();
        assertNull(selection.selectedPersonProperty().get());
        selection.select(null);
        assertThrows(NullPointerException.class, () -> new CandidateSelectionModel(null, visible));
        assertThrows(NullPointerException.class, () -> new CandidateSelectionModel(all, null));
    }
}

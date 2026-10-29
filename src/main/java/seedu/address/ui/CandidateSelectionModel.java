package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.model.person.Person;

/**
 * Tracks a profile through immutable candidate replacements, deletion and search changes.
 * Uses source-list replacement positions so changing a name or phone does not lose the profile.
 */
public class CandidateSelectionModel {
    private final ObservableList<Person> allPersons;
    private final ObservableList<Person> visiblePersons;
    private final ReadOnlyObjectWrapper<Person> selected = new ReadOnlyObjectWrapper<>();

    /** Creates selection state backed by the application's existing observable lists. */
    public CandidateSelectionModel(ObservableList<Person> allPersons, ObservableList<Person> visiblePersons) {
        this.allPersons = requireNonNull(allPersons);
        this.visiblePersons = requireNonNull(visiblePersons);
        allPersons.addListener(this::handleSourceChange);
    }

    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return selected.getReadOnlyProperty();
    }

    /** Selects only candidates in the displayed list. Null explicitly clears the selection. */
    public void select(Person candidate) {
        if (candidate != null && !visiblePersons.contains(candidate)) {
            throw new IllegalArgumentException("Cannot select a candidate outside the displayed list");
        }
        selected.set(candidate);
    }

    /** Clears profiles hidden by a command's new filter, after command execution finishes. */
    public void reconcileVisibility() {
        if (selected.get() != null && !visiblePersons.contains(selected.get())) {
            selected.set(null);
        }
    }

    private void handleSourceChange(ListChangeListener.Change<? extends Person> change) {
        while (change.next()) {
            Person current = selected.get();
            int removedIndex = change.getRemoved().indexOf(current);
            if (current == null || removedIndex < 0) {
                continue;
            }
            if (change.wasReplaced() && change.getRemovedSize() == change.getAddedSize()) {
                selected.set(change.getAddedSubList().get(removedIndex));
            } else {
                // Reset/rollback may retain the same candidate at another source position.
                selected.set(allPersons.stream().filter(current::isSamePerson).findFirst().orElse(null));
            }
        }
    }
}

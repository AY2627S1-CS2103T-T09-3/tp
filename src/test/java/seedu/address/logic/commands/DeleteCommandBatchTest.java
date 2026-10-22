package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.GEORGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Tests batch deletion against live displayed lists and validates command invariants.
 */
public class DeleteCommandBatchTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_invalidIndexes_throwsException() {
        assertThrows(NullPointerException.class, () -> new DeleteCommand((List<Index>) null));
        assertThrows(NullPointerException.class, () -> new DeleteCommand(Arrays.asList(Index.fromOneBased(1), null)));
        assertThrows(IllegalArgumentException.class, () -> new DeleteCommand(List.<Index>of()));
        assertThrows(IllegalArgumentException.class, () -> command(1, 1));
    }

    @Test
    public void constructor_mutableIndexes_copiesTargets() throws Exception {
        List<Index> indexes = new ArrayList<>(List.of(Index.fromOneBased(1), Index.fromOneBased(3)));
        DeleteCommand command = new DeleteCommand(indexes);
        indexes.clear();
        indexes.add(Index.fromOneBased(2));

        command.execute(model);

        assertEquals(List.of(BENSON, DANIEL, ELLE, FIONA, GEORGE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_nonconsecutiveIndexes_deletesOriginalTargets() throws Exception {
        CommandResult result = command(1, 3, 5).execute(model);

        assertEquals(List.of(BENSON, DANIEL, FIONA, GEORGE), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, DANIEL, FIONA, GEORGE), model.getAddressBook().getPersonList());
        assertEquals("Deleted 3 candidates:\n" + Messages.format(ALICE) + "\n" + Messages.format(CARL)
                + "\n" + Messages.format(ELLE), result.getFeedbackToUser());
    }

    @Test
    public void execute_reversedIndexes_reportsInRequestedOrder() throws Exception {
        CommandResult result = command(7, 1).execute(model);

        assertEquals(List.of(BENSON, CARL, DANIEL, ELLE, FIONA), model.getFilteredPersonList());
        assertEquals("Deleted 2 candidates:\n" + Messages.format(GEORGE) + "\n" + Messages.format(ALICE),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_allDisplayedCandidates_keepsHiddenCandidatesAndFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().fullName.contains("Meier"));
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());

        command(1, 2).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(ALICE, CARL, ELLE, FIONA, GEORGE), model.getAddressBook().getPersonList());
        assertTrue(model.getFilteredPersonListPredicate().test(BENSON));
        assertFalse(model.getFilteredPersonListPredicate().test(ALICE));
    }

    @Test
    public void execute_allCandidates_leavesEmptyAddressBook() throws Exception {
        command(1, 2, 3, 4, 5, 6, 7).execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_invalidLaterIndex_deletesNothing() {
        assertCommandFailure(command(1, 8), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(command(8, 1), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexOnlyValidInFullList_deletesNothing() {
        model.updateFilteredPersonList(person -> person.getName().fullName.contains("Meier"));

        assertCommandFailure(command(1, 3), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertTrue(model.getFilteredPersonListPredicate().test(BENSON));
        assertFalse(model.getFilteredPersonListPredicate().test(ALICE));
    }

    @Test
    public void execute_emptyDisplayedList_deletesNothing() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(command(1, 2), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(command(1, 2), new ModelManager(), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_sortedFilteredView_usesDisplayedIndexesAndPreservesOrder() throws Exception {
        SortedViewModel sortedModel = new SortedViewModel();
        sortedModel.updateFilteredPersonList(person -> !person.equals(GEORGE));
        assertEquals(List.of(FIONA, ELLE, DANIEL, CARL, BENSON, ALICE), sortedModel.getFilteredPersonList());
        Comparator<? super Person> comparator = sortedModel.displayedPersons.getComparator();

        command(1, 3).execute(sortedModel);

        assertEquals(List.of(ELLE, CARL, BENSON, ALICE), sortedModel.getFilteredPersonList());
        assertEquals(List.of(ALICE, BENSON, CARL, ELLE, GEORGE), sortedModel.getAddressBook().getPersonList());
        assertSame(comparator, sortedModel.displayedPersons.getComparator());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command(1, 2).execute(null));
    }

    @Test
    public void equals_comparesTargetsAndFeedbackOrder() {
        assertEquals(command(1, 3), command(1, 3));
        assertFalse(command(1, 3).equals(command(3, 1)));
        assertFalse(command(1, 3).equals(command(1, 2)));
        assertFalse(command(1, 3).equals(command(1)));
        assertFalse(command(1, 3).equals(new DeleteCommand(new Name("Alice Pauline"))));
        assertEquals(new DeleteCommand(Index.fromOneBased(1)), command(1));
    }

    @Test
    public void toString_multipleIndexes() {
        List<Index> indexes = List.of(Index.fromOneBased(1), Index.fromOneBased(3));
        assertEquals(DeleteCommand.class.getCanonicalName() + "{targetIndexes=" + indexes + "}",
                new DeleteCommand(indexes).toString());
    }

    private static DeleteCommand command(int... indexes) {
        return new DeleteCommand(Arrays.stream(indexes).mapToObj(Index::fromOneBased).toList());
    }

    /**
     * Supplies a live sorted view without depending on the separate list-sorting feature branch.
     */
    private static class SortedViewModel extends ModelManager {
        private final SortedList<Person> displayedPersons;

        SortedViewModel() {
            super(getTypicalAddressBook(), new UserPrefs());
            displayedPersons = new SortedList<>(super.getFilteredPersonList(),
                    Comparator.comparing((Person person) -> person.getName().fullName).reversed());
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return displayedPersons;
        }
    }
}

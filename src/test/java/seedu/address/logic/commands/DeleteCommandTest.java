package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validNameUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(personToDelete.getName());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model,
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                expectedModel);
    }

    @Test
    public void execute_nameWithDifferentCase_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(new Name("aLiCe PaUlInE"));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model,
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                expectedModel);
    }

    @Test
    public void execute_validNameFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToDelete = model.getFilteredPersonList().get(0);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(new DeleteCommand(personToDelete.getName()), model,
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                expectedModel);
    }

    @Test
    public void execute_nameOutsideFilteredList_throwsCommandException() {
        Name hiddenName = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()).getName();
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DeleteCommand(hiddenName), model, DeleteCommand.MESSAGE_NAME_NOT_FOUND);
    }

    @Test
    public void execute_missingOrPartialName_throwsCommandException() {
        assertCommandFailure(new DeleteCommand(new Name("Nobody Here")), model, DeleteCommand.MESSAGE_NAME_NOT_FOUND);
        assertCommandFailure(new DeleteCommand(new Name("Alice")), model, DeleteCommand.MESSAGE_NAME_NOT_FOUND);
    }

    @Test
    public void execute_nameInEmptyList_throwsCommandException() {
        showNoPerson(model);

        assertCommandFailure(new DeleteCommand(new Name("Alice Pauline")), model, DeleteCommand.MESSAGE_NAME_NOT_FOUND);
    }

    @Test
    public void execute_ambiguousName_throwsCommandException() {
        model.addPerson(new PersonBuilder().withName("ALICE PAULINE").build());

        assertCommandFailure(new DeleteCommand(new Name("Alice Pauline")), model, DeleteCommand.MESSAGE_AMBIGUOUS_NAME);
    }

    @Test
    public void execute_ambiguousNameOutsideFilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.addPerson(new PersonBuilder().withName("ALICE PAULINE").build());
        model.updateFilteredPersonList(person -> person.equals(personToDelete));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(new DeleteCommand(personToDelete.getName()), model,
                String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)),
                expectedModel);
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    @Test
    public void equals_nameTarget() {
        DeleteCommand command = new DeleteCommand(new Name("Alice Pauline"));
        assertTrue(command.equals(new DeleteCommand(new Name("Alice Pauline"))));
        assertFalse(command.equals(new DeleteCommand(new Name("Benson Meier"))));
        assertFalse(command.equals(new DeleteCommand(INDEX_FIRST_PERSON)));
        assertFalse(new DeleteCommand(INDEX_FIRST_PERSON).equals(command));
    }

    @Test
    public void toStringMethod_nameTarget() {
        Name targetName = new Name("Alice Pauline");
        DeleteCommand deleteCommand = new DeleteCommand(targetName);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetName=" + targetName + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
}

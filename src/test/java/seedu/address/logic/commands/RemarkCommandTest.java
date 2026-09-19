package seedu.address.logic.commands;

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
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {

    @Test
    public void execute_validRemark_updatesPersonAndPreservesOtherFields() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withRemark("Likes swimming").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes swimming")), model,
                String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson)),
                expectedModel);
        assertTrue(model.getAddressBook().getPersonList().contains(editedPerson));
        assertEqualsExceptRemark(personToEdit, editedPerson);
    }

    @Test
    public void execute_emptyRemark_removesRemark() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithRemark = new PersonBuilder(personToEdit).withRemark("Likes swimming").build();
        model.setPerson(personToEdit, personWithRemark);
        Person editedPerson = new PersonBuilder(personWithRemark).withRemark("").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithRemark, editedPerson);

        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")), model,
                String.format(RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS, Messages.format(editedPerson)),
                expectedModel);
    }

    @Test
    public void execute_filteredList_updatesSelectedPerson() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person personToEdit = model.getFilteredPersonList().get(0);
        Person editedPerson = new PersonBuilder(personToEdit).withRemark("Filtered remark").build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Filtered remark")), model,
                String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(editedPerson)),
                expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new RemarkCommand(outOfBoundIndex, new Remark("Likes swimming")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand firstCommand = new RemarkCommand(Index.fromOneBased(1), new Remark("Likes swimming"));
        RemarkCommand secondCommand = new RemarkCommand(Index.fromOneBased(1), new Remark("Likes swimming"));

        assertTrue(firstCommand.equals(secondCommand));
        assertTrue(firstCommand.equals(firstCommand));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(new ClearCommand()));
        assertFalse(firstCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes swimming"))));
        assertFalse(firstCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Different remark"))));
    }

    /**
     * Confirms that changing a remark leaves all other person fields unchanged.
     */
    private void assertEqualsExceptRemark(Person original, Person updated) {
        assertTrue(original.getName().equals(updated.getName()));
        assertTrue(original.getPhone().equals(updated.getPhone()));
        assertTrue(original.getEmail().equals(updated.getEmail()));
        assertTrue(original.getAddress().equals(updated.getAddress()));
        assertTrue(original.getTags().equals(updated.getTags()));
        assertFalse(original.getRemark().equals(updated.getRemark()));
    }
}

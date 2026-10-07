package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ViewCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_showsAllDetailsWithoutChangingModel() {
        Person candidate = new PersonBuilder().withTags("Java", "Backend")
                .withRemark("Available in November").withStage(HiringStage.INTERVIEW).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(candidate);
        model = new ModelManager(addressBook, new UserPrefs());
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());

        String expected = "Candidate profile:\nName: Amy Bee\nPhone: 85355255\nEmail: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\nTags: Backend, Java\nRemark: Available in November\n"
                + "Hiring stage: Interview";
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expected, expectedModel);
    }

    @Test
    public void execute_emptyOptionalFields_showsNone() {
        Person candidate = new PersonBuilder().build();
        model.addPerson(candidate);
        Index lastIndex = Index.fromOneBased(model.getFilteredPersonList().size());
        String expected = "Candidate profile:\nName: Amy Bee\nPhone: 85355255\nEmail: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\nTags: None\nRemark: None\nHiring stage: Applied";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ViewCommand(lastIndex), model, expected, expectedModel);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        String expected = "Candidate profile:\nName: Benson Meier\nPhone: 98765432\n"
                + "Email: johnd@example.com\nAddress: 311, Clementi Ave 2, #02-25\n"
                + "Tags: friends, owesMoney\nRemark: None\nHiring stage: Applied";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expected, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ViewCommand(invalidIndex), model,
                ViewCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidFilteredIndex_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                ViewCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model.updateFilteredPersonList(person -> false);
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), model,
                ViewCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        Model emptyModel = new ModelManager();
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), emptyModel,
                ViewCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals() {
        ViewCommand first = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(first.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(new ListCommand()));
    }

    @Test
    public void toStringMethod() {
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}",
                command.toString());
    }
}

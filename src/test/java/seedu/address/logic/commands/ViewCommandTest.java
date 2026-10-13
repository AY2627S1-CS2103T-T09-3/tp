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
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Name;
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
        assertThrows(NullPointerException.class, () -> new ViewCommand((Index) null));
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void execute_fullName_ignoresCaseAndPreservesFilteredList() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        CommandResult expected = new ViewCommand(INDEX_FIRST_PERSON).execute(model);

        assertCommandSuccess(new ViewCommand(new Name("bEnSoN mEiEr")), model, expected, expectedModel);
    }

    @Test
    public void execute_nameNotFound_throwsCommandException() {
        assertCommandFailure(new ViewCommand(new Name("Unknown Candidate")), model,
                ViewCommand.MESSAGE_NAME_NOT_FOUND);
        assertCommandFailure(new ViewCommand(new Name("Alice")), model,
                ViewCommand.MESSAGE_NAME_NOT_FOUND);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertCommandFailure(new ViewCommand(new Name("Alice Pauline")), model,
                ViewCommand.MESSAGE_NAME_NOT_FOUND);
        assertCommandFailure(new ViewCommand(new Name("Alice Pauline")), new ModelManager(),
                ViewCommand.MESSAGE_NAME_NOT_FOUND);
    }

    @Test
    public void execute_ambiguousName_requiresIndex() throws Exception {
        Person first = new PersonBuilder().withName("Amy Bee").build();
        Person second = new PersonBuilder().withName("AMY BEE").withEmail("other@example.com").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(first);
        addressBook.addPerson(second);
        model = new ModelManager(addressBook, new UserPrefs());
        assertCommandFailure(new ViewCommand(new Name("amy bee")), model, ViewCommand.MESSAGE_AMBIGUOUS_NAME);
        assertTrue(new ViewCommand(INDEX_SECOND_PERSON).execute(model).getFeedbackToUser()
                .contains("Email: other@example.com"));

        model.updateFilteredPersonList(person -> person.getEmail().equals(first.getEmail()));
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.updateFilteredPersonList(person -> person.getEmail().equals(first.getEmail()));
        CommandResult expected = new ViewCommand(INDEX_FIRST_PERSON).execute(model);
        assertCommandSuccess(new ViewCommand(new Name("amy bee")), model, expected, expectedModel);
    }

    @Test
    public void execute_stageThenViewByName_displaysLatestStage() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        for (HiringStage stage : HiringStage.values()) {
            new StageCommand(INDEX_FIRST_PERSON, stage).execute(model);
            CommandResult result = new ViewCommand(new Name("Benson Meier")).execute(model);
            assertTrue(result.getFeedbackToUser().endsWith("Hiring stage: " + stage));
        }
    }

    @Test
    public void execute_candidateWorkflow_viewsUpdatedProfileAndReportsDeletedCandidate() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        Model workflowModel = new ModelManager();
        parser.parseCommand("add n/Amy Bee p/85355255 e/amy@gmail.com a/Jurong t/Java")
                .execute(workflowModel);
        parser.parseCommand("list").execute(workflowModel);
        parser.parseCommand("remark 1 r/Ready for interview").execute(workflowModel);
        parser.parseCommand("stage 1 s/Interview").execute(workflowModel);
        Model expectedModel = new ModelManager(workflowModel.getAddressBook(), new UserPrefs());
        String expected = "Candidate profile:\nName: Amy Bee\nPhone: 85355255\nEmail: amy@gmail.com\n"
                + "Address: Jurong\nTags: Java\nRemark: Ready for interview\nHiring stage: Interview";
        assertCommandSuccess(parser.parseCommand("view amy bee"), workflowModel, expected, expectedModel);
        parser.parseCommand("delete Amy Bee").execute(workflowModel);
        assertCommandFailure(parser.parseCommand("view Amy Bee"), workflowModel, ViewCommand.MESSAGE_NAME_NOT_FOUND);
    }

    @Test
    public void nameConstructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand((Name) null));
    }

    @Test
    public void nameCommand_equalsAndToString() {
        Name name = new Name("Alice Pauline");
        ViewCommand command = new ViewCommand(name);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ViewCommand(name)));
        assertFalse(command.equals(new ViewCommand(new Name("Benson Meier"))));
        assertFalse(command.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(new ViewCommand(INDEX_FIRST_PERSON).equals(command));
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetName=" + name + "}", command.toString());
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

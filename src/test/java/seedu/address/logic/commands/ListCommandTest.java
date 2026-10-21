package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_reportsCandidateCount() {
        assertCommandSuccess(new ListCommand(), model, "Listed 7 candidates.", expectedModel);
    }

    @Test
    public void execute_listIsFiltered_reportsAllCandidates() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, "Listed 7 candidates.", expectedModel);
    }

    @Test
    public void execute_emptyList_reportsZeroCandidates() {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());
        Model expectedEmptyModel = new ModelManager(new AddressBook(), new UserPrefs());

        assertCommandSuccess(new ListCommand(), emptyModel, ListCommand.MESSAGE_EMPTY_LIST, expectedEmptyModel);
    }

    @Test
    public void execute_oneCandidate_reportsSingularCandidate() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        Model oneCandidateModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedOneCandidateModel = new ModelManager(addressBook, new UserPrefs());

        assertCommandSuccess(new ListCommand(), oneCandidateModel,
                "Listed 1 candidate.", expectedOneCandidateModel);
    }

    @Test
    public void execute_sortByName_listsCandidatesAlphabetically() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(BOB);
        addressBook.addPerson(ALICE);
        Model unsortedModel = new ModelManager(addressBook, new UserPrefs());

        new ListCommand(true).execute(unsortedModel);

        assertEquals(List.of(ALICE, BOB), unsortedModel.getFilteredPersonList());
        assertEquals(List.of(BOB, ALICE), unsortedModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortByName_isCaseInsensitive() {
        Person lowercaseAlice = new PersonBuilder().withName("alice").withPhone("11111111").build();
        Person uppercaseBob = new PersonBuilder().withName("Bob").withPhone("22222222").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(uppercaseBob);
        addressBook.addPerson(lowercaseAlice);
        Model mixedCaseModel = new ModelManager(addressBook, new UserPrefs());

        new ListCommand(true).execute(mixedCaseModel);

        assertEquals(List.of(lowercaseAlice, uppercaseBob), mixedCaseModel.getFilteredPersonList());
    }

    @Test
    public void execute_listAfterFind_resetsSortComparator() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(BOB);
        addressBook.addPerson(ALICE);
        Model modelWithSort = new ModelManager(addressBook, new UserPrefs());

        new ListCommand(true).execute(modelWithSort);
        assertEquals(List.of(ALICE, BOB), modelWithSort.getFilteredPersonList());

        new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob"))).execute(modelWithSort);
        assertEquals(List.of(BOB, ALICE), modelWithSort.getFilteredPersonList());

        new ListCommand().execute(modelWithSort);
        assertEquals(List.of(BOB, ALICE), modelWithSort.getFilteredPersonList());
    }

    @Test
    public void execute_sortByName_emptyList() throws Exception {
        Model emptyModel = new ModelManager(new AddressBook(), new UserPrefs());

        assertCommandSuccess(new ListCommand(true), emptyModel, ListCommand.MESSAGE_EMPTY_LIST,
                new ModelManager(new AddressBook(), new UserPrefs()));
    }
}

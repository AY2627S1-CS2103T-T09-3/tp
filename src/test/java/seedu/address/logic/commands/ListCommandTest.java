package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
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
    public void execute_stageFilter_reportsMatchingCandidates() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(new PersonBuilder(ALICE).withStage(HiringStage.INTERVIEW).build());
        addressBook.addPerson(new PersonBuilder(BOB).withStage(HiringStage.APPLIED).build());
        Model stageModel = new ModelManager(addressBook, new UserPrefs());
        Model expectedStageModel = new ModelManager(addressBook, new UserPrefs());
        expectedStageModel.updateFilteredPersonList(person -> person.getStage() == HiringStage.INTERVIEW);

        assertCommandSuccess(new ListCommand(HiringStage.INTERVIEW), stageModel,
                "Listed 1 candidate.", expectedStageModel);
    }

    @Test
    public void execute_stageFilterWithNoMatches_reportsNoMatches() {
        Model expectedStageModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedStageModel.updateFilteredPersonList(person -> person.getStage() == HiringStage.INTERVIEW);

        assertCommandSuccess(new ListCommand(HiringStage.INTERVIEW), model,
                "No candidates found at the Interview stage.", expectedStageModel);
    }
}

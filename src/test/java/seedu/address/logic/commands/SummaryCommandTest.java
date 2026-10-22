package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SummaryCommandTest {

    private static final String EMPTY_SUMMARY = "Hiring-stage summary — all candidates\n"
            + "Applied: 0\nScreened: 0\nInterview: 0\nOffered: 0\nRejected: 0\nTotal: 0";
    private static final String MIXED_SUMMARY = "Hiring-stage summary — all candidates\n"
            + "Applied: 2\nScreened: 1\nInterview: 1\nOffered: 1\nRejected: 1\nTotal: 6";

    @Test
    public void execute_emptyPipeline_reportsAllZeroCounts() {
        assertCommandSuccess(new SummaryCommand(), new ModelManager(), EMPTY_SUMMARY, new ModelManager());
    }

    @Test
    public void execute_mixedStages_reportsCountsInPipelineOrder() {
        Model model = createMixedPipeline();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new SummaryCommand(), model, MIXED_SUMMARY, expected);
    }

    @Test
    public void execute_filteredList_countsAllCandidatesAndPreservesFilter() {
        Model model = createMixedPipeline();
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        Predicate<Person> filter = candidate -> candidate.getStage() == HiringStage.INTERVIEW;
        model.updateFilteredPersonList(filter);
        expected.updateFilteredPersonList(filter);
        assertCommandSuccess(new SummaryCommand(), model, MIXED_SUMMARY, expected);
    }

    @Test
    public void execute_noSearchResults_stillCountsStoredCandidates() {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withStage(HiringStage.OFFERED).build());
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        Predicate<Person> filter = candidate -> false;
        model.updateFilteredPersonList(filter);
        expected.updateFilteredPersonList(filter);
        String message = "Hiring-stage summary — all candidates\n"
                + "Applied: 0\nScreened: 0\nInterview: 0\nOffered: 1\nRejected: 0\nTotal: 1";

        assertCommandSuccess(new SummaryCommand(), model, message, expected);
    }

    @Test
    public void execute_afterCandidateChanges_recalculatesCounts() throws Exception {
        Model model = new ModelManager();
        Person original = new PersonBuilder().build();
        model.addPerson(original);
        SummaryCommand command = new SummaryCommand();
        assertEquals("Hiring-stage summary — all candidates\n"
                + "Applied: 1\nScreened: 0\nInterview: 0\nOffered: 0\nRejected: 0\nTotal: 1",
                command.execute(model).getFeedbackToUser());

        new StageCommand(INDEX_FIRST_PERSON, HiringStage.INTERVIEW).execute(model);
        assertEquals("Hiring-stage summary — all candidates\n"
                + "Applied: 0\nScreened: 0\nInterview: 1\nOffered: 0\nRejected: 0\nTotal: 1",
                command.execute(model).getFeedbackToUser());

        model.deletePerson(model.getFilteredPersonList().get(0));
        assertEquals(EMPTY_SUMMARY, command.execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SummaryCommand().execute(null));
    }

    private Model createMixedPipeline() {
        Model model = new ModelManager();
        for (HiringStage stage : HiringStage.values()) {
            model.addPerson(new PersonBuilder().withName("Candidate " + stage).withStage(stage).build());
        }
        model.addPerson(new PersonBuilder().withName("Another Applied Candidate").build());
        return model;
    }
}

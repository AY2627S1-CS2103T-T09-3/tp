package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpCommand.Action;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.FollowUpFilter;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class FollowUpCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private final FollowUp reminder = new FollowUp("2026-11-05", "Call candidate", false);

    @Test
    public void execute_scheduleCompleteRescheduleClear_preservesCandidateDetails() throws Exception {
        Person original = model.getFilteredPersonList().getFirst();
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        assertEquals(original.withFollowUp(reminder), model.getFilteredPersonList().getFirst());
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.COMPLETE, FollowUp.empty()).execute(model);
        assertEquals(original.withFollowUp(reminder.complete()), model.getFilteredPersonList().getFirst());
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        assertFalse(model.getFilteredPersonList().getFirst().getFollowUp().isCompleted());
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.CLEAR, FollowUp.empty()).execute(model);
        assertEquals(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndRestoresAllCandidates() throws Exception {
        Person target = model.getFilteredPersonList().get(1);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        assertEquals(target.withFollowUp(reminder), model.getAddressBook().getPersonList().get(1));
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndexOrCompletion_doesNotChangeData() throws Exception {
        assertCommandFailure(new FollowUpCommand(INDEX_FIRST_PERSON, Action.COMPLETE, FollowUp.empty()),
                model, FollowUpCommand.MESSAGE_NO_FOLLOW_UP);
        Index invalid = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new FollowUpCommand(invalid, Action.SET, reminder), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new FollowUpCommand(INDEX_SECOND_PERSON, Action.SET, reminder), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.COMPLETE, FollowUp.empty()).execute(model);
        assertCommandFailure(new FollowUpCommand(INDEX_FIRST_PERSON, Action.COMPLETE, FollowUp.empty()),
                model, FollowUpCommand.MESSAGE_ALREADY_COMPLETED);
    }

    @Test
    public void execute_editAndRemark_preserveFollowUp() throws Exception {
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPhone(new Phone("12345678"));
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Keep reminder")).execute(model);
        assertEquals(reminder, model.getFilteredPersonList().getFirst().getFollowUp());
    }

    @Test
    public void execute_followUpAndOtherEdits_preserveStageSkillsAndReminder() throws Exception {
        Person original = new PersonBuilder(model.getFilteredPersonList().getFirst())
                .withStage(HiringStage.INTERVIEW).withSkills("Java", "SQL").build();
        model.setPerson(model.getFilteredPersonList().getFirst(), original);
        new FollowUpCommand(INDEX_FIRST_PERSON, Action.SET, reminder).execute(model);
        assertEquals(original.withFollowUp(reminder), model.getFilteredPersonList().getFirst());
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPhone(new Phone("12345678"));
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Keep reminder")).execute(model);
        new StageCommand(INDEX_FIRST_PERSON, HiringStage.OFFERED).execute(model);
        Person updated = model.getFilteredPersonList().getFirst();
        assertEquals(reminder, updated.getFollowUp());
        assertEquals(original.getSkills(), updated.getSkills());
        assertEquals(HiringStage.OFFERED, updated.getStage());
    }

    @Test
    public void execute_viewsSearchAllCandidatesAndIncludeSummary() {
        List<Person> originals = List.copyOf(model.getFilteredPersonList());
        LocalDate today = LocalDate.now();
        model.setPerson(originals.get(0), originals.get(0).withFollowUp(
                new FollowUp(today.minusDays(1).toString(), "Call", false)));
        model.setPerson(originals.get(1), originals.get(1).withFollowUp(
                new FollowUp(today.toString(), "Email", false)));
        model.setPerson(originals.get(2), originals.get(2).withFollowUp(
                new FollowUp(today.plusDays(1).toString(), "Meet", false)));
        model.setPerson(originals.get(3), originals.get(3).withFollowUp(reminder.complete()));
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        CommandResult result = new FollowUpsCommand(FollowUpFilter.PENDING).execute(model);
        assertEquals(3, model.getFilteredPersonList().size());
        assertTrue(result.getFeedbackToUser().contains("1 overdue, 1 due today"));
        for (FollowUpFilter filter : List.of(FollowUpFilter.OVERDUE, FollowUpFilter.TODAY,
                FollowUpFilter.UPCOMING, FollowUpFilter.COMPLETED)) {
            new FollowUpsCommand(filter).execute(model);
            assertEquals(1, model.getFilteredPersonList().size());
        }
        new FollowUpsCommand(FollowUpFilter.ALL).execute(model);
        assertEquals(4, model.getFilteredPersonList().size());
        new ListCommand().execute(model);
        assertEquals(originals.size(), model.getFilteredPersonList().size());
    }

    @Test
    public void execute_emptyView_returnsZeroCounts() {
        String feedback = new FollowUpsCommand(FollowUpFilter.OVERDUE).execute(model).getFeedbackToUser();
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(feedback.contains("0 candidate(s)"));
        assertTrue(feedback.contains("0 overdue, 0 due today"));
    }
}

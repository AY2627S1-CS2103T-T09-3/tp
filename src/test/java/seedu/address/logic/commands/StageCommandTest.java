package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class StageCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_anyTransition_preservesOtherCandidateDetails() {
        Person original = new PersonBuilder().withRemark("Available in November").withTags("Java", "Backend").build();
        for (HiringStage from : HiringStage.values()) {
            for (HiringStage to : HiringStage.values()) {
                AddressBook book = new AddressBook();
                Person candidate = new PersonBuilder(original).withStage(from).build();
                book.addPerson(candidate);
                model = new ModelManager(book, new UserPrefs());
                Model expected = new ModelManager(book, new UserPrefs());
                expected.setPerson(candidate, new PersonBuilder(candidate).withStage(to).build());

                assertCommandSuccess(new StageCommand(INDEX_FIRST_PERSON, to), model,
                        String.format(StageCommand.MESSAGE_SUCCESS, original.getName(), to), expected);
            }
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedCandidateAndPreservesFilter() {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expected = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person updated = new PersonBuilder(target).withStage(HiringStage.INTERVIEW).build();
        expected.setPerson(target, updated);
        showPersonAtIndex(expected, INDEX_SECOND_PERSON);

        assertCommandSuccess(new StageCommand(INDEX_FIRST_PERSON, HiringStage.INTERVIEW), model,
                String.format(StageCommand.MESSAGE_SUCCESS, target.getName(), HiringStage.INTERVIEW), expected);
    }

    @Test
    public void execute_invalidIndex_doesNotChangeModel() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new StageCommand(invalidIndex, HiringStage.REJECTED), model,
                StageCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new StageCommand(INDEX_SECOND_PERSON, HiringStage.REJECTED), model,
                StageCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
        assertCommandFailure(new StageCommand(INDEX_FIRST_PERSON, HiringStage.REJECTED), new ModelManager(),
                StageCommand.MESSAGE_INVALID_CANDIDATE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_editAndRemarkCommands_preserveStage() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new StageCommand(INDEX_FIRST_PERSON, HiringStage.OFFERED).execute(model);
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setPhone(new Phone("12345678"));
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Accepted the offer")).execute(model);

        assertEquals(new PersonBuilder(original).withPhone("12345678").withRemark("Accepted the offer")
                .withStage(HiringStage.OFFERED).build(), model.getFilteredPersonList().get(0));
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StageCommand(null, HiringStage.APPLIED));
        assertThrows(NullPointerException.class, () -> new StageCommand(INDEX_FIRST_PERSON, null));
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, HiringStage.APPLIED);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void equals_comparesIndexAndStage() {
        StageCommand command = new StageCommand(INDEX_FIRST_PERSON, HiringStage.APPLIED);
        assertEquals(command, command);
        assertEquals(command, new StageCommand(INDEX_FIRST_PERSON, HiringStage.APPLIED));
        assertNotEquals(command, new StageCommand(INDEX_SECOND_PERSON, HiringStage.APPLIED));
        assertNotEquals(command, new StageCommand(INDEX_FIRST_PERSON, HiringStage.REJECTED));
        assertNotEquals(command, null);
        assertNotEquals(command, new ListCommand());
    }
}

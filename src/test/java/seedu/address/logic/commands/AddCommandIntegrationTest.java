package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().withSkills("Java", "SQL").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, validPerson.getName(), validPerson.getPhone(),
                        "Java, SQL"),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_sameNameDifferentPhone_success() {
        Person existing = new PersonBuilder().withName("Jordan Lee").withPhone("1234567")
                .withSkills("Java").build();
        model.addPerson(existing);
        Person newPerson = new PersonBuilder(existing).withPhone("7654321").withSkills("SQL").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(newPerson);

        assertCommandSuccess(new AddCommand(newPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, newPerson.getName(), newPerson.getPhone(), "SQL"),
                expectedModel);
    }

    @Test
    public void execute_differentNameSamePhone_success() {
        Person existing = new PersonBuilder().withName("Jordan Lee").withPhone("1234567")
                .withSkills("Java").build();
        model.addPerson(existing);
        Person newPerson = new PersonBuilder(existing).withName("Morgan Lee").withSkills("SQL").build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(newPerson);

        assertCommandSuccess(new AddCommand(newPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, newPerson.getName(), newPerson.getPhone(), "SQL"),
                expectedModel);
    }

    @Test
    public void execute_sameNameAndPhoneIgnoringCase_throwsCommandException() {
        Person existing = new PersonBuilder().withName("Jordan Lee").withPhone("1234567")
                .withSkills("Java").build();
        model.addPerson(existing);
        Person duplicate = new PersonBuilder(existing).withName("jOrDaN lEe").withSkills("SQL").build();

        assertCommandFailure(new AddCommand(duplicate), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

}

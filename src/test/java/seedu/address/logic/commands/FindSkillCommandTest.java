package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindSkillCommand}.
 */
public class FindSkillCommandTest {

    private static final PersonBuilder PERSON_BUILDER = new PersonBuilder();

    private final Person javaCandidate = PERSON_BUILDER
            .withName("John Tan")
            .withPhone("98765432")
            .withEmail("john@example.com")
            .withAddress("1 Java Street")
            .withRemark("Available for interview")
            .withStage(HiringStage.INTERVIEW)
            .withTags("experienced")
            .withSkills("Java", "SQL")
            .build();
    private final Person anotherJavaCandidate = new PersonBuilder()
            .withName("Mary Lim")
            .withPhone("91234567")
            .withSkills("java")
            .build();
    private final Person noSqlCandidate = new PersonBuilder()
            .withName("Noah Tan")
            .withPhone("81234567")
            .withSkills("NoSQL")
            .build();
    private final Person pythonCandidate = new PersonBuilder()
            .withName("Peter Ong")
            .withPhone("82234567")
            .withSkills("Python")
            .build();

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        Arrays.asList(javaCandidate, anotherJavaCandidate, noSqlCandidate, pythonCandidate)
                .forEach(addressBook::addPerson);
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void equals() {
        FindSkillCommand findJavaPythonCommand = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("Python")));
        FindSkillCommand sameSkillsDifferentOrder = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("python"), new Skill("JAVA")));
        FindSkillCommand differentSkillsSameMode = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("SQL")));
        FindSkillCommand findJavaPythonAllCommand = new FindSkillCommand(FindSkillCommand.SearchMode.ALL,
                List.of(new Skill("Java"), new Skill("Python")));

        assertTrue(findJavaPythonCommand.equals(findJavaPythonCommand));
        assertTrue(findJavaPythonCommand.equals(sameSkillsDifferentOrder));
        assertEquals(findJavaPythonCommand.hashCode(), sameSkillsDifferentOrder.hashCode());
        assertFalse(findJavaPythonCommand.equals(differentSkillsSameMode));
        assertFalse(findJavaPythonCommand.equals(findJavaPythonAllCommand));
        assertFalse(findJavaPythonCommand.equals(null));
        assertFalse(findJavaPythonCommand.equals(1));
    }

    @Test
    public void execute_singleSkillMatch_filtersCandidatesCaseInsensitively() {
        FindSkillCommand command = new FindSkillCommand(new Skill("JAVA"));
        expectedModel.updateFilteredPersonList(person -> hasSkill(person, "JAVA"));

        assertCommandSuccess(command, model, "2 person(s) listed!", expectedModel);
        assertEquals(javaCandidate, model.getFilteredPersonList().get(0));
        assertEquals(anotherJavaCandidate, model.getFilteredPersonList().get(1));
        assertEquals(javaCandidate.getRemark(), model.getFilteredPersonList().get(0).getRemark());
        assertEquals(javaCandidate.getStage(), model.getFilteredPersonList().get(0).getStage());
        assertEquals(javaCandidate.getTags(), model.getFilteredPersonList().get(0).getTags());
    }

    @Test
    public void execute_anyMode_matchesCandidatesWithEitherSkill() {
        FindSkillCommand command = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("Python")));
        expectedModel.updateFilteredPersonList(person -> hasSkill(person, "Java") || hasSkill(person, "Python"));

        assertCommandSuccess(command, model, "3 person(s) listed!", expectedModel);
        assertEquals(List.of(javaCandidate, anotherJavaCandidate, pythonCandidate), model.getFilteredPersonList());
    }

    @Test
    public void execute_allMode_matchesCandidatesWithEverySkill() {
        FindSkillCommand command = new FindSkillCommand(FindSkillCommand.SearchMode.ALL,
                List.of(new Skill("java"), new Skill("sql")));
        expectedModel.updateFilteredPersonList(person -> hasSkill(person, "java") && hasSkill(person, "sql"));

        assertCommandSuccess(command, model, "1 person(s) listed!", expectedModel);
        assertEquals(javaCandidate, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_partialSkillMatch_doesNotIncludeCandidate() {
        FindSkillCommand command = new FindSkillCommand(new Skill("SQL"));
        expectedModel.updateFilteredPersonList(person -> hasSkill(person, "SQL"));

        assertCommandSuccess(command, model, "1 person(s) listed!", expectedModel);
        assertEquals(javaCandidate, model.getFilteredPersonList().get(0));
        assertFalse(model.getFilteredPersonList().contains(noSqlCandidate));
    }

    @Test
    public void execute_noMatchingCandidate_showsFeedback() {
        FindSkillCommand command = new FindSkillCommand(new Skill("Go"));
        expectedModel.updateFilteredPersonList(person -> hasSkill(person, "Go"));

        assertCommandSuccess(command, model, "No candidates found matching skill search: Go.", expectedModel);
        assertEquals(0, model.getFilteredPersonList().size());
    }

    @Test
    public void constructor_duplicateSkills_deduplicatesCaseInsensitively() {
        FindSkillCommand command = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("java"), new Skill("Python")));

        assertEquals("seedu.address.logic.commands.FindSkillCommand{searchMode=ANY, skills=[Java, Python]}",
                command.toString());
    }

    @Test
    public void execute_filteredResultsKeepDisplayedIndexesInOrder() {
        FindSkillCommand command = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("Python")));

        command.execute(model);

        assertEquals(javaCandidate, model.getFilteredPersonList().get(0));
        assertEquals(anotherJavaCandidate, model.getFilteredPersonList().get(1));
        assertEquals(pythonCandidate, model.getFilteredPersonList().get(2));
    }

    @Test
    public void toStringMethod() {
        FindSkillCommand command = new FindSkillCommand(FindSkillCommand.SearchMode.ANY,
                List.of(new Skill("Java"), new Skill("Python")));
        String expected = FindSkillCommand.class.getCanonicalName()
                + "{searchMode=ANY, skills=[Java, Python]}";
        assertEquals(expected, command.toString());
    }

    private static boolean hasSkill(Person person, String targetSkill) {
        return person.getSkills().stream().anyMatch(skill -> skill.value.equalsIgnoreCase(targetSkill));
    }
}

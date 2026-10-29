package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CandidateWorkspaceToolbarTest {
    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void dueFollowUpFilter_showsTodayAndOverduePendingCandidates() throws Exception {
        FxTestUtil.run(() -> {
            Person dueToday = new PersonBuilder().withName("Due Today").build()
                    .withFollowUp(new FollowUp(LocalDate.now().toString(), "Call", false));
            Person overdue = new PersonBuilder().withName("Overdue").build()
                    .withFollowUp(new FollowUp(LocalDate.now().minusDays(1).toString(), "Call", false));
            Person upcoming = new PersonBuilder().withName("Upcoming").build()
                    .withFollowUp(new FollowUp(LocalDate.now().plusDays(1).toString(), "Call", false));
            Person completedToday = new PersonBuilder().withName("Completed Today").build()
                    .withFollowUp(new FollowUp(LocalDate.now().toString(), "Call", true));
            StubLogic logic = new StubLogic(dueToday, overdue, upcoming, completedToday);
            CandidateWorkspaceToolbar toolbar = new CandidateWorkspaceToolbar(logic, message -> { });
            new Scene(toolbar.getRoot());
            ToggleButton dueFilter = (ToggleButton) toolbar.getRoot().lookup("#dueFollowUps");

            dueFilter.fire();

            assertEquals(List.of(dueToday, overdue), logic.getFilteredPersonList());
        });
    }

    @Test
    public void missingContactFilter_showsCandidatesWithoutEmailOrPhone() throws Exception {
        FxTestUtil.run(() -> {
            Person missingEmail = new PersonBuilder().withName("Missing Email").withEmail("").build();
            Person complete = new PersonBuilder().withName("Complete").withEmail("complete@example.com").build();
            StubLogic logic = new StubLogic(missingEmail, complete);
            CandidateWorkspaceToolbar toolbar = new CandidateWorkspaceToolbar(logic, message -> { });
            new Scene(toolbar.getRoot());
            ToggleButton contactFilter = (ToggleButton) toolbar.getRoot().lookup("#missingContact");

            contactFilter.fire();

            assertEquals(List.of(missingEmail), logic.getFilteredPersonList());
        });
    }

    private static final class StubLogic implements Logic {
        private final ObservableList<Person> allPersons;
        private final FilteredList<Person> filteredPersons;

        private StubLogic(Person... persons) {
            allPersons = FXCollections.observableArrayList(persons);
            filteredPersons = new FilteredList<>(allPersons);
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            return null;
        }

        @Override
        public ObservableList<Person> getAllPersons() {
            return allPersons;
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return filteredPersons;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            filteredPersons.setPredicate(predicate);
        }

        @Override
        public void sortFilteredPersonList(Comparator<? super Person> comparator) {
            // Sorting is not part of this toolbar filter test double.
        }

        @Override
        public GuiSettings getGuiSettings() {
            return new GuiSettings();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            // Preferences are not part of this toolbar filter test double.
        }
    }
}

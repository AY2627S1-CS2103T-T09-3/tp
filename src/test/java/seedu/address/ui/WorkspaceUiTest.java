package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class WorkspaceUiTest {
    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void profile_reflectsSelectionEditsAndDeletion() throws Exception {
        FxTestUtil.run(() -> {
            Person candidate = new PersonBuilder().withRemark("Long remark ".repeat(50)).build();
            ObservableList<Person> all = FXCollections.observableArrayList(candidate);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            CandidateProfilePanel profile = new CandidateProfilePanel(selection);
            new Scene(profile.getRoot());
            profile.getRoot().applyCss();
            assertTrue(profile.getRoot().lookup("#emptyProfile").isVisible());
            selection.select(candidate);
            assertEquals(candidate.getRemark().value, label(profile, "profileRemark"));
            assertFalse(profile.getRoot().lookup("#emptyProfile").isVisible());
            Person edited = new PersonBuilder(candidate).withName("Updated Name").withRemark("Updated").build();
            all.set(0, edited);
            assertEquals("Updated Name", label(profile, "profileName"));
            assertEquals("Updated", label(profile, "profileRemark"));
            all.clear();
            assertTrue(profile.getRoot().lookup("#emptyProfile").isVisible());
            assertFalse(profile.getRoot().lookup("#profileContent").isVisible());
        });
    }

    @Test
    public void profile_quickActionPassesSelectedCandidateToHandler() throws Exception {
        FxTestUtil.run(() -> {
            Person candidate = new PersonBuilder().build();
            ObservableList<Person> all = FXCollections.observableArrayList(candidate);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            AtomicReference<CandidateProfilePanel.QuickAction> action = new AtomicReference<>();
            AtomicReference<Person> selected = new AtomicReference<>();
            CandidateProfilePanel profile = new CandidateProfilePanel(selection, (chosenAction, chosenPerson) -> {
                action.set(chosenAction);
                selected.set(chosenPerson);
            });
            new Scene(profile.getRoot());
            selection.select(candidate);
            profile.getRoot().applyCss();
            Button editButton = (Button) profile.getRoot().lookup("#editCandidate");

            assertNotNull(editButton);
            editButton.fire();

            assertEquals(CandidateProfilePanel.QuickAction.EDIT, action.get());
            assertEquals(candidate, selected.get());
        });
    }

    @Test
    public void dashboard_countsAllCandidatesWhileSearchChanges() throws Exception {
        FxTestUtil.run(() -> {
            Person first = new PersonBuilder().build();
            Person second = new PersonBuilder().withName("Other").withStage(HiringStage.INTERVIEW).build();
            ObservableList<Person> all = FXCollections.observableArrayList(first, second);
            FilteredList<Person> visible = new FilteredList<>(all);
            PipelineDashboard dashboard = new PipelineDashboard(all, visible);
            new Scene(dashboard.getRoot(), 760, 160);
            dashboard.getRoot().applyCss();
            dashboard.getRoot().layout();
            HBox tiles = (HBox) dashboard.getRoot().lookup("#stageTiles");
            assertEquals(5, tiles.getChildren().size());
            assertTrue(tiles.getChildren().stream().allMatch(tile -> tile.getBoundsInParent().getMaxX()
                    <= tiles.getWidth()));
            visible.setPredicate(first::equals);
            assertEquals("1", label(dashboard, "countINTERVIEW"));
            assertEquals("All candidates: 2  •  Displayed: 1", label(dashboard, "pipelineCount"));
            all.set(1, new PersonBuilder(second).withStage(HiringStage.OFFERED).build());
            assertEquals("0", label(dashboard, "countINTERVIEW"));
            assertEquals("1", label(dashboard, "countOFFERED"));
        });
    }

    @Test
    public void dashboard_stageTileActivatesSelectionHandler() throws Exception {
        FxTestUtil.run(() -> {
            Person candidate = new PersonBuilder().build();
            ObservableList<Person> all = FXCollections.observableArrayList(candidate);
            AtomicReference<HiringStage> selectedStage = new AtomicReference<>();
            PipelineDashboard dashboard = new PipelineDashboard(all, all, selectedStage::set);
            new Scene(dashboard.getRoot());
            HBox tiles = (HBox) dashboard.getRoot().lookup("#stageTiles");
            VBox interviewTile = (VBox) tiles.getChildren().get(2);

            interviewTile.getOnMouseClicked().handle(null);

            assertEquals(HiringStage.INTERVIEW, selectedStage.get());
        });
    }

    @Test
    public void resultList_reindexesAfterDeletionWithoutOpeningAnotherProfile() throws Exception {
        FxTestUtil.run(() -> {
            Person first = new PersonBuilder().build();
            Person second = new PersonBuilder().withName("Other").build();
            ObservableList<Person> all = FXCollections.observableArrayList(first, second);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            PersonListPanel panel = new PersonListPanel(all, all, selection);
            StackPane root = new StackPane(panel.getRoot());
            new Scene(root, 400, 500);
            root.applyCss();
            root.layout();
            selection.select(first);
            all.remove(first);
            panel.refreshSelection();
            root.layout();
            assertNull(selection.selectedPersonProperty().get());
            assertEquals("1. ", ((Label) panel.getRoot().lookup("#id")).getText());
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            assertNull(list.getSelectionModel().getSelectedItem());
            all.clear();
            assertTrue(((Label) list.getPlaceholder()).getText().contains("pipeline is empty"));
        });
    }

    @Test
    public void resultList_supportsMultiSelectionForBulkActions() throws Exception {
        FxTestUtil.run(() -> {
            Person first = new PersonBuilder().build();
            Person second = new PersonBuilder().withName("Other").build();
            ObservableList<Person> all = FXCollections.observableArrayList(first, second);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            PersonListPanel panel = new PersonListPanel(all, all, selection);
            new Scene(panel.getRoot());
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            list.getSelectionModel().selectAll();

            assertEquals(SelectionMode.MULTIPLE, list.getSelectionModel().getSelectionMode());
            assertEquals(2, list.getSelectionModel().getSelectedItems().size());
            assertNotNull(panel.getRoot().lookup("#bulkStage"));
        });
    }

    @Test
    public void bulkDelete_usesOneBatchCommandForSelectedIndexes() throws Exception {
        FxTestUtil.run(() -> {
            Person first = new PersonBuilder().build();
            Person second = new PersonBuilder().withName("Other").build();
            ObservableList<Person> all = FXCollections.observableArrayList(first, second);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            List<String> commands = new ArrayList<>();
            PersonListPanel panel = new PersonListPanel(all, all, selection, command -> {
                commands.add(command);
                return new CommandResult("Deleted");
            });
            new Scene(panel.getRoot());
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            list.getSelectionModel().selectAll();

            panel.executeBulkDelete();

            assertEquals(List.of("delete 1,2"), commands);
            assertEquals(0, list.getSelectionModel().getSelectedItems().size());
        });
    }

    @Test
    public void bulkStage_stopsAfterFirstFailureAndKeepsSelectionForRetry() throws Exception {
        FxTestUtil.run(() -> {
            Person first = new PersonBuilder().build();
            Person second = new PersonBuilder().withName("Other").build();
            ObservableList<Person> all = FXCollections.observableArrayList(first, second);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            List<String> commands = new ArrayList<>();
            PersonListPanel panel = new PersonListPanel(all, all, selection, command -> {
                commands.add(command);
                throw new CommandException("save failed");
            });
            new Scene(panel.getRoot());
            ListView<?> list = (ListView<?>) panel.getRoot().lookup("#personListView");
            list.getSelectionModel().selectAll();

            panel.executeBulkStage("Interview");

            assertEquals(List.of("stage 1 s/Interview"), commands);
            assertEquals(2, list.getSelectionModel().getSelectedItems().size());
        });
    }

    @Test
    public void optionalFieldsAndLongContent_areReadable() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().withEmail("").withAddress("").withRemark("").build();
            ObservableList<Person> all = FXCollections.observableArrayList(person);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            CandidateProfilePanel profile = new CandidateProfilePanel(selection);
            new Scene(profile.getRoot());
            profile.getRoot().applyCss();
            selection.select(person);
            assertEquals("Not provided", label(profile, "profileEmail"));
            assertEquals("Not provided", label(profile, "profileAddress"));
            assertEquals("Not provided", label(profile, "profileRemark"));
            assertTrue(profile.getRoot().lookup("#copyEmail").isDisabled());
            assertTrue(((ScrollPane) profile.getRoot().lookup("#profileContent")).isFitToWidth());
        });
    }

    @Test
    public void profile_followUpUpdatesAcrossDueStatesCompletionAndClearing() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().build();
            ObservableList<Person> all = FXCollections.observableArrayList(person);
            CandidateSelectionModel selection = new CandidateSelectionModel(all, all);
            CandidateProfilePanel profile = new CandidateProfilePanel(selection);
            new Scene(profile.getRoot());
            profile.getRoot().applyCss();
            selection.select(person);
            assertEquals("No follow-up scheduled", label(profile, "profileFollowUp"));
            LocalDate today = LocalDate.now();
            for (LocalDate date : java.util.List.of(today.minusDays(1), today, today.plusDays(1))) {
                FollowUp reminder = new FollowUp(date.toString(), "Call candidate", false);
                all.set(0, person.withFollowUp(reminder));
                assertEquals(reminder.format(today), label(profile, "profileFollowUp"));
                all.set(0, person.withFollowUp(reminder.complete()));
                assertEquals(reminder.complete().format(today), label(profile, "profileFollowUp"));
            }
            all.set(0, person);
            assertEquals("No follow-up scheduled", label(profile, "profileFollowUp"));
        });
    }

    private String label(UiPart<?> part, String id) {
        return ((Label) ((javafx.scene.Parent) part.getRoot()).lookup("#" + id)).getText();
    }
}

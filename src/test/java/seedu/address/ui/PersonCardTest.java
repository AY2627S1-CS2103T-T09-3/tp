package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void constructor_personWithRemark_displaysRemark() throws Exception {
        assertDisplayedRemark("Likes swimming");
    }

    @Test
    public void constructor_personWithEmptyRemark_displaysEmptyRemark() throws Exception {
        assertDisplayedRemark("");
    }

    @Test
    public void constructor_followUp_displaysDateStatusAndNoteOrHidesEmptyLabel() throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            FollowUp reminder = new FollowUp("2026-11-05", "Call candidate", false);
            Person person = new PersonBuilder().build().withFollowUp(reminder);
            PersonCard card = new PersonCard(person, 1);
            Label label = (Label) card.getRoot().lookup("#followUp");
            assertEquals(reminder.format(LocalDate.now()), label.getText());
            assertTrue(label.isVisible());
            assertTrue(label.isManaged());
            PersonCard empty = new PersonCard(person.withFollowUp(FollowUp.empty()), 1);
            Label emptyLabel = (Label) empty.getRoot().lookup("#followUp");
            assertEquals("", emptyLabel.getText());
            assertFalse(emptyLabel.isVisible());
            assertFalse(emptyLabel.isManaged());
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }

    private void assertDisplayedRemark(String remark) throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            Person person = new PersonBuilder().withRemark(remark).build();
            PersonCard card = new PersonCard(person, 1);
            Label remarkLabel = (Label) card.getRoot().lookup("#remark");
            assertEquals(remark, remarkLabel.getText());
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_eachHiringStage_displaysStage() throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            for (HiringStage stage : HiringStage.values()) {
                Person person = new PersonBuilder().withStage(stage).build();
                PersonCard card = new PersonCard(person, 1);
                Label stageLabel = (Label) card.getRoot().lookup("#hiringStage");
                assertEquals("Hiring stage: " + stage, stageLabel.getText());
            }
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_stageBadges_distinctAndReadableWhenSelected() throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            Set<Paint> backgrounds = new HashSet<>();
            for (HiringStage stage : HiringStage.values()) {
                PersonCard card = new PersonCard(new PersonBuilder().withStage(stage).build(), 1);
                ListCell<Person> cell = new ListCell<>();
                cell.setGraphic(card.getRoot());
                StackPane root = new StackPane(cell);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
                root.applyCss();
                root.layout();

                Label badge = (Label) card.getRoot().lookup("#hiringStage");
                assertEquals("Hiring stage: " + stage, badge.getText());
                assertTrue(badge.getStyleClass().contains("hiring-stage-badge"));
                assertReadableBadge(badge);
                backgrounds.add(badge.getBackground().getFills().get(0).getFill());

                cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
                root.applyCss();
                assertReadableBadge(badge);
            }
            assertEquals(HiringStage.values().length, backgrounds.size());
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }

    private void assertReadableBadge(Label badge) {
        Color foreground = (Color) badge.getTextFill();
        Color background = (Color) badge.getBackground().getFills().get(0).getFill();
        double foregroundLuminance = relativeLuminance(foreground);
        double backgroundLuminance = relativeLuminance(background);
        double contrast = (Math.max(foregroundLuminance, backgroundLuminance) + 0.05)
                / (Math.min(foregroundLuminance, backgroundLuminance) + 0.05);
        assertTrue(contrast >= 4.5, "Badge text/background contrast must be at least 4.5:1");
        assertEquals(1.0, badge.getOpacity());
    }

    private double relativeLuminance(Color color) {
        return 0.2126 * linearize(color.getRed())
                + 0.7152 * linearize(color.getGreen())
                + 0.0722 * linearize(color.getBlue());
    }

    private double linearize(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    @Test
    public void constructor_personWithSkills_displaysSkillsInOrder() throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            Person person = new PersonBuilder().withSkills("SQL", "Java").build();
            PersonCard card = new PersonCard(person, 1);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            List<String> displayedSkills = skills.getChildren().stream()
                    .map(node -> ((Label) node).getText())
                    .toList();
            assertEquals(List.of("Java", "SQL"), displayedSkills);
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void constructor_personWithEmptySkills_displaysNone() throws Exception {
        FutureTask<Void> assertion = new FutureTask<>(() -> {
            Person person = new PersonBuilder().withSkills().build();
            PersonCard card = new PersonCard(person, 1);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            assertEquals(List.of("None"), skills.getChildren().stream()
                    .map(node -> ((Label) node).getText())
                    .toList());
            return null;
        });
        Platform.runLater(assertion);
        assertion.get(10, TimeUnit.SECONDS);
    }
}

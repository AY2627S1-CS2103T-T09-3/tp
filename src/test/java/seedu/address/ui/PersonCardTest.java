package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        FxTestUtil.initialize();
    }

    @Test
    public void constructor_eachStage_displaysReadableBadge() throws Exception {
        FxTestUtil.run(() -> {
            for (HiringStage stage : HiringStage.values()) {
                PersonCard card = new PersonCard(new PersonBuilder().withStage(stage).build(), 1);
                Label label = (Label) card.getRoot().lookup("#hiringStage");
                assertEquals(stage.toString(), label.getText());
                assertTrue(label.getStyleClass().contains("stage-badge"));
            }
        });
    }

    @Test
    public void skills_areSortedAndLimitedToThreeWithOverflow() throws Exception {
        FxTestUtil.run(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().withSkills("SQL", "Java", "Python", "C").build(), 2);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            List<String> values = skills.getChildren().stream().map(node -> ((Label) node).getText()).toList();
            assertEquals(List.of("C", "Java", "Python", "+1 more"), values);
            assertEquals("2. ", ((Label) card.getRoot().lookup("#id")).getText());
        });
    }

    @Test
    public void emptySkillsAndInvalidInputs_areHandled() throws Exception {
        FxTestUtil.run(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            FlowPane skills = (FlowPane) card.getRoot().lookup("#skills");
            assertEquals("No skills", ((Label) skills.getChildren().get(0)).getText());
            assertThrows(NullPointerException.class, () -> new PersonCard(null, 1));
            assertThrows(IllegalArgumentException.class, () -> new PersonCard(new PersonBuilder().build(), 0));
        });
    }

    @Test
    public void constructor_stageBadges_distinctAndReadableWhenSelected() throws Exception {
        FxTestUtil.run(() -> {
            Set<Paint> backgrounds = new HashSet<>();
            for (HiringStage stage : HiringStage.values()) {
                PersonCard card = new PersonCard(new PersonBuilder().withStage(stage).build(), 1);
                ListCell<Person> cell = new ListCell<>();
                cell.setGraphic(card.getRoot());
                StackPane root = new StackPane(cell);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/view/Workspace.css").toExternalForm());
                root.applyCss();
                root.layout();

                Label badge = (Label) card.getRoot().lookup("#hiringStage");
                assertEquals(stage.toString(), badge.getText());
                assertTrue(badge.getStyleClass().contains("stage-badge"));
                assertReadableBadge(badge);
                backgrounds.add(badge.getBackground().getFills().get(0).getFill());

                cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), true);
                root.applyCss();
                assertReadableBadge(badge);
            }
            assertEquals(HiringStage.values().length, backgrounds.size());
        });
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
    public void constructor_followUp_displaysDateStatusAndNoteOrHidesEmptyLabel() throws Exception {
        FxTestUtil.run(() -> {
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
        });
    }

}

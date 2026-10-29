package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.HiringStage;
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
}

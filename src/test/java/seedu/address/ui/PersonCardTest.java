package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
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
}

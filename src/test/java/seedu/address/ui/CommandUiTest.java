package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;

public class CommandUiTest {
    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void history_recallsSuccessFailureAndRestoresUnsubmittedDraft() throws Exception {
        FxTestUtil.run(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(input -> {
                executed.add(input);
                if (input.equals("invalid")) {
                    throw new CommandException("Invalid command");
                }
                return new CommandResult("OK");
            });
            TextField field = field(box);
            enter(field, "list");
            assertEquals("", field.getText());
            enter(field, "invalid");
            assertEquals("invalid", field.getText());
            assertTrue(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            field.setText("my draft");
            press(field, KeyCode.UP);
            assertEquals("invalid", field.getText());
            press(field, KeyCode.UP);
            assertEquals("list", field.getText());
            press(field, KeyCode.DOWN);
            press(field, KeyCode.DOWN);
            assertEquals("my draft", field.getText());
            assertFalse(field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            enter(field, "   ");
            assertEquals(List.of("list", "invalid"), executed);
        });
    }

    @Test
    public void completion_tabPreservesArgumentsAndEscapeDismisses() throws Exception {
        FxTestUtil.run(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(input -> {
                executed.add(input);
                return new CommandResult("OK");
            });
            TextField field = field(box);
            field.setText("vi Amy Bee");
            field.positionCaret(2);
            press(field, KeyCode.TAB);
            assertEquals("view Amy Bee", field.getText());
            assertEquals(4, field.getCaretPosition());
            field.setText("stage 1 s/in");
            field.positionCaret(field.getLength());
            press(field, KeyCode.TAB);
            assertEquals("stage 1 s/Interview", field.getText());
            field.setText("fi");
            field.positionCaret(2);
            ListView<?> suggestions = (ListView<?>) box.getRoot().lookup("#suggestions");
            assertTrue(suggestions.isVisible());
            press(field, KeyCode.ESCAPE);
            field.positionCaret(1);
            assertFalse(suggestions.isVisible());
            assertEquals(List.of(), executed);
        });
    }

    @Test
    public void help_searchesOfflineAndInsertsWithoutExecuting() throws Exception {
        FxTestUtil.run(() -> {
            List<String> templates = new ArrayList<>();
            HelpWindow help = new HelpWindow(templates::add);
            try {
                help.getRoot().getScene().getRoot().applyCss();
                TextField search = (TextField) help.getRoot().getScene().lookup("#helpSearch");
                ListView<?> entries = (ListView<?>) help.getRoot().getScene().lookup("#commandEntries");
                Button insert = (Button) help.getRoot().getScene().lookup("#insertTemplate");
                search.setText("zzznomatch");
                assertTrue(entries.getItems().isEmpty());
                assertTrue(insert.isDisabled());
                search.setText("stage");
                assertFalse(entries.getItems().isEmpty());
                // Select by word because other commands may also mention stages in their usage.
                for (int index = 0; index < entries.getItems().size(); index++) {
                    if (((CommandReference) entries.getItems().get(index)).getWord().equals("stage")) {
                        entries.getSelectionModel().select(index);
                    }
                }
                insert.fire();
                assertEquals(List.of("stage 1 s/"), templates);
                assertFalse(help.isShowing());
            } finally {
                help.hide();
            }
        });
    }

    @Test
    public void feedback_usesTextAndStyleForErrorsThenClearsFailureState() throws Exception {
        FxTestUtil.run(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea area = (TextArea) display.getRoot().lookup("#resultDisplay");
            display.showFailure("Invalid index");
            assertEquals("Error: Invalid index", area.getText());
            assertTrue(area.getPseudoClassStates().stream().anyMatch(state -> state.getPseudoClassName()
                    .equals("failure")));
            display.setFeedbackToUser("Saved");
            assertEquals("Success: Saved", area.getText());
            assertFalse(area.getPseudoClassStates().stream().anyMatch(state -> state.getPseudoClassName()
                    .equals("failure")));
        });
    }

    @Test
    public void templateInsertion_positionsCaretAtFirstEditableName() throws Exception {
        FxTestUtil.run(() -> {
            CommandBox box = new CommandBox(input -> new CommandResult("OK"));
            TextField field = field(box);
            box.insertTemplate("add n/ p/ s/");
            assertEquals(6, field.getCaretPosition());
            assertEquals("add n/ p/ s/", field.getText());
            assertTrue(((Label) box.getRoot().lookup("#commandHint")).getText().startsWith("add"));
        });
    }

    private TextField field(CommandBox box) {
        new Scene(box.getRoot());
        box.getRoot().applyCss();
        return (TextField) box.getRoot().lookup("#commandTextField");
    }

    private void enter(TextField field, String input) {
        field.setText(input);
        field.fireEvent(new ActionEvent());
    }

    private void press(TextField field, KeyCode code) {
        field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }
}

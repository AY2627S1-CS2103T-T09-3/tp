package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.HiringStage;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void workflow_keepsProfileDashboardAndIndexesInSync() throws Exception {
        FxTestUtil.run(() -> {
            AtomicBoolean failSave = new AtomicBoolean();
            MainWindow window = createWindow(failSave);
            try {
                window.show();
                execute(window, "view 1");
                assertEquals("Alex Morgan", label(window, "profileName"));
                execute(window, "edit 1 n/Alex Tan p/91234568");
                assertEquals("Alex Tan", label(window, "profileName"));
                execute(window, "stage 1 s/Interview");
                assertEquals("Interview", label(window, "profileStage"));
                assertEquals("2", label(window, "countINTERVIEW"));
                failSave.set(true);
                execute(window, "delete 1");
                assertEquals("Alex Tan", label(window, "profileName"));
                assertEquals("All candidates: 4  •  Displayed: 4", label(window, "pipelineCount"));
                assertTrue(result(window).startsWith("Error:"));
                execute(window, "clear");
                assertEquals("Alex Tan", label(window, "profileName"));
                failSave.set(false);
                execute(window, "find Jamie");
                assertTrue(window.getPrimaryStage().getScene().lookup("#emptyProfile").isVisible());
                execute(window, "view 1");
                assertEquals("Jamie Tan", label(window, "profileName"));
                execute(window, "list");
                assertEquals("Jamie Tan", label(window, "profileName"));
                execute(window, "delete 2");
                assertTrue(window.getPrimaryStage().getScene().lookup("#emptyProfile").isVisible());
                assertEquals("All candidates: 3  •  Displayed: 3", label(window, "pipelineCount"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void workspace_rendersDashboardAssistanceAndHelp() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow window = createWindow(new AtomicBoolean());
            try {
                window.show();
                execute(window, "view 1");
                window.getPrimaryStage().getScene().getRoot().applyCss();
                window.getPrimaryStage().getScene().getRoot().layout();
                savePreview(window.getPrimaryStage(), "workspace");
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                input.setText("stage 1 s/");
                input.positionCaret(input.getLength());
                savePreview(window.getPrimaryStage(), "command-assistance");
                ListView<?> suggestions = (ListView<?>) window.getPrimaryStage().getScene().lookup("#suggestions");
                assertTrue(suggestions.getHeight() >= 84);
                window.handleHelp();
                Stage help = (Stage) Window.getWindows().stream().filter(candidate -> candidate instanceof Stage
                        && ((Stage) candidate).getTitle().equals("Scoutly command reference"))
                        .findFirst().orElseThrow();
                try {
                    savePreview(help, "command-reference");
                } finally {
                    help.hide();
                }
                input.clear();
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void minimumWorkspace_keepsProfilesAndInputUsable() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow window = createWindow(new AtomicBoolean());
            window.getPrimaryStage().setWidth(760);
            window.getPrimaryStage().setHeight(700);
            try {
                window.show();
                execute(window, "view 1");
                savePreview(window.getPrimaryStage(), "workspace-minimum");
                assertEquals(760, window.getPrimaryStage().getScene().getWidth(), 2);
                ListView<?> list = (ListView<?>) window.getPrimaryStage().getScene().lookup("#personListView");
                assertTrue(list.lookupAll(".scroll-bar").stream().noneMatch(node -> node instanceof ScrollBar bar
                        && bar.getOrientation() == Orientation.HORIZONTAL && bar.isVisible()));
                assertTrue(window.getPrimaryStage().getScene().lookup("#commandTextField").getBoundsInParent()
                        .getWidth() > 500);
                assertTrue(window.getPrimaryStage().getScene().lookup("#profileContent").getBoundsInParent()
                        .getHeight() > 100);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow(AtomicBoolean failSave) {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alex Morgan").withSkills("Java", "SQL", "Python")
                .withRemark("Available in November. Interested in backend engineering.").build());
        model.addPerson(new PersonBuilder().withName("Jamie Tan").withPhone("92222222")
                .withStage(HiringStage.SCREENED).withSkills("Design", "Figma").build());
        model.addPerson(new PersonBuilder().withName("Priya Shah").withPhone("93333333")
                .withStage(HiringStage.INTERVIEW).withSkills("Python", "SQL").build());
        model.addPerson(new PersonBuilder().withName("Sam Lee").withPhone("94444444")
                .withStage(HiringStage.OFFERED).withSkills("Java", "Testing").build());
        Path data = temporaryFolder.resolve("candidates.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(data) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (failSave.get()) {
                    throw new IOException("Simulated save failure");
                }
                super.saveAddressBook(addressBook);
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, data);
        window.fillInnerParts();
        return window;
    }

    private void execute(MainWindow window, String command) {
        TextField field = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        field.setText(command);
        field.fireEvent(new ActionEvent());
    }

    private String label(MainWindow window, String id) {
        return ((Label) window.getPrimaryStage().getScene().lookup("#" + id)).getText();
    }

    private String result(MainWindow window) {
        return ((TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay")).getText();
    }

    private void savePreview(Stage stage, String name) {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        WritableImage snapshot = stage.getScene().snapshot(null);
        BufferedImage output = new BufferedImage((int) snapshot.getWidth(), (int) snapshot.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
            }
        }
        try {
            Path target = Path.of("build", "reports", "ui", name + ".png");
            Files.createDirectories(target.getParent());
            ImageIO.write(output, "png", target.toFile());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}

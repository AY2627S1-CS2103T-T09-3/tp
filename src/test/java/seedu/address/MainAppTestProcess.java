package seedu.address;

import java.nio.file.Path;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Exercises the duplicate-instance warning in a separate JVM because it shuts down the JavaFX toolkit.
 */
public class MainAppTestProcess {

    /**
     * Starts an application for a locked folder, reports and dismisses its warning, and shuts down normally.
     */
    public static void main(String[] args) throws Exception {
        Platform.startup(() -> Platform.setImplicitExit(false));
        MainApp app = new MainApp(Path.of(args[0]), Path.of(args[1]));
        app.init();
        Platform.runLater(() -> {
            Platform.runLater(MainAppTestProcess::dismissWarning);
            app.start(new Stage());
            app.stop();
        });
    }

    private static void dismissWarning() {
        for (Window window : List.copyOf(Window.getWindows())) {
            if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane pane) {
                System.out.println(pane.getContentText());
                System.out.flush();
                Button button = (Button) pane.lookupButton(ButtonType.OK);
                button.fire();
                return;
            }
        }
        System.err.println("Expected the data-folder warning, but no warning was displayed.");
        System.exit(1);
    }
}

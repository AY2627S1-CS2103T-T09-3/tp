package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

/** Searchable offline command reference with safe template insertion into the command box. */
public class HelpWindow extends UiPart<Stage> {
    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t09-3.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Refer to the user guide: " + USERGUIDE_URL;

    private final Consumer<String> templateConsumer;

    @FXML
    private TextField helpSearch;
    @FXML
    private ListView<CommandReference> commandEntries;
    @FXML
    private Label commandTitle;
    @FXML
    private Label helpStatus;
    @FXML
    private TextArea commandUsage;
    @FXML
    private Button insertTemplate;

    public HelpWindow() {
        this(new Stage(), null);
    }

    public HelpWindow(Stage root) {
        this(root, null);
    }

    /** Creates help connected to the application's command entry. */
    public HelpWindow(Consumer<String> templateConsumer) {
        this(new Stage(), requireNonNull(templateConsumer));
    }

    private HelpWindow(Stage root, Consumer<String> templateConsumer) {
        super("HelpWindow.fxml", requireNonNull(root));
        this.templateConsumer = templateConsumer;
        commandEntries.setPlaceholder(new Label("No matching commands"));
        helpSearch.textProperty().addListener((observable, previous, current) -> filter(current));
        commandEntries.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, current) -> showCommand(current));
        filter("");
    }

    private void filter(String query) {
        CommandReference previous = commandEntries.getSelectionModel().getSelectedItem();
        List<CommandReference> matches = CommandReference.search(query);
        commandEntries.setItems(FXCollections.observableArrayList(matches));
        if (previous != null && matches.contains(previous)) {
            commandEntries.getSelectionModel().select(previous);
        } else if (!matches.isEmpty()) {
            commandEntries.getSelectionModel().selectFirst();
        } else {
            showCommand(null);
        }
        helpStatus.setText(matches.size() + " commands • Templates are inserted for review and never executed.");
    }

    private void showCommand(CommandReference entry) {
        commandTitle.setText(entry == null ? "No matching commands" : entry.toString());
        commandUsage.setText(entry == null ? "Try a command name, such as view, or a keyword, such as skill."
                : entry.getUsage());
        insertTemplate.setDisable(entry == null || templateConsumer == null);
    }

    @FXML
    private void handleInsertTemplate() {
        CommandReference entry = commandEntries.getSelectionModel().getSelectedItem();
        if (entry != null && templateConsumer != null) {
            templateConsumer.accept(entry.getTemplate());
            hide();
        }
    }

    @FXML
    private void copyUrl() {
        ClipboardContent content = new ClipboardContent();
        content.putString(USERGUIDE_URL);
        helpStatus.setText(Clipboard.getSystemClipboard().setContent(content)
                ? "User guide link copied" : "Could not copy user guide link");
    }

    /** Shows or restores the same help window, including when it was minimized. */
    public void show() {
        getRoot().setIconified(false);
        getRoot().show();
        getRoot().toFront();
        helpSearch.requestFocus();
    }

    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /** Hides help without discarding its search or selected command. */
    public void hide() {
        getRoot().hide();
    }

    /** Restores minimized help before bringing it to the front. */
    public void focus() {
        show();
    }
}

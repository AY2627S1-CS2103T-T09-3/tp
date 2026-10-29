package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

/** Contextual syntax and keyboard-selectable suggestions beneath the command input. */
public class CommandAssistPanel extends UiPart<Region> {
    @FXML
    private Label commandHint;
    @FXML
    private ListView<CommandSuggestion> suggestions;

    /** Creates assistance whose accepted suggestions are handled by the command box. */
    public CommandAssistPanel(Consumer<CommandSuggestion> onAccept) {
        super("CommandAssistPanel.fxml");
        requireNonNull(onAccept);
        suggestions.setOnMouseClicked(event -> {
            CommandSuggestion selected = suggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onAccept.accept(selected);
            }
        });
        dismiss();
    }

    /** Updates help and suggestions without taking keyboard focus away from command input. */
    public void update(String input, List<CommandSuggestion> completions) {
        requireNonNull(completions);
        String hint = CommandReference.forInput(input).map(CommandReference::getUsage)
                .orElse("Type a command • Tab accepts suggestions • ↑/↓ recalls history • F1 opens help");
        commandHint.setText(hint);
        commandHint.setTooltip(new Tooltip(hint));
        suggestions.setItems(FXCollections.observableArrayList(completions));
        boolean hasSuggestions = !completions.isEmpty();
        suggestions.setVisible(hasSuggestions);
        suggestions.setManaged(hasSuggestions);
        if (hasSuggestions) {
            suggestions.getSelectionModel().selectFirst();
            double height = Math.min(completions.size(), 3) * 28 + 4;
            suggestions.setMinHeight(height);
            suggestions.setPrefHeight(height);
            suggestions.setMaxHeight(height);
        }
    }

    public boolean hasSuggestions() {
        return suggestions.isVisible() && !suggestions.getItems().isEmpty();
    }

    /** Moves within suggestions without wrapping unexpectedly at the boundaries. */
    public void moveSelection(int offset) {
        if (!hasSuggestions()) {
            return;
        }
        int index = suggestions.getSelectionModel().getSelectedIndex();
        int nextIndex = Math.max(0, Math.min(suggestions.getItems().size() - 1, index + offset));
        suggestions.getSelectionModel().select(nextIndex);
        suggestions.scrollTo(nextIndex);
    }

    public CommandSuggestion getSelectedSuggestion() {
        return hasSuggestions() ? suggestions.getSelectionModel().getSelectedItem() : null;
    }

    /** Hides the completion list; the contextual syntax remains available. */
    public void dismiss() {
        suggestions.setVisible(false);
        suggestions.setManaged(false);
    }
}

package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/** Live recruitment overview. Stage totals always describe the entire address book. */
public class PipelineDashboard extends UiPart<Region> {
    private final Map<HiringStage, Label> stageCounts = new EnumMap<>(HiringStage.class);
    private final ObservableList<Person> allPersons;
    private final ObservableList<Person> visiblePersons;

    @FXML
    private HBox stageTiles;
    @FXML
    private Label pipelineCount;

    /** Creates tiles and observes both source data and displayed results. */
    public PipelineDashboard(ObservableList<Person> allPersons, ObservableList<Person> visiblePersons) {
        super("PipelineDashboard.fxml");
        this.allPersons = requireNonNull(allPersons);
        this.visiblePersons = requireNonNull(visiblePersons);
        for (HiringStage stage : HiringStage.values()) {
            Label count = new Label("0");
            count.getStyleClass().add("tile-count");
            count.setId("count" + stage.name());
            Label title = new Label(stage.toString());
            VBox tile = new VBox(5, title, count);
            tile.getStyleClass().add("stage-tile");
            tile.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tile, Priority.ALWAYS);
            stageTiles.getChildren().add(tile);
            stageCounts.put(stage, count);
        }
        allPersons.addListener((ListChangeListener<Person>) change -> refresh());
        visiblePersons.addListener((ListChangeListener<Person>) change -> refresh());
        refresh();
    }

    private void refresh() {
        PipelineSummary summary = new PipelineSummary(allPersons);
        stageCounts.forEach((stage, count) -> count.setText(Integer.toString(summary.getCount(stage))));
        pipelineCount.setText("All candidates: " + summary.getTotal()
                + "  •  Displayed: " + visiblePersons.size());
    }
}

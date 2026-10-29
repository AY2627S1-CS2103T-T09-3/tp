package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PipelineSummaryTest {
    @Test
    public void countsEveryStage_andEmptyPipeline() {
        List<Person> candidates = java.util.Arrays.stream(HiringStage.values())
                .map(stage -> new PersonBuilder().withStage(stage).build()).toList();
        PipelineSummary summary = new PipelineSummary(candidates);
        assertEquals(5, summary.getTotal());
        for (HiringStage stage : HiringStage.values()) {
            assertEquals(1, summary.getCount(stage));
            assertEquals(0, new PipelineSummary(List.of()).getCount(stage));
        }
    }

    @Test
    public void snapshot_doesNotChangeWhenSourceChanges() {
        ObservableList<Person> candidates = FXCollections.observableArrayList(new PersonBuilder().build());
        PipelineSummary summary = new PipelineSummary(candidates);
        candidates.clear();
        assertEquals(1, summary.getTotal());
        assertEquals(1, summary.getCount(HiringStage.APPLIED));
        assertThrows(NullPointerException.class, () -> new PipelineSummary(null));
        assertThrows(NullPointerException.class, () -> summary.getCount(null));
    }
}

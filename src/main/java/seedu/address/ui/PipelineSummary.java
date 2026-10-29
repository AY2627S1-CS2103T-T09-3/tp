package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/** Immutable stage totals calculated from a snapshot, independent of the active search. */
public final class PipelineSummary {
    private final Map<HiringStage, Integer> counts = new EnumMap<>(HiringStage.class);
    private final int total;

    /** Counts each candidate exactly once in their current stage. */
    public PipelineSummary(List<Person> candidates) {
        requireNonNull(candidates);
        for (HiringStage stage : HiringStage.values()) {
            counts.put(stage, 0);
        }
        for (Person candidate : candidates) {
            HiringStage stage = requireNonNull(candidate).getStage();
            counts.merge(stage, 1, Integer::sum);
        }
        total = candidates.size();
        assert counts.values().stream().mapToInt(Integer::intValue).sum() == total;
    }

    public int getCount(HiringStage stage) {
        return counts.get(requireNonNull(stage));
    }

    public int getTotal() {
        return total;
    }
}

package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import seedu.address.model.Model;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Person;

/**
 * Shows hiring-stage counts for the entire candidate pipeline without changing the displayed list.
 */
public class SummaryCommand extends Command {

    public static final String COMMAND_WORD = "summary";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows hiring-stage counts and a total for all stored candidates. Takes no parameters.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_HEADER = "Hiring-stage summary — all candidates";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        List<Person> candidates = model.getAddressBook().getPersonList();
        Map<HiringStage, Integer> counts = countCandidatesByStage(candidates);
        return new CommandResult(formatSummary(counts, candidates.size()));
    }

    /**
     * Counts the candidates at each stage in a single pass through the stored list.
     */
    private Map<HiringStage, Integer> countCandidatesByStage(List<Person> candidates) {
        Map<HiringStage, Integer> counts = new EnumMap<>(HiringStage.class);
        for (Person candidate : candidates) {
            counts.merge(candidate.getStage(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Formats every stage in pipeline order, including zero counts, followed by the overall total.
     */
    private String formatSummary(Map<HiringStage, Integer> counts, int total) {
        StringBuilder result = new StringBuilder(MESSAGE_HEADER);
        for (HiringStage stage : HiringStage.values()) {
            result.append("\n").append(stage).append(": ").append(counts.getOrDefault(stage, 0));
        }
        return result.append("\nTotal: ").append(total).toString();
    }
}

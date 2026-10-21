package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;

/**
 * Finds and lists all persons whose skills contain an exact, case-insensitive match.
 */
public class FindSkillCommand extends Command {

    public static final String COMMAND_WORD = "findskill";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds candidates by exact skill name (case-insensitive) and displays them as a list with index "
            + "numbers.\n"
            + "Parameters: SKILL | any s/SKILL [s/SKILL]... | all s/SKILL [s/SKILL]...\n"
            + "Examples: " + COMMAND_WORD + " Java; " + COMMAND_WORD + " any s/Java s/Python";

    public static final String MESSAGE_NO_MATCH = "No candidates found matching skill search: %1$s.";
    private static final String MESSAGE_SUCCESS_FORMAT = "Listed %d candidate%s.";

    /** The supported ways of combining multiple skill terms. */
    public enum SearchMode {
        ANY,
        ALL
    }

    private final SearchMode searchMode;
    private final List<Skill> skills;

    /**
     * Creates a single-skill search. This is equivalent to an {@link SearchMode#ANY} search with one skill.
     */
    public FindSkillCommand(Skill skill) {
        this(SearchMode.ANY, List.of(skill));
    }

    /**
     * Creates a search for the supplied skills.
     *
     * @param searchMode whether candidates must have any or all of the skills
     * @param skills skills to search for
     */
    public FindSkillCommand(SearchMode searchMode, Collection<Skill> skills) {
        this.searchMode = requireNonNull(searchMode);
        requireNonNull(skills);
        this.skills = List.copyOf(deduplicateSkills(skills));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(this::matchesSearch);
        int matchingCandidates = model.getFilteredPersonList().size();
        if (matchingCandidates == 0) {
            return new CommandResult(String.format(MESSAGE_NO_MATCH, getSkillDescription()));
        }
        String suffix = matchingCandidates == 1 ? "" : "s";
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, matchingCandidates, suffix));
    }

    private boolean matchesSearch(Person person) {
        if (searchMode == SearchMode.ALL) {
            return skills.stream().allMatch(skill -> hasMatchingSkill(person, skill));
        }
        return skills.stream().anyMatch(skill -> hasMatchingSkill(person, skill));
    }

    private static boolean hasMatchingSkill(Person person, Skill skill) {
        return person.getSkills().stream()
                .anyMatch(candidateSkill -> candidateSkill.value.equalsIgnoreCase(skill.value));
    }

    private static List<Skill> deduplicateSkills(Collection<Skill> skills) {
        List<Skill> deduplicatedSkills = new ArrayList<>();
        for (Skill skill : skills) {
            requireNonNull(skill);
            if (deduplicatedSkills.stream().noneMatch(existing -> existing.value.equalsIgnoreCase(skill.value))) {
                deduplicatedSkills.add(skill);
            }
        }
        return deduplicatedSkills;
    }

    private String getSkillDescription() {
        return skills.stream()
                .map(Skill::toString)
                .collect(Collectors.joining(", "));
    }

    private Set<String> getNormalizedSkills() {
        return skills.stream()
                .map(skill -> skill.value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindSkillCommand otherFindSkillCommand)) {
            return false;
        }

        return searchMode == otherFindSkillCommand.searchMode
                && getNormalizedSkills().equals(otherFindSkillCommand.getNormalizedSkills());
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchMode, getNormalizedSkills());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("searchMode", searchMode)
                .add("skills", skills)
                .toString();
    }
}

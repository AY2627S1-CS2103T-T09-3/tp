package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Supported follow-up views. All views exclude candidates without a scheduled follow-up.
 */
public enum FollowUpFilter {
    PENDING, OVERDUE, TODAY, UPCOMING, COMPLETED, ALL;

    public static final String MESSAGE_CONSTRAINTS = "Follow-up view must be pending, overdue, today, "
            + "upcoming, completed or all.";

    /**
     * Parses a case-insensitive view name.
     */
    public static FollowUpFilter fromString(String text) {
        requireNonNull(text);
        try {
            return valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }

    /**
     * Captures one reference date so all candidates in a view use the same day.
     */
    public Predicate<Person> predicate(LocalDate today) {
        requireNonNull(today);
        return person -> matches(person.getFollowUp(), today);
    }

    /**
     * Matches a reminder against this view, including completion state.
     */
    public boolean matches(FollowUp followUp, LocalDate today) {
        requireNonNull(followUp);
        requireNonNull(today);
        if (!followUp.isScheduled()) {
            return false;
        }
        return switch (this) {
            case ALL -> true;
            case COMPLETED -> followUp.isCompleted();
            case PENDING -> !followUp.isCompleted();
            case OVERDUE -> followUp.isOverdue(today);
            case TODAY -> !followUp.isCompleted() && followUp.getDate().orElseThrow().equals(today);
            case UPCOMING -> !followUp.isCompleted() && followUp.getDate().orElseThrow().isAfter(today);
        };
    }
}

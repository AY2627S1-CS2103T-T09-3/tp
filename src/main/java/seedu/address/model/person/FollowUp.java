package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable candidate follow-up, with an optional date, note and completion state.
 */
public final class FollowUp {
    public static final String MESSAGE_DATE_CONSTRAINTS = "Follow-up dates must be valid dates in YYYY-MM-DD format "
            + "between 0001-01-01 and 9999-12-31.";
    public static final String MESSAGE_NOTE_CONSTRAINTS =
            "Follow-up notes must be a single line of at most 200 characters.";
    public static final String MESSAGE_EMPTY_CONSTRAINTS = "A follow-up note or completion state requires a date.";

    private final LocalDate date;
    private final String note;
    private final boolean completed;

    /**
     * Creates a follow-up. An empty date represents a candidate without a follow-up.
     */
    public FollowUp(String date, String note, boolean completed) {
        requireNonNull(date);
        requireNonNull(note);
        checkArgument(date.isEmpty() || isValidDate(date), MESSAGE_DATE_CONSTRAINTS);
        checkArgument(isValidNote(note), MESSAGE_NOTE_CONSTRAINTS);
        checkArgument(!date.isEmpty() || (note.isEmpty() && !completed), MESSAGE_EMPTY_CONSTRAINTS);
        this.date = date.isEmpty() ? null : LocalDate.parse(date);
        this.note = note.strip();
        this.completed = completed;
    }

    /**
     * Returns the absence of a follow-up.
     */
    public static FollowUp empty() {
        return new FollowUp("", "", false);
    }

    /**
     * Checks both the exact date format and calendar validity, including leap years.
     */
    public static boolean isValidDate(String value) {
        requireNonNull(value);
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return false;
        }
        try {
            return LocalDate.parse(value).getYear() >= 1;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks the note length and disallows line breaks and other control characters.
     */
    public static boolean isValidNote(String value) {
        requireNonNull(value);
        return value.length() <= 200 && value.codePoints().noneMatch(codePoint -> Character.isISOControl(codePoint)
                || codePoint == 0x2028 || codePoint == 0x2029);
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    public String getDateText() {
        return date == null ? "" : date.toString();
    }

    public String getNote() {
        return note;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isScheduled() {
        return date != null;
    }

    /**
     * Returns whether this follow-up is pending and earlier than the reference date.
     */
    public boolean isOverdue(LocalDate today) {
        requireNonNull(today);
        return date != null && !completed && date.isBefore(today);
    }

    /**
     * Returns a completed copy, preserving the scheduled date and note.
     */
    public FollowUp complete() {
        if (!isScheduled()) {
            throw new IllegalStateException("There is no follow-up to complete.");
        }
        return new FollowUp(getDateText(), note, true);
    }

    /**
     * Formats the reminder relative to the given local date.
     */
    public String format(LocalDate today) {
        requireNonNull(today);
        if (!isScheduled()) {
            return "";
        }
        String status;
        if (completed) {
            status = "completed";
        } else if (isOverdue(today)) {
            status = "overdue";
        } else if (date.equals(today)) {
            status = "today";
        } else {
            status = "upcoming";
        }
        return "Follow-up: " + date + " (" + status + ")" + (note.isEmpty() ? "" : " — " + note);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FollowUp followUp
                && Objects.equals(date, followUp.date)
                && note.equals(followUp.note) && completed == followUp.completed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, note, completed);
    }

    @Override
    public String toString() {
        return isScheduled() ? getDateText() + (completed ? " [completed]" : " [pending]")
                + (note.isEmpty() ? "" : ": " + note) : "No follow-up";
    }
}

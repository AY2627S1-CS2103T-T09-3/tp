package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * The current stage of a candidate in the hiring process.
 * Candidates may move freely between stages, including to correct an earlier update.
 */
public enum HiringStage {
    APPLIED("Applied"),
    SCREENED("Screened"),
    INTERVIEW("Interview"),
    OFFERED("Offered"),
    REJECTED("Rejected");

    public static final String MESSAGE_CONSTRAINTS =
            "Hiring stage must be one of: Applied, Screened, Interview, Offered, Rejected.";

    private final String displayName;

    HiringStage(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Parses a stage name, ignoring case and surrounding whitespace.
     *
     * @throws IllegalArgumentException if the name does not identify one of the five stages
     */
    public static HiringStage fromString(String value) {
        requireNonNull(value);
        String trimmedValue = value.trim();
        for (HiringStage stage : values()) {
            if (stage.displayName.equalsIgnoreCase(trimmedValue)) {
                return stage;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return displayName;
    }
}

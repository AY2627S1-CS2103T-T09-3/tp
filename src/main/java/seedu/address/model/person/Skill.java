package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a candidate's skill.
 * Guarantees: immutable; is valid as declared in {@link #isValidSkill(String)}.
 */
public class Skill {

    public static final String MESSAGE_CONSTRAINTS =
            "Skills should contain only alphanumeric characters and be between 1 and 30 characters long";
    public static final String VALIDATION_REGEX = "\\p{Alnum}{1,30}";

    public final String value;

    /**
     * Constructs a {@code Skill}.
     *
     * @param skill A valid skill.
     */
    public Skill(String skill) {
        requireNonNull(skill);
        checkArgument(isValidSkill(skill), MESSAGE_CONSTRAINTS);
        value = skill;
    }

    /**
     * Returns true if a given string is a valid skill.
     */
    public static boolean isValidSkill(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Skill otherSkill)) {
            return false;
        }

        return value.equals(otherSkill.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}

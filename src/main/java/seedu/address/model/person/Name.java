package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS = "Invalid name: names may only contain letters, spaces, hyphens, "
            + "and apostrophes, and must be 1-100 characters long.";

    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{N} '-]{0,99}";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        String normalizedName = normalizeName(name);
        checkArgument(isValidName(normalizedName), MESSAGE_CONSTRAINTS);
        fullName = normalizedName;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        return normalizeName(test).matches(VALIDATION_REGEX);
    }

    /**
     * Trims leading and trailing whitespace and collapses repeated internal whitespace.
     */
    public static String normalizeName(String name) {
        requireNonNull(name);
        return name.trim().replaceAll("\\s+", " ");
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}

package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Invalid phone number: must be 7-15 digits, optionally starting with +.";
    public static final String VALIDATION_REGEX = "\\+?\\d{7,15}";
    private static final String LEGACY_VALIDATION_REGEX = "\\d{3,}";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    private Phone(String phone, boolean legacyValue) {
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if a given string was valid under the original AB3 phone constraints.
     */
    public static boolean isValidLegacyPhone(String test) {
        requireNonNull(test);
        return test.matches(LEGACY_VALIDATION_REGEX);
    }

    /**
     * Creates a phone from an old stored value without applying the current length limit.
     * This path is intended only for deserializing legacy address-book data.
     */
    public static Phone fromLegacyValue(String phone) {
        requireNonNull(phone);
        checkArgument(isValidLegacyPhone(phone), MESSAGE_CONSTRAINTS);
        return new Phone(phone, true);
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

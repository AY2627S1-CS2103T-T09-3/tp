package seedu.address.logic.commands;

import java.util.List;
import java.util.function.Function;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Finds candidates in a displayed list using the name normalization used for candidate identity.
 */
final class PersonLookup {

    private PersonLookup() {
    }

    /**
     * Returns the unique candidate matching the normalized full name, ignoring case.
     *
     * @throws CommandException with the supplied message if no candidate or multiple candidates match
     */
    static Person findByName(List<Person> displayedPersons, Name targetName,
            String notFoundMessage, String ambiguousMessage) throws CommandException {
        return findByName(displayedPersons, targetName, notFoundMessage, matches -> ambiguousMessage);
    }

    /**
     * Returns the unique candidate matching the normalized full name, ignoring case.
     * Uses the matching candidates to construct the error message when the name is ambiguous.
     *
     * @throws CommandException if no candidate or multiple candidates match
     */
    static Person findByName(List<Person> displayedPersons, Name targetName,
            String notFoundMessage, Function<List<Person>, String> ambiguousMessage) throws CommandException {
        String normalizedName = Name.normalizeName(targetName.fullName);
        List<Person> matches = displayedPersons.stream()
                .filter(person -> Name.normalizeName(person.getName().fullName).equalsIgnoreCase(normalizedName))
                .toList();
        if (matches.isEmpty()) {
            throw new CommandException(notFoundMessage);
        }
        if (matches.size() > 1) {
            throw new CommandException(ambiguousMessage.apply(matches));
        }
        return matches.get(0);
    }
}

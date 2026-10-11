package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILL;

import java.util.Comparator;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a candidate to the pipeline. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_SKILL + "SKILL [" + PREFIX_SKILL + "SKILL]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_SKILL + "Java "
            + PREFIX_SKILL + "SQL";

    public static final String MESSAGE_SUCCESS = "New candidate added: %1$s | %2$s | Skills: %3$s";
    public static final String MESSAGE_NO_SKILL = "At least one skill (s/) must be provided.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown field: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "This candidate already exists in your pipeline (same name and phone number).";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);
        String skills = toAdd.getSkills().stream()
                .sorted(Comparator.comparing((Skill skill) -> skill.value, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(skill -> skill.value))
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName(), toAdd.getPhone(), skills));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}

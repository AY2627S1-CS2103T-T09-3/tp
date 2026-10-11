package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILL;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.HiringStage;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.person.Skill;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    private static final Pattern FIELD_PREFIX_PATTERN = Pattern.compile("(?<!\\S)([A-Za-z][A-Za-z0-9_-]*)/");
    private static final Set<String> SUPPORTED_PREFIXES = Set.of(
            PREFIX_NAME.toString(), PREFIX_PHONE.toString(), PREFIX_SKILL.toString());

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        verifyNoUnknownPrefixes(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_SKILL);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_PHONE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        List<String> skillValues = argMultimap.getAllValues(PREFIX_SKILL);
        if (skillValues.isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_NO_SKILL);
        }
        Map<String, Skill> uniqueSkills = new LinkedHashMap<>();
        for (String skillValue : skillValues) {
            Skill skill = ParserUtil.parseSkill(skillValue);
            uniqueSkills.putIfAbsent(skill.value.toLowerCase(Locale.ROOT), skill);
        }

        Person person = new Person(name, phone, new Email(""), new Address(""), new Remark(""),
                HiringStage.APPLIED, Set.of(), new LinkedHashSet<>(uniqueSkills.values()));

        return new AddCommand(person);
    }

    /**
     * Rejects field-like tokens that are not part of the Add Candidate syntax.
     * ArgumentTokenizer only recognizes prefixes supplied to it, so an unknown prefix would otherwise
     * become part of a neighboring field's value.
     */
    private static void verifyNoUnknownPrefixes(String args) throws ParseException {
        Matcher matcher = FIELD_PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            String prefix = matcher.group(1) + "/";
            if (!SUPPORTED_PREFIXES.contains(prefix)) {
                throw new ParseException(String.format(AddCommand.MESSAGE_UNKNOWN_FIELD, prefix));
            }
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}

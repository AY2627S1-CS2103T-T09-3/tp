package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILL;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.FindSkillCommand;
import seedu.address.logic.commands.FindSkillCommand.SearchMode;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Skill;

/**
 * Parses input arguments and creates a new {@link FindSkillCommand} object.
 */
public class FindSkillCommandParser implements Parser<FindSkillCommand> {

    /**
     * Parses the given arguments in the context of the {@link FindSkillCommand} and returns a command.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindSkillCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindSkillCommand.MESSAGE_USAGE));
        }

        String[] modeAndSkills = trimmedArgs.split("\\s+", 2);
        String modeText = modeAndSkills[0];
        if (!modeText.equalsIgnoreCase("any") && !modeText.equalsIgnoreCase("all")) {
            return new FindSkillCommand(ParserUtil.parseSkill(trimmedArgs));
        }

        if (modeAndSkills.length == 1) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindSkillCommand.MESSAGE_USAGE));
        }

        SearchMode searchMode = modeText.equalsIgnoreCase("all") ? SearchMode.ALL : SearchMode.ANY;
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + modeAndSkills[1], PREFIX_SKILL);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindSkillCommand.MESSAGE_USAGE));
        }

        List<String> skillValues = argMultimap.getAllValues(PREFIX_SKILL);
        List<Skill> skills = new ArrayList<>();
        for (String skillValue : skillValues) {
            Skill skill = ParserUtil.parseSkill(skillValue);
            if (skills.stream().noneMatch(existing -> existing.value.equalsIgnoreCase(skill.value))) {
                skills.add(skill);
            }
        }
        return new FindSkillCommand(searchMode, skills);
    }
}

package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Skill;

/**
 * Jackson-friendly version of {@link Skill}.
 */
class JsonAdaptedSkill {

    private final String skill;

    /**
     * Constructs a {@code JsonAdaptedSkill} with the given {@code skill}.
     */
    @JsonCreator
    public JsonAdaptedSkill(String skill) {
        this.skill = skill;
    }

    /**
     * Converts a given {@code Skill} into this class for Jackson use.
     */
    public JsonAdaptedSkill(Skill source) {
        skill = source.value;
    }

    @JsonValue
    public String getSkill() {
        return skill;
    }

    /**
     * Converts this Jackson-friendly adapted skill object into the model's {@code Skill} object.
     *
     * @throws IllegalValueException if the stored skill violates the model constraints.
     */
    public Skill toModelType() throws IllegalValueException {
        if (skill == null || !Skill.isValidSkill(skill)) {
            throw new IllegalValueException(Skill.MESSAGE_CONSTRAINTS);
        }
        return new Skill(skill);
    }
}

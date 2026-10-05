package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SkillTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Skill(null));
    }

    @Test
    public void constructor_invalidSkill_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Skill(""));
        assertThrows(IllegalArgumentException.class, () -> new Skill(" "));
        assertThrows(IllegalArgumentException.class, () -> new Skill("Java Script"));
        assertThrows(IllegalArgumentException.class, () -> new Skill("C++"));
        assertThrows(IllegalArgumentException.class, () -> new Skill("1234567890123456789012345678901"));
    }

    @Test
    public void isValidSkill() {
        // null skill
        assertThrows(NullPointerException.class, () -> Skill.isValidSkill(null));

        // invalid skills
        assertFalse(Skill.isValidSkill(""));
        assertFalse(Skill.isValidSkill(" "));
        assertFalse(Skill.isValidSkill("Java Script"));
        assertFalse(Skill.isValidSkill("C++"));
        assertFalse(Skill.isValidSkill("1234567890123456789012345678901"));

        // valid skills
        assertTrue(Skill.isValidSkill("Java"));
        assertTrue(Skill.isValidSkill("J"));
        assertTrue(Skill.isValidSkill("123456789012345678901234567890"));
        assertTrue(Skill.isValidSkill("java"));
        assertTrue(Skill.isValidSkill("JAVA"));
        assertTrue(Skill.isValidSkill("Java17"));
    }

    @Test
    public void equals() {
        Skill skill = new Skill("Java");

        // same values -> returns true
        assertTrue(skill.equals(new Skill("Java")));

        // same object -> returns true
        assertTrue(skill.equals(skill));

        // null -> returns false
        assertFalse(skill.equals(null));

        // different types -> returns false
        assertFalse(skill.equals(5.0f));

        // different values -> returns false
        assertFalse(skill.equals(new Skill("Python")));
    }

    @Test
    public void hashCode_sameValues_returnsSameHashCode() {
        Skill firstSkill = new Skill("Java");
        Skill secondSkill = new Skill("Java");

        assertEquals(firstSkill.hashCode(), secondSkill.hashCode());
    }

    @Test
    public void toString_returnsSkillValue() {
        Skill skill = new Skill("Java");

        assertEquals("Java", skill.toString());
    }
}

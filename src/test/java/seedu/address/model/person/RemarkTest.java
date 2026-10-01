package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RemarkTest {
    @Test
    public void constructor_acceptsEmptyTextButRejectsNull() {
        assertEquals("", new Remark("").value);
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equals_comparesText() {
        assertEquals(new Remark("Note"), new Remark("Note"));
        assertEquals(new Remark("Note").hashCode(), new Remark("Note").hashCode());
        assertNotEquals(new Remark("Note"), new Remark("Other"));
        assertNotEquals(new Remark("Note"), null);
    }

    @Test
    public void personEquality_includesRemarkButIdentityDoesNot() {
        Person original = new PersonBuilder().build();
        Person edited = new PersonBuilder(original).withRemark("Note").build();
        assertNotEquals(original, edited);
        assertTrue(original.isSamePerson(edited));
        assertEquals(edited, new PersonBuilder(edited).build());
    }
}

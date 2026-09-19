package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_acceptsAnyString() {
        assertTrue(new Remark("").value.isEmpty());
        assertTrue(new Remark(" ").value.equals(" "));
        assertTrue(new Remark("Likes swimming").value.equals("Likes swimming"));
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes swimming");

        // same values -> returns true
        assertTrue(remark.equals(new Remark("Likes swimming")));

        // same object -> returns true
        assertTrue(remark.equals(remark));

        // null -> returns false
        assertFalse(remark.equals(null));

        // different types -> returns false
        assertFalse(remark.equals(5.0f));

        // different values -> returns false
        assertFalse(remark.equals(new Remark("Likes baseball")));
    }
}

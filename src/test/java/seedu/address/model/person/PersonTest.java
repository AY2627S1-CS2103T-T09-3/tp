package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> person.getSkills().add(new Skill("Java")));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name and same phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, same phone -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has repeated/trailing spaces, same phone -> returns true after normalization
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different skills -> returns false
        editedAlice = new PersonBuilder(ALICE).withSkills("Java").build();
        assertFalse(ALICE.equals(editedAlice));

        // different remark -> returns false
        editedAlice = new PersonBuilder(ALICE).withRemark("Likes swimming").build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void hashCode_equalPersonsWithRemarks_sameHashCode() {
        Person person = new PersonBuilder(ALICE).withRemark("Likes swimming").withSkills("Java").build();
        Person copy = new PersonBuilder(person).build();
        assertEquals(person, copy);
        assertEquals(person.hashCode(), copy.hashCode());
    }

    @Test
    public void constructor_withoutStage_defaultsToApplied() {
        assertEquals(HiringStage.APPLIED, new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags()).getStage());
        assertEquals(HiringStage.APPLIED, new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getRemark(), ALICE.getTags()).getStage());
    }

    @Test
    public void constructor_withoutSkills_defaultsToEmpty() {
        Person person = new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getRemark(), HiringStage.INTERVIEW, ALICE.getTags());

        assertEquals(HiringStage.INTERVIEW, person.getStage());
        assertTrue(person.getSkills().isEmpty());
    }

    @Test
    public void constructor_nullStage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonBuilder().withStage(null).build());
    }

    @Test
    public void stage_changesEqualityButNotIdentity() {
        Person updated = new PersonBuilder(ALICE).withStage(HiringStage.INTERVIEW).build();
        assertFalse(ALICE.equals(updated));
        assertTrue(ALICE.isSamePerson(updated));
        Person copy = new PersonBuilder(updated).build();
        assertEquals(updated, copy);
        assertEquals(updated.hashCode(), copy.hashCode());
    }

    @Test
    public void skills_areCopiedAndDefaultToEmpty() {
        Person person = new PersonBuilder().withSkills("Java", "SQL").build();
        Person copy = new PersonBuilder(person).build();

        assertEquals(Set.of(new Skill("Java"), new Skill("SQL")), person.getSkills());
        assertEquals(person.getSkills(), copy.getSkills());
        assertEquals(Set.of(), new PersonBuilder().build().getSkills());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress()
                + ", remark=" + ALICE.getRemark() + ", followUp=" + ALICE.getFollowUp()
                + ", stage=" + ALICE.getStage() + ", tags=" + ALICE.getTags()
                + ", skills=" + ALICE.getSkills() + "}";
        assertEquals(expected, ALICE.toString());
    }
}

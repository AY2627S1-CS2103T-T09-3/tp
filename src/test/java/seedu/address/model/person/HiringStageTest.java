package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class HiringStageTest {

    @Test
    public void fromString_validNames_normalizesCaseAndWhitespace() {
        for (HiringStage stage : HiringStage.values()) {
            assertEquals(stage, HiringStage.fromString(stage.toString()));
            assertEquals(stage, HiringStage.fromString(stage.name()));
            assertEquals(stage, HiringStage.fromString(" \t" + stage.name().toLowerCase(Locale.ROOT) + "\n"));
        }
        assertEquals(HiringStage.INTERVIEW, HiringStage.fromString("iNtErViEw"));
    }

    @Test
    public void fromString_invalidName_throwsIllegalArgumentException() {
        for (String value : new String[]{"", " ", "Hired", "Applied Screened", "Interview!", "1"}) {
            assertThrows(IllegalArgumentException.class, () -> HiringStage.fromString(value));
        }
        assertThrows(NullPointerException.class, () -> HiringStage.fromString(null));
    }

    @Test
    public void toString_usesDisplayNames() {
        assertEquals("Applied", HiringStage.APPLIED.toString());
        assertEquals("Screened", HiringStage.SCREENED.toString());
        assertEquals("Interview", HiringStage.INTERVIEW.toString());
        assertEquals("Offered", HiringStage.OFFERED.toString());
        assertEquals("Rejected", HiringStage.REJECTED.toString());
    }
}

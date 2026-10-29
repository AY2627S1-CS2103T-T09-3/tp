package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CandidateExportServiceTest {
    @Test
    public void toCsv_includesHeadersEscapesFieldsAndPreservesOrder() {
        Person candidate = new PersonBuilder().withName("A Candidate").withEmail("ab@example.com")
                .withSkills("Java", "SQL").withTags("referral").withRemark("Call, soon").build();

        String csv = CandidateExportService.toCsv(List.of(candidate));

        assertTrue(csv.startsWith("Name,Email,Phone,Stage,Skills,Tags,Remark\n"));
        assertTrue(csv.contains("\"A Candidate\""));
        assertTrue(csv.contains("\"Java; SQL\""));
        assertTrue(csv.contains("\"Call, soon\""));
    }

    @Test
    public void toJson_containsStructuredCandidateFieldsAndEscapesQuotes() {
        Person candidate = new PersonBuilder().withName("A Candidate").withEmail("ab@example.com")
                .withSkills("Java").withTags("referral").withRemark("He said \"Call soon\"").build();

        String json = CandidateExportService.toJson(List.of(candidate));

        assertTrue(json.startsWith("{\n  \"candidates\": ["));
        assertTrue(json.contains("\"name\": \"A Candidate\""));
        assertTrue(json.contains("\"skills\": [\"Java\"]"));
        assertTrue(json.contains("\"remark\": \"He said \\\"Call soon\\\"\""));
        assertTrue(json.endsWith("  ]\n}\n"));
    }
}

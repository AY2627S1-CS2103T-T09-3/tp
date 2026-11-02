package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CandidateExportServiceTest {
    @Test
    public void toCsv_emptyCollection_containsHeaderOnly() {
        assertEquals("Name,Email,Phone,Address,Stage,Skills,Tags,Remark\n", CandidateExportService.toCsv(List.of()));
    }

    @Test
    public void toCsv_includesHeadersEscapesFieldsAndPreservesOrder() {
        Person candidate = new PersonBuilder().withName("A Candidate").withEmail("ab@example.com")
                .withSkills("Java", "SQL").withTags("referral").withRemark("Call, soon").build();

        String csv = CandidateExportService.toCsv(List.of(candidate));

        assertTrue(csv.startsWith("Name,Email,Phone,Address,Stage,Skills,Tags,Remark\n"));
        assertTrue(csv.contains("\"A Candidate\""));
        assertTrue(csv.contains("\"Java; SQL\""));
        assertTrue(csv.contains("\"Call, soon\""));
    }

    @Test
    public void toCsv_includesEscapedAndEmptyAddressesInCandidateOrder() {
        Person first = new PersonBuilder().withName("First").withEmail("").withPhone("91234567")
                .withAddress("123, \"Main\" Street \\ 北京").withSkills().withTags().withRemark("").build();
        Person second = new PersonBuilder(first).withName("Second").withAddress("").build();

        String csv = CandidateExportService.toCsv(List.of(first, second));

        assertEquals("Name,Email,Phone,Address,Stage,Skills,Tags,Remark\n"
                + "\"First\",\"\",\"91234567\",\"123, \"\"Main\"\" Street \\ 北京\",\"Applied\",\"\",\"\",\"\"\n"
                + "\"Second\",\"\",\"91234567\",\"\",\"Applied\",\"\",\"\",\"\"\n", csv);
    }

    @Test
    public void toJson_preservesEscapedAndEmptyAddressesInCandidateOrder() throws Exception {
        String address = "123, \"Main\" Street \\ 北京";
        Person first = new PersonBuilder().withName("First").withAddress(address).build();
        Person second = new PersonBuilder(first).withName("Second").withAddress("").build();

        JsonNode candidates = new ObjectMapper().readTree(CandidateExportService.toJson(List.of(first, second)))
                .get("candidates");

        assertEquals(2, candidates.size());
        assertEquals("First", candidates.get(0).get("name").asText());
        assertEquals(address, candidates.get(0).get("address").asText());
        assertEquals("Second", candidates.get(1).get("name").asText());
        assertEquals("", candidates.get(1).get("address").asText());
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

    @Test
    public void toJson_preservesCandidateOrder() {
        Person first = new PersonBuilder().withName("First Candidate").build();
        Person second = new PersonBuilder().withName("Second Candidate").withPhone("81234567").build();

        String json = CandidateExportService.toJson(List.of(first, second));

        assertTrue(json.indexOf("First Candidate") < json.indexOf("Second Candidate"));
    }
}

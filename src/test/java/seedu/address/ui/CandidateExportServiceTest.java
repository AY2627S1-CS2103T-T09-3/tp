package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CandidateExportServiceTest {
    @Test
    public void toCsv_emptyCollection_containsHeaderOnly() {
        assertEquals("Name,Email,Phone,Address,Stage,Skills,Tags,Remark,Follow-up Date,Follow-up Note,"
                + "Follow-up Completed\n", CandidateExportService.toCsv(List.of()));
    }

    @Test
    public void toCsv_includesAllCandidateFieldsEscapesFieldsAndPreservesOrder() {
        Person candidate = new PersonBuilder().withName("A Candidate").withEmail("ab@example.com")
                .withAddress("123, Main \"Street\"").withSkills("Java", "SQL").withTags("referral")
                .withRemark("Call, soon").build().withFollowUp(new FollowUp("2026-11-05", "Call, \"soon\"", true));

        String csv = CandidateExportService.toCsv(List.of(candidate));

        assertTrue(csv.startsWith("Name,Email,Phone,Address,Stage,Skills,Tags,Remark,Follow-up Date,Follow-up Note,"
                + "Follow-up Completed\n"));
        assertTrue(csv.contains("\"A Candidate\""));
        assertTrue(csv.contains("\"123, Main \"\"Street\"\"\""));
        assertTrue(csv.contains("\"Java; SQL\""));
        assertTrue(csv.contains("\"Call, soon\""));
        assertTrue(csv.contains("\"2026-11-05\""));
        assertTrue(csv.contains("\"Call, \"\"soon\"\"\""));
        assertTrue(csv.endsWith("\"true\"\n"));
    }

    @Test
    public void toCsv_includesEscapedAndEmptyAddressesInCandidateOrder() {
        Person first = new PersonBuilder().withName("First").withEmail("").withPhone("91234567")
                .withAddress("123, \"Main\" Street \\ 北京").withSkills().withTags().withRemark("").build();
        Person second = new PersonBuilder(first).withName("Second").withAddress("").build();

        String csv = CandidateExportService.toCsv(List.of(first, second));
        List<String> rows = csv.lines().toList();

        assertEquals(3, rows.size());
        assertEquals("Name,Email,Phone,Address,Stage,Skills,Tags,Remark,"
                + "Follow-up Date,Follow-up Note,Follow-up Completed", rows.get(0));
        assertTrue(rows.get(1).startsWith(
                "\"First\",\"\",\"91234567\",\"123, \"\"Main\"\" Street \\ 北京\","
                        + "\"Applied\",\"\",\"\",\"\","));
        assertTrue(rows.get(2).startsWith(
                "\"Second\",\"\",\"91234567\",\"\",\"Applied\",\"\",\"\",\"\","));
    }

    @Test
    public void toJson_preservesEscapedAndEmptyAddressesInCandidateOrder() throws Exception {
        String address = "123, \"Main\" Street \\ 北京";
        Person first = new PersonBuilder().withName("First").withAddress(address).build();
        Person second = new PersonBuilder(first).withName("Second").withAddress("").build();

        JsonNode candidates = new ObjectMapper().readTree(
                CandidateExportService.toJson(List.of(first, second))).get("candidates");

        assertEquals(2, candidates.size());
        assertEquals("First", candidates.get(0).get("name").asText());
        assertEquals(address, candidates.get(0).get("address").asText());
        assertEquals("Second", candidates.get(1).get("name").asText());
        assertEquals("", candidates.get(1).get("address").asText());
    }

    @Test
    public void toJson_containsAllCandidateFieldsAndEscapesQuotes() {
        Person candidate = new PersonBuilder().withName("A Candidate").withEmail("ab@example.com")
                .withAddress("123 Main Street").withSkills("Java").withTags("referral")
                .withRemark("He said \"Call soon\"").build()
                .withFollowUp(new FollowUp("2026-11-05", "Call soon", false));

        String json = CandidateExportService.toJson(List.of(candidate));

        assertTrue(json.startsWith("{\n  \"candidates\": ["));
        assertTrue(json.contains("\"name\": \"A Candidate\""));
        assertTrue(json.contains("\"address\": \"123 Main Street\""));
        assertTrue(json.contains("\"skills\": [\"Java\"]"));
        assertTrue(json.contains("\"remark\": \"He said \\\"Call soon\\\"\""));
        assertTrue(json.contains("\"followUp\": {"));
        assertTrue(json.contains("\"date\": \"2026-11-05\""));
        assertTrue(json.contains("\"note\": \"Call soon\""));
        assertTrue(json.contains("\"completed\": false"));
        assertTrue(json.endsWith("  ]\n}\n"));
    }

    @Test
    public void toJson_emptyOptionalFields_areExportedAsEmptyValues() {
        Person candidate = new PersonBuilder().withEmail("").withAddress("").build();

        String json = CandidateExportService.toJson(List.of(candidate));

        assertTrue(json.contains("\"email\": \"\""));
        assertTrue(json.contains("\"address\": \"\""));
        assertTrue(json.contains("\"date\": \"\""));
        assertTrue(json.contains("\"note\": \"\""));
        assertTrue(json.contains("\"completed\": false"));
    }

    @Test
    public void toJson_controlCharacters_areEscaped() {
        Person candidate = new PersonBuilder().withRemark("before\b\f\u0001after").build();

        String json = CandidateExportService.toJson(List.of(candidate));

        assertTrue(json.contains("before\\b\\f\\u0001after"));
    }

    @Test
    public void toCsv_formulaLikeValues_areMarkedAsText() {
        for (String value : List.of("=SUM(1,2)", "+123", "-123", "@command",
                "  =SUM(1,2)", "\t+123", "\u0001-123", "\u0000@command")) {
            Person candidate = new PersonBuilder().withRemark(value).build();

            String csv = CandidateExportService.toCsv(List.of(candidate));

            assertTrue(csv.contains("\"'" + value + "\""));
        }
    }

    @Test
    public void toCsv_ordinaryWhitespaceAndMultilingualValues_arePreserved() {
        List<Person> candidates = List.of(
                new PersonBuilder().withRemark("  你好世界").build(),
                new PersonBuilder().withName("Élodie Candidate").withPhone("81234567")
                        .withRemark("\téclair").build());

        String csv = CandidateExportService.toCsv(candidates);

        assertTrue(csv.contains("\"  你好世界\""));
        assertTrue(csv.contains("\"\téclair\""));
        assertFalse(csv.contains("\"'  你好世界\""));
        assertFalse(csv.contains("\"'\téclair\""));
    }

    @Test
    public void toJson_preservesCandidateOrder() {
        Person first = new PersonBuilder().withName("First Candidate").build();
        Person second = new PersonBuilder().withName("Second Candidate").withPhone("81234567").build();

        String json = CandidateExportService.toJson(List.of(first, second));

        assertTrue(json.indexOf("First Candidate") < json.indexOf("Second Candidate"));
    }
}

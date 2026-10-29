package seedu.address.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.model.person.Person;

/** Provides deterministic, dependency-free exports for the candidate workspace. */
public final class CandidateExportService {
    private static final String[] CSV_HEADERS = {"Name", "Email", "Phone", "Stage", "Skills", "Tags", "Remark"};

    private CandidateExportService() {
    }

    /** Returns a CSV document containing the supplied candidates in their current display order. */
    public static String toCsv(Collection<Person> candidates) {
        StringBuilder output = new StringBuilder();
        output.append(String.join(",", CSV_HEADERS)).append('\n');
        candidates.forEach(candidate -> output.append(String.join(",",
                csv(candidate.getName().fullName),
                csv(candidate.getEmail().value),
                csv(candidate.getPhone().value),
                csv(candidate.getStage().toString()),
                csv(joinSkills(candidate)),
                csv(candidate.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining("; "))),
                csv(candidate.getRemark().value))).append('\n'));
        return output.toString();
    }

    /** Returns a readable JSON document containing the supplied candidates. */
    public static String toJson(Collection<Person> candidates) {
        StringBuilder output = new StringBuilder("{\n  \"candidates\": [\n");
        List<Person> ordered = List.copyOf(candidates);
        for (int i = 0; i < ordered.size(); i++) {
            Person candidate = ordered.get(i);
            output.append("    {\n")
                    .append("      \"name\": \"").append(json(candidate.getName().fullName)).append("\",\n")
                    .append("      \"email\": \"").append(json(candidate.getEmail().value)).append("\",\n")
                    .append("      \"phone\": \"").append(json(candidate.getPhone().value)).append("\",\n")
                    .append("      \"stage\": \"").append(json(candidate.getStage().toString())).append("\",\n")
                    .append("      \"skills\": [").append(jsonArray(candidate.getSkills().stream()
                            .map(skill -> skill.value).sorted().collect(Collectors.toList()))).append("],\n")
                    .append("      \"tags\": [").append(jsonArray(candidate.getTags().stream()
                            .map(tag -> tag.tagName).sorted().collect(Collectors.toList()))).append("],\n")
                    .append("      \"remark\": \"").append(json(candidate.getRemark().value)).append("\"\n")
                    .append("    }").append(i + 1 == ordered.size() ? "\n" : ",\n");
        }
        return output.append("  ]\n}\n").toString();
    }

    private static String joinSkills(Person candidate) {
        return candidate.getSkills().stream().map(skill -> skill.value).sorted().collect(Collectors.joining("; "));
    }

    private static String csv(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static String jsonArray(List<String> values) {
        return values.stream().map(value -> "\"" + json(value) + "\"").collect(Collectors.joining(", "));
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}

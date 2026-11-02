package seedu.address.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.model.person.Person;

/** Provides deterministic, dependency-free exports for the candidate workspace. */
public final class CandidateExportService {
    private static final String[] CSV_HEADERS = {"Name", "Email", "Phone", "Address", "Stage", "Skills", "Tags",
        "Remark", "Follow-up Date", "Follow-up Note", "Follow-up Completed"};

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
                csv(candidate.getAddress().value),
                csv(candidate.getStage().toString()),
                csv(joinSkills(candidate)),
                csv(candidate.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining("; "))),
                csv(candidate.getRemark().value),
                csv(candidate.getFollowUp().getDateText()),
                csv(candidate.getFollowUp().getNote()),
                csv(Boolean.toString(candidate.getFollowUp().isCompleted())))).append('\n'));
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
                    .append("      \"address\": \"").append(json(candidate.getAddress().value)).append("\",\n")
                    .append("      \"stage\": \"").append(json(candidate.getStage().toString())).append("\",\n")
                    .append("      \"skills\": [").append(jsonArray(candidate.getSkills().stream()
                            .map(skill -> skill.value).sorted().collect(Collectors.toList()))).append("],\n")
                    .append("      \"tags\": [").append(jsonArray(candidate.getTags().stream()
                            .map(tag -> tag.tagName).sorted().collect(Collectors.toList()))).append("],\n")
                    .append("      \"remark\": \"").append(json(candidate.getRemark().value)).append("\",\n")
                    .append("      \"followUp\": {\n")
                    .append("        \"date\": \"").append(json(candidate.getFollowUp().getDateText()))
                    .append("\",\n")
                    .append("        \"note\": \"").append(json(candidate.getFollowUp().getNote())).append("\",\n")
                    .append("        \"completed\": ").append(candidate.getFollowUp().isCompleted()).append("\n")
                    .append("      }\n")
                    .append("    }").append(i + 1 == ordered.size() ? "\n" : ",\n");
        }
        return output.append("  ]\n}\n").toString();
    }

    private static String joinSkills(Person candidate) {
        return candidate.getSkills().stream().map(skill -> skill.value).sorted().collect(Collectors.joining("; "));
    }

    private static String csv(String value) {
        String safeValue = isFormulaLike(value) ? "'" + value : value;
        String escaped = safeValue.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    /** Prevents spreadsheet applications from interpreting exported text as a formula. */
    private static boolean isFormulaLike(String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (Character.isWhitespace(character) || Character.isSpaceChar(character)
                    || Character.isISOControl(character)) {
                continue;
            }
            return "=+-@".indexOf(character) >= 0;
        }
        return false;
    }

    private static String jsonArray(List<String> values) {
        return values.stream().map(value -> "\"" + json(value) + "\"").collect(Collectors.joining(", "));
    }

    private static String json(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char character : value.toCharArray()) {
            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '\"' -> escaped.append("\\\"");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}

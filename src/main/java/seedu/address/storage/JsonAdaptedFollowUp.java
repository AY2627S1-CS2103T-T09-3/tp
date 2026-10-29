package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUp;

/**
 * Stored follow-up data, validated before it enters the model.
 */
class JsonAdaptedFollowUp {
    private final String date;
    private final String note;
    private final Boolean completed;

    @JsonCreator
    public JsonAdaptedFollowUp(@JsonProperty("date") String date, @JsonProperty("note") String note,
            @JsonProperty("completed") Boolean completed) {
        this.date = date;
        this.note = note;
        this.completed = completed;
    }

    public JsonAdaptedFollowUp(FollowUp followUp) {
        date = followUp.getDateText();
        note = followUp.getNote();
        completed = followUp.isCompleted();
    }

    /**
     * Rejects malformed saved reminders rather than silently dropping candidate information.
     */
    public FollowUp toModelType() throws IllegalValueException {
        if (date == null || note == null || completed == null) {
            throw new IllegalValueException("Follow-up date, note and completed fields must all be present.");
        }
        try {
            return new FollowUp(date, note, completed);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}

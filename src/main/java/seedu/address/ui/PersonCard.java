package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Skill;

/** Compact candidate summary; full contact information belongs in the profile panel. */
public class PersonCard extends UiPart<Region> {
    private static final int MAX_PREVIEW_SKILLS = 3;

    public final Person person;

    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label hiringStage;
    @FXML
    private FlowPane skills;
    @FXML
    private Label followUp;

    /** Creates a card with an index matching the model's displayed list. */
    public PersonCard(Person person, int displayedIndex) {
        super("PersonListCard.fxml");
        this.person = requireNonNull(person);
        if (displayedIndex < 1) {
            throw new IllegalArgumentException("Displayed index must be positive");
        }
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        name.setTooltip(new Tooltip(person.getName().fullName));
        followUp.setText(person.getFollowUp().format(LocalDate.now()));
        followUp.setVisible(person.getFollowUp().isScheduled());
        followUp.setManaged(person.getFollowUp().isScheduled());
        followUp.setTooltip(new Tooltip(followUp.getText()));
        hiringStage.setText(person.getStage().toString());
        hiringStage.getStyleClass().add("stage-" + person.getStage().name().toLowerCase(Locale.ROOT));
        List<Skill> orderedSkills = person.getSkills().stream().sorted(Skill.DISPLAY_ORDER).toList();
        orderedSkills.stream().limit(MAX_PREVIEW_SKILLS).forEach(skill -> addSkill(skill.value));
        if (orderedSkills.isEmpty()) {
            addSkill("No skills");
        } else if (orderedSkills.size() > MAX_PREVIEW_SKILLS) {
            addSkill("+" + (orderedSkills.size() - MAX_PREVIEW_SKILLS) + " more");
        }
        getRoot().setAccessibleText(displayedIndex + ". " + person.getName() + ", " + person.getStage());
    }

    private void addSkill(String value) {
        Label chip = new Label(value);
        chip.getStyleClass().add("chip");
        skills.getChildren().add(chip);
    }
}

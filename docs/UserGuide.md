---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# Scoutly User Guide

Scoutly is a **desktop application for managing recruitment candidates through typed commands**, with a graphical interface for viewing your candidate pipeline. You can store candidate details and skills, add recruitment notes, and track hiring stages from application to decision.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [Scoutly releases](https://github.com/AY2627S1-CS2103T-T09-3/tp/releases).
   Choose `addressbook-mac-arm64.jar` for an Apple Silicon Mac (M-series chip), or `addressbook.jar` for
   an Intel Mac, Windows, or Linux. On Mac, check **Apple menu → About This Mac** to identify your chip.

1. Copy the file to a writable folder you want to use as the _home folder_ for Scoutly.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   **Apple Silicon Mac:** Run `java -jar addressbook-mac-arm64.jar` using an ARM64 Java 25 installation.<br>
   Launch Scoutly from a folder where you have permission to create and update files. If double-clicking the JAR does not work, use the terminal command above.
   The application window should appear in a few seconds. On first launch, it contains sample candidates.
   Use the command box to enter commands, the result area to read feedback, and the candidate list to find displayed indexes.

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all candidates.

   * `add n/John Doe p/98765432 s/Java s/SQL` : Adds a candidate named `John Doe` with two skills.

   * `delete 3` : Deletes the 3rd candidate shown in the current list.

   * `clear` : Deletes all candidates.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

### Using the workspace

The five dashboard tiles count **all stored candidates** by their current hiring stage, even during
a search. The displayed count shows how many candidates are in the current results. The tiles
update after additions, edits, stage changes, and deletions. They show current stages, not a history
of transitions.

Candidate cards show their current command index, stage, and up to three skills. Open the profile
for the complete skill list, contact information and follow-up details. An empty pipeline and a search with no matches
have different prompts; use `list` to reset a search.

### Command assistance and keyboard shortcuts

| Key | Action |
| --- | --- |
| Up / Down | Recall older/newer commands when suggestions are hidden. Returning past the newest entry restores your unfinished draft. |
| Up / Down with suggestions open | Select a completion. |
| Ctrl+Up / Ctrl+Down | Recall history even while suggestions are visible. |
| Tab | Accept the selected command-name or hiring-stage completion. Existing arguments are preserved. |
| Escape | Dismiss suggestions until the input changes. |
| Ctrl+L (Windows/Linux), Command+L (Mac) | Focus the command box while preserving its text and caret. |
| F1 | Open or restore the offline command reference. |

Syntax help appears below the input for recognized command names. Suggestions do not execute commands;
press **Enter** to submit. Failed commands remain available for correction. History stores up to
200 attempted commands for the current session, omits consecutive duplicates, and resets when Scoutly closes.
Success and failure feedback use both text labels and different colours.

### Opening Scoutly more than once

Only one Scoutly instance can use a home folder at a time. If you launch another instance from the same folder, it shows `Scoutly is already running with this data folder. Please use the existing window.` and closes after you dismiss the message. Use the window that is already open.

Before upgrading, close any windows opened by older versions of Scoutly.

Closing Scoutly releases the folder for the next launch. If Scoutly is forcibly terminated, the operating system releases the lock automatically; no manual cleanup is needed. Instances using separate home folders can run independently.

### Try a recruitment workflow

1. Add a candidate: `add n/Jordan Lee p/91234567 s/Java s/SQL`.
1. Run `list` to show all candidates and note Jordan Lee's displayed index.
1. Run `view INDEX`, replacing `INDEX` with that number, to inspect the candidate's profile.
1. Run `remark INDEX r/Available for interviews next week` to record a recruitment note.
1. Run `stage INDEX s/Interview` to update the hiring stage.

Use the candidate's current displayed index each time. Commands such as `find` change the displayed list,
so the same candidate may have a different index afterward. If the example candidate already exists,
use their displayed index or add a candidate with a different name or phone number.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `remark INDEX [r/REMARK]` can be used as `remark 1 r/Available immediately` or as `remark 1`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/shortlisted` or `t/shortlisted t/referral`.

* Command words are case-sensitive. Separate the command and prefixed fields with ordinary spaces.<br>

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for `help`, `exit`, and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`. The `list` command accepts either the optional `s/STAGE` or `sort/name` parameter, but not both, and the `summary` command rejects extra parameters; use it on its own.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens a searchable, offline command reference. Search by command name, description, or syntax,
then select a command to read its usage and examples. **Insert template** places a starter command
in the command box for you to complete. It never executes the command automatically.
You can also open the reference with **F1** or **Help → Command reference**.
Opening help again restores it if it was minimized.

You can also open the [Scoutly user guide](https://AY2627S1-CS2103T-T09-3.github.io/tp/UserGuide.html) directly.

Format: `help`


### Adding a candidate: `add`

Adds a candidate to the pipeline. New candidates start in the **Applied** hiring stage and have empty email,
address, remark, and tags.

Use `stage INDEX s/STAGE` after adding a candidate to change its hiring stage.

Format: `add n/NAME p/PHONE s/SKILL [s/SKILL]...`

* `n/NAME` and `p/PHONE` are required exactly once.
* At least one `s/SKILL` is required. Multiple skills may be provided.
* Names must contain 1–100 characters and start with a letter or number. Remaining characters may be letters, numbers, spaces, hyphens, or apostrophes. Leading/trailing whitespace is
  removed and repeated spaces are normalized.
* Phone numbers must contain 7–15 digits and may have one leading `+`.
* Skills must contain 1–30 alphanumeric characters with no spaces.
* Duplicate skills are removed case-insensitively, preserving the first spelling.
* A candidate is a duplicate when both the normalized name (ignoring case) and phone number match. Candidates with
  the same name but different phone numbers, or different names but the same phone number, are allowed.

Successful additions use the message `New candidate added: NAME | PHONE | Skills: SKILL1, SKILL2`.
* Invalid names report `Invalid name: names must start with a letter or number, may only contain letters, numbers, spaces, hyphens, and apostrophes, and must be 1-100 characters long.`
* Invalid phone numbers report `Invalid phone number: must be 7-15 digits, optionally starting with +.`
* Invalid skills report `Invalid skill: skills must be 1-30 alphanumeric characters with no spaces.`
* Missing skills report `At least one skill (s/) must be provided.`

Duplicate candidates report `This candidate already exists in your pipeline (same name and phone number).`

Examples:
* `add n/John Doe p/98765432 s/Java s/SQL`
* `add n/Betsy Crowe p/91234567 s/Python`

### Listing candidates: `list`

Shows candidates currently stored in Scoutly and reports the number of candidates listed. You can optionally filter the list by hiring stage. If there are no candidates, Scoutly displays `No candidates in your pipeline yet. Use 'add' to add one.`

Format: `list`, `list s/STAGE`, or `list sort/name`

* `STAGE` must be **Applied**, **Screened**, **Interview**, **Offered**, or **Rejected**. Stage names are case-insensitive.
* `list` displays all candidates.
* `list s/Interview` displays only candidates currently at the Interview stage.
* If no candidates match the selected stage, Scoutly displays `No candidates found at the Interview stage.`
* Invalid parameters show either the command usage instructions or the allowed stage values.

Use `list sort/name` to display candidates in ascending alphabetical order by name. Name sorting is case-insensitive.
The plain `list` command restores the stored candidate order, which follows insertion order; it clears any active search, stage filter, or alphabetical sort. Displayed indexes can change. Recheck the candidate’s index before viewing, editing, or deleting.

The only supported sorting parameter is `sort/name`; sorting cannot be combined with a hiring-stage filter. Use either
`list sort/name` or `list s/STAGE`.

Examples:
* `list` displays all candidates in stored order.
* `list sort/name` displays all candidates alphabetically by name.

### Summarizing the hiring pipeline: `summary`

Shows the number of candidates at every hiring stage and the total number of stored candidates.

Format: `summary`

* Always counts **all stored candidates**, including candidates hidden by `find` or another filter.
* Shows Applied, Screened, Interview, Offered, and Rejected in that order, including stages with zero candidates.
* Leaves candidate data, displayed order, and the current filter unchanged.
* Recalculates the counts each time, reflecting additions, deletions, and stage changes.
* Takes no parameters; extra arguments show usage instructions.
* The summary appears in the command result area. Scroll within that area to read the full result.

For example:

```text
Hiring-stage summary — all candidates
Applied: 8
Screened: 5
Interview: 3
Offered: 2
Rejected: 4
Total: 22
```

An empty pipeline shows zero for every stage and the total. Running `summary` after `find Jordan` still summarizes the entire pipeline while keeping Jordan's search results visible.

### Viewing a candidate profile: `view`

Opens the candidate's name, phone, email, address, tags, remark, current hiring stage, and skills
in the **Candidate profile** panel. Scroll within the panel to read long profiles. Tags and skills
are shown in alphabetical order. Missing tags or skills appear as `None`; empty email, address,
or remark fields appear as `Not provided`. Copy buttons let you copy available contact details.
The profile also displays the scheduled follow-up date, status and note, or `No follow-up scheduled`.

You can also select a candidate in the list using the mouse or keyboard. The profile refreshes
after edits. Deleting the candidate or hiding them through a search clears the profile;
a failed save restores the previous profile and candidate data.

Format: `view INDEX` or `view FULL_NAME`

* `INDEX` must be a positive integer referring to the currently displayed list.
* Viewing a profile leaves the candidate data and the displayed list unchanged.
* `FULL_NAME` must match the full name of exactly one candidate in the displayed list, ignoring case.
  Leading/trailing whitespace is removed and repeated internal whitespace is treated as one space,
  consistently with candidate identity.
* Name lookup follows the same rules as `delete FULL_NAME`. Partial names do not match.
* All-digit input is treated as an index. Use the displayed index to view a candidate whose full name is numeric.
* A missing or malformed argument shows the command usage. An index outside the displayed list shows an error.
* If no displayed candidate matches the name, Scoutly suggests using `list` to show all candidates.
* If multiple displayed candidates match the name, the error lists their displayed indices, names, and phone numbers. Use `view INDEX` with one of those indices to choose the intended candidate.

Examples:
* `list` followed by `view 2` displays the second candidate's profile.
* `find Betsy` followed by `view 1` displays the first candidate in the search results.
* `view alice pauline` displays Alice Pauline's profile if she is in the displayed list.

### Editing a candidate: `edit`

Edits an existing candidate in your pipeline.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SKILL]...`

* Edits the candidate at the specified `INDEX`. The index refers to the index number shown in the displayed candidate list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values. After a successful edit, all candidates are displayed in stored order; recheck indexes before the next command.
* Editing candidate details or tags preserves the candidate's skills, remark, and hiring stage. Use `remark` or `stage` to change the remark or stage.
* If one or more `s/SKILL` fields are provided, they replace all existing skills. Skills must be 1–30 alphanumeric characters; duplicate skills are removed case-insensitively while preserving the first spelling. If no `s/` field is provided, existing skills are preserved.
* Successful edits use candidate-oriented feedback; when skills are changed, the resulting skills are listed in deterministic alphabetical order.
* Every `s/` prefix must be followed by a valid skill. A bare `s/` is rejected.
* When editing tags, all of the candidate's existing tags are removed; adding tags is not cumulative.
* To remove all of a candidate's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st candidate to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd candidate to be `Betsy Crower` and clears all existing tags.

### Finding candidates by name: `find`

Finds candidates whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Candidates matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.
* A successful search reports the number of matching candidates using candidate terminology.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh` and `David Li` if those candidates are in your pipeline.

### Finding candidates by skill: `findskill`

Finds candidates by their skills.

Formats: `findskill SKILL`, `findskill any s/SKILL [s/SKILL]...`, or
`findskill all s/SKILL [s/SKILL]...`

* A single-skill search is case-insensitive and matches the complete skill name; for example, `findskill java`
  matches `Java`, but `findskill SQL` does not match `NoSQL`.
* `any` returns candidates with at least one of the specified skills.
* `all` returns candidates with every specified skill.
* The skill must contain 1–30 alphanumeric characters with no spaces.
* Skills supplied with `any` or `all` are deduplicated case-insensitively, preserving the first spelling.
* If no candidates match, Scoutly displays `No candidates found matching skill search: SKILL(S).`
* The displayed indexes are renumbered after filtering and can be used by subsequent commands.

Examples:
* `findskill Java` returns candidates with the `Java` skill.
* `findskill any s/Java s/Python` returns candidates with Java or Python.
* `findskill all s/Java s/SQL` returns candidates with both Java and SQL.

### Adding or removing a remark: `remark`

Adds or replaces the remark for a candidate in the currently displayed list.

Format: `remark INDEX [r/REMARK]`

* `INDEX` must be a positive integer referring to the displayed list.
* Example: `remark 2 r/Available for interviews next week`.
* Use `remark 2 r/` (or `remark 2`) to remove the remark.
* Remarks are shown on candidate cards and saved with your candidates. Editing other details preserves the remark.
* After updating a remark, all candidates are displayed again in stored order. Recheck displayed indexes before the next command.
* Use at most one `r/` prefix. Repeated `r/` prefixes are rejected and leave the existing remark unchanged.
* Successful updates report `Added remark to candidate: ...` or `Removed remark from candidate: ...`.

### Managing candidate follow-ups: `followup`

Schedules one follow-up per candidate, with an optional note. The candidate card shows
the date, note and whether it is overdue, due today, upcoming or completed.

Format: `followup INDEX d/YYYY-MM-DD [r/NOTE]`

* `INDEX` refers to the currently displayed list and must be a positive integer.
* Dates must be valid calendar dates from `0001-01-01` to `9999-12-31`, using exactly `YYYY-MM-DD`.
* Past dates are accepted, allowing recruiters to record an overdue task.
* Notes are optional, limited to 200 characters, and cannot contain control characters or line breaks.
* `d/` and `r/` may appear in either order. Repeating either prefix is rejected.
* Scheduling again replaces the previous date and note, and marks the follow-up as pending.
  Omitting `r/` clears the previous follow-up note. This note is separate from the candidate's remark.
* `followup INDEX done` marks the follow-up completed while keeping its date and note.
  It fails if no follow-up exists or it is already completed.
* `followup INDEX clear` removes the follow-up, including its note and completion state.
* A successful update displays all candidates again. Editing candidate details or remarks preserves the follow-up.
* Follow-ups are saved with candidate data and restored on the next launch. Older data files remain supported.

Examples:

* `followup 1 d/2026-11-05 r/Call about interview availability`
* `followup 1 done`
* `followup 1 clear`

### Listing candidate follow-ups: `followups`

Format: `followups [pending|overdue|today|upcoming|completed|all]`

View | Candidates displayed
-----|---------------------
`pending` | All unfinished follow-ups; this is the default when the view is omitted.
`overdue` | Unfinished follow-ups dated before today.
`today` | Unfinished follow-ups dated today.
`upcoming` | Unfinished follow-ups dated after today.
`completed` | Completed follow-ups, regardless of date.
`all` | All candidates with a follow-up, including completed ones.

* Every view excludes candidates without a follow-up.
* Views search the entire candidate list, including candidates hidden by a previous `find`.
* View names are case-insensitive. Only one view may be supplied.
* Results retain the usual candidate order and display new indices for use with `followup`, `edit` and `delete`.
* The result message includes the number of matching candidates and counts overdue and due-today tasks within that view.
* Dates use your computer's local calendar day. Run the command again after midnight to refresh the view.
* These are in-app reminders; the application does not send notifications while closed.
* Use `list` to display all candidates again.

Examples:

* `followups` shows all pending follow-ups.
* `followups overdue` shows unfinished follow-ups whose dates have passed.
* `followups today` shows the candidates to contact today.

### Updating a candidate's hiring stage: `stage`

Sets the hiring stage of a candidate in the currently displayed list.

Format: `stage INDEX s/STAGE`

* `INDEX` must be a positive integer from the displayed list. After `find`, it refers to the search results.
* The `s/` prefix is required and may appear only once. `STAGE` must be **Applied**, **Screened**, **Interview**, **Offered**, or **Rejected**. Stage names are case-insensitive.
* Any stage can be changed to any other stage, including going back to correct an earlier update. Setting the same stage again is allowed and reports `[Name] is already at [Stage].` without changing the candidate.
* The command preserves the current search filter and all other candidate details.
* The stage appears on the candidate's card and in `view INDEX`, and is saved automatically across restarts.
* Candidate cards show a labelled stage badge: Applied in grey, Screened in blue, Interview in purple, Offered in green, and Rejected in red. The stage name is always shown, so you do not need to rely on colour. Badges reflect the current stage after an update.
* New candidates and older saved records without a stage default to **Applied**. Editing details or remarks preserves the stage.
* Missing arguments or an invalid index format show usage instructions. An empty or unknown stage shows the allowed values; an index outside the displayed list shows an error. Failed commands do not change the candidate's stage.

Examples:
* `stage 1 s/Interview` moves the first displayed candidate to Interview.
* Running it again for Alex Yeoh already at Interview displays `Alex Yeoh is already at Interview.`
* `find Betsy` followed by `stage 1 s/screened` moves the first search result to Screened, keeping the search results visible.
* `stage 1 s/Applied` resets that candidate's stage to Applied.

### Deleting candidates: `delete`

Permanently deletes one candidate by index or full name, or several candidates by their displayed indexes.

Format: `delete INDEX`, `delete INDEX,INDEX[,INDEX]...`, or `delete FULL_NAME`

* All formats select candidates from the **currently displayed list**. After a `find` or `findskill` command, only candidates in its results can be deleted. Use `list` first to select from all candidates.
* `INDEX` refers to the index number shown in the displayed list and **must be a positive integer** 1, 2, 3, ...
* For batch deletion, separate indexes with commas, for example `delete 1,3,5`. Spaces around commas are allowed,
  and indexes may be supplied in any order.
* All indexes refer to the list **before the command runs**. For example, `delete 1,3` deletes the original first
  and third candidates, even though the displayed indexes change after deletion.
* Every index is checked before deletion. Duplicate indexes, empty entries, non-integers, non-positive values,
  values larger than 2147483647, or indexes outside the displayed list cause the entire command to fail:
  **no candidates are deleted**. For example, `delete 1,1`, `delete 1,`, and `delete 1,999` (when fewer than 999
  candidates are displayed) all fail.
* Batch deletion accepts indexes only; do not mix names and indexes or use ranges such as `delete 1-3`.
  Names containing spaces, including numeric names such as `1 3`, retain full-name lookup behavior; use commas
  to select multiple indexes.
* Successful batch deletion reports the number of deleted candidates and their details in the order of the
  supplied indexes. Candidates hidden by the current filter remain stored, and the remaining displayed
  candidates retain their order and filter.
* `FULL_NAME` must match the candidate's entire name, **ignoring case**. Partial names do not match. Do not put quotation marks around the name.
* Leading/trailing whitespace is ignored and repeated internal whitespace is treated as one space,
  consistently with candidate identity.
* If no displayed candidate matches the name, no record is deleted and an error is shown.
* If multiple displayed candidates match the name, no record is deleted. The error lists each matching candidate's displayed index, name, and phone number. Use `delete INDEX` with one of those indexes to choose the intended candidate.
* Input containing only digits is always treated as an index. To delete a candidate whose name consists only of digits, use their displayed index.
* Deletion removes the record completely; it does not archive the candidate. There is no undo command. Back up your data before deleting records you may need later.
* Deleting one candidate reports `Deleted candidate: ...`; batch deletion reports the number of deleted candidates.

Examples:
* `list` followed by `delete 2` deletes the 2nd candidate in the candidate pipeline.
* `find Betsy` followed by `delete 1` deletes the 1st candidate in the results of the `find` command.
* `delete 1,3,5` deletes the original 1st, 3rd, and 5th displayed candidates.
* `findskill Java` followed by `delete 3, 1` deletes the 3rd and 1st displayed Java candidates, provided at least
  three matches are displayed. Other candidates are preserved.
* `delete Alice Pauline` deletes the candidate named Alice Pauline if exactly one displayed candidate matches.
* `delete alice pauline` matches the same full name, ignoring case.

For example, if two displayed candidates match `delete Alice Pauline`, the feedback could be:

```text
Multiple candidates match "Alice Pauline":
1. Alice Pauline — Phone: 91234567
3. ALICE PAULINE — Phone: 81234567
Use delete INDEX to choose a candidate.
```

The numbers refer to positions in the currently displayed list, including after `find`; they are not a separate numbering of the matches. In this example, `delete 3` deletes the candidate with phone number `81234567`.

### Clearing all entries: `clear`

Clears all entries from the candidate pipeline.

Format: `clear`

This removes every candidate, including candidates hidden by the current filter. There is no undo command. Back up your data first if you may need it later.

The success message is `Candidate pipeline has been cleared!`.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

Scoutly automatically saves candidate data after each successful command that changes it. You do not need to save manually. Commands that only display information, change the displayed list, or exit do not rewrite the candidate file. Setting an existing value again also leaves the file unchanged.

### Editing the data file

Scoutly saves candidate data in `data/addressbook.json`, relative to the folder from which you launch the application.
If you follow the quick-start instructions, this is `[JAR file location]/data/addressbook.json`.
Advanced users can edit this file while Scoutly is closed. Back up the file first.

<box type="warning" seamless>

**Caution:**
If the data file cannot be loaded, Scoutly aborts startup to prevent overwriting it. The existing file remains unchanged.
Correct the invalid data or restore a valid backup before launching Scoutly again.<br>
Furthermore, certain edits can cause Scoutly to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install Scoutly on the other computer. Close Scoutly on both computers, then copy `data/addressbook.json`
from the old home folder into the new home folder's `data` folder. Back up any existing destination file before replacing it.
Launch Scoutly from the new home folder to load your candidates.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE s/SKILL [s/SKILL]...` <br> e.g., `add n/James Ho p/22224444 s/Java s/SQL`
**Clear**  | `clear`
**Delete** | `delete INDEX`, `delete INDEX,INDEX[,INDEX]...`, or `delete FULL_NAME`<br> e.g., `delete 3`, `delete 1,3,5`, or `delete Alice Pauline`
**Remark** | `remark INDEX [r/REMARK]`<br> e.g., `remark 1 r/Available immediately`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [s/SKILL]...`<br> e.g., `edit 2 n/James Lee s/Java s/SQL`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Follow-up** | `followup INDEX d/YYYY-MM-DD [r/NOTE]`, `followup INDEX done`, `followup INDEX clear`
**Follow-ups** | `followups [VIEW]`<br> e.g., `followups overdue`
**Find skill** | `findskill SKILL`, `findskill any s/SKILL [s/SKILL]...`, or `findskill all s/SKILL [s/SKILL]...`<br> e.g., `findskill any s/Java s/Python`
**List**   | `list`, `list s/STAGE`, or `list sort/name`<br>e.g., `list s/Interview` or `list sort/name`
**Summary** | `summary`
**View**   | `view INDEX` or `view FULL_NAME`<br> e.g., `view 2` or `view Alice Pauline`
**Stage**  | `stage INDEX s/STAGE`<br> e.g., `stage 2 s/Interview`
**Help**   | `help`
**Exit**   | `exit`

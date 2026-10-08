---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

New candidates start in the **Applied** hiring stage. Use `stage INDEX s/STAGE` after adding a candidate to change it.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... `

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
</box>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all candidates: `list`

Shows all candidates currently stored in Scoutly and reports the number of candidates listed.

Format: `list`

### Viewing a candidate profile: `view`

Displays a candidate's name, phone, email, address, tags, remark, and hiring stage in the command result area.
Scroll within the result area to read the full profile. Tags are shown in alphabetical order;
missing tags or an empty remark are shown as `None`.

Format: `view INDEX`

* `INDEX` must be a positive integer referring to the currently displayed list.
* Viewing a profile leaves the candidate data and the displayed list unchanged.
* A missing or malformed index shows the command usage. An index outside the displayed list shows an error.

Examples:
* `list` followed by `view 2` displays the second candidate's profile.
* `find Betsy` followed by `view 1` displays the first candidate in the search results.

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Editing contact details or tags preserves the candidate's hiring stage. Use `stage` to change it.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Adding or removing a remark: `remark`

Adds or replaces the remark for a person in the currently displayed list.

Format: `remark INDEX r/REMARK`

* `INDEX` must be a positive integer referring to the displayed list.
* Example: `remark 2 r/Likes baseball`.
* Use `remark 2 r/` (or `remark 2`) to remove the remark.
* Remarks are shown on person cards and saved with your contacts. Editing other details preserves the remark.
* After updating a remark, all contacts are displayed again.

### Updating a candidate's hiring stage: `stage`

Sets the hiring stage of a candidate in the currently displayed list.

Format: `stage INDEX s/STAGE`

* `INDEX` must be a positive integer from the displayed list. After `find`, it refers to the search results.
* The `s/` prefix is required and may appear only once. `STAGE` must be **Applied**, **Screened**, **Interview**, **Offered**, or **Rejected**. Stage names are case-insensitive.
* Any stage can be changed to any other stage, including going back to correct an earlier update. Setting the same stage again is allowed.
* The command preserves the current search filter and all other candidate details.
* The stage appears on the candidate's card and in `view INDEX`, and is saved automatically across restarts.
* New candidates and older saved records without a stage default to **Applied**. Editing details or remarks preserves the stage.
* Missing arguments or an invalid index format show usage instructions. An empty or unknown stage shows the allowed values; an index outside the displayed list shows an error. Failed commands do not change the candidate's stage.

Examples:
* `stage 1 s/Interview` moves the first displayed candidate to Interview.
* `find Betsy` followed by `stage 1 s/screened` moves the first search result to Screened, keeping the search results visible.
* `stage 1 s/Applied` resets that candidate's stage to Applied.

### Deleting a candidate: `delete`

Permanently deletes the specified candidate's record (represented as a person in the address book).

Format: `delete INDEX` or `delete FULL_NAME`

* Both formats select candidates from the **currently displayed list**. After a `find` command, only candidates in its results can be deleted. Use `list` first to select from all candidates.
* `INDEX` refers to the index number shown in the displayed list and **must be a positive integer** 1, 2, 3, ...
* `FULL_NAME` must match the candidate's entire name, **ignoring case**. Partial names do not match. Do not put quotation marks around the name.
* Leading and trailing spaces in the input are ignored; spaces within the name must match the stored name.
* If no displayed candidate matches the name, no record is deleted and an error is shown.
* If multiple displayed candidates match the name, no record is deleted. Use `delete INDEX` to choose the intended candidate.
* Input containing only digits is always treated as an index. To delete a candidate whose name consists only of digits, use their displayed index.
* Deletion removes the record completely; it does not archive the candidate.

Examples:
* `list` followed by `delete 2` deletes the 2nd candidate in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st candidate in the results of the `find` command.
* `delete Alice Pauline` deletes the candidate named Alice Pauline if exactly one displayed candidate matches.
* `delete alice pauline` matches the same full name, ignoring case.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear**  | `clear`
**Delete** | `delete INDEX` or `delete FULL_NAME`<br> e.g., `delete 3` or `delete Alice Pauline`
**Remark** | `remark INDEX [r/REMARK]`<br> e.g., `remark 1 r/Likes swimming`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**View**   | `view INDEX`<br> e.g., `view 2`
**Stage**  | `stage INDEX s/STAGE`<br> e.g., `stage 2 s/Interview`
**Help**   | `help`

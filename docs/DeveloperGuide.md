---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# Scoutly Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

Scoutly is adapted from [AddressBook Level 3 by SE-EDU](https://github.com/se-edu/addressbook-level3). It uses [JavaFX](https://openjfx.io/) for the desktop interface, [Jackson](https://github.com/FasterXML/jackson) for JSON persistence, [JUnit 5](https://junit.org/junit5/) for tests, and [MarkBind](https://markbind.org/) for this website.

**Notable uses of AI:**

* [@antaradaw](https://github.com/antaradaw) used OpenAI Codex extensively across their Scoutly contributions
  to generate and refine implementation code, write tests, investigate and fix bugs, and draft and revise
  project documentation, including work on candidate profiles and follow-up reminders.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

#### Candidate workspace state

`Logic#getAllPersons` exposes the address book's read-only observable source list. `PipelineDashboard`
observes that list for global hiring-stage totals, and observes `getFilteredPersonList` separately for
the displayed count. `PipelineSummary` computes immutable counts for all five stages.

`ViewCommand` resolves its target through the existing index/name lookup and returns it in
`CommandResult#getProfileTarget`. `MainWindow` uses this structured target to open the profile; it does
not parse feedback text. `CandidateSelectionModel` tracks immutable replacements using source-list
replacement positions, including name/phone edits. After each command, the window reconciles visibility
and restores the list highlight. User list gestures update profile selection; automatic selection
changes during deletion do not select a different profile. A failed command restores the prior profile
when the rolled-back candidate remains visible, including failed delete and clear operations.

The list displays the model's filtered and sorted order directly, keeping command indexes consistent
with `list sort/name`, stage filters and follow-up filters. Follow-up reminders appear on compact
cards and in the full profile, including completion state and notes. Profiles display only stored current stages;
there is no stage-history or job-role data in the model.

Bulk stage updates snapshot the selected `Person` targets and send one `stage INDEX[,INDEX]... s/STAGE`
command. `StageCommand` resolves every displayed index against a snapshot before modifying any candidate,
so a stage filter cannot remove an earlier row and retarget a later selection. Invalid stages, empty
selections, duplicate indexes, and out-of-range indexes are rejected before any candidate is changed.

The central `workspaceScroll` contains the dashboard, toolbar and candidate/profile split pane. Header
placeholders use their preferred heights, while a minimum split-pane height keeps profiles usable.
When the window or command suggestions leave less room, the central viewport scrolls instead of
shrinking header containers below their content. Bulk and profile action rows use wrapping flow panes.
Command entry, feedback and the status bar stay outside the scrolling area. Layout regression tests
check section bounds and scrolling at default and minimum window sizes, with suggestions open.

#### Command workspace state

`CommandHistory` maintains a bounded session history and an independent unfinished draft.
`CommandSuggestionProvider` computes token-range replacements without invoking command execution.
`CommandSuggestion` preserves input outside that range, including arguments after the caret.
`CommandBox` retains invalid input and routes every submitted command through its existing executor.
`CommandAssistPanel` handles suggestion presentation and contextual syntax.

`CommandReference` is the shared immutable catalogue for suggestions, syntax help, and searchable
`HelpWindow` entries. It reuses command `MESSAGE_USAGE` constants where available. Templates are
inserted through a callback; only Enter executes them. Feedback uses an error pseudo-class alongside
explicit Success/Error text. JavaFX focus changes preserve the input's anchor and caret.

JavaFX regression tests share `FxTestUtil` so the toolkit starts once per test worker. `MainWindowTest`
exercises real command execution, save rollback, and layout integration, and writes a rendered workspace
preview to `build/reports/ui/workspace.png`.

Candidate cards display the hiring stage as a labelled badge. `PersonCard` derives a stage-specific CSS class from the `HiringStage` enum name using `Locale.ROOT`. `PersonListCard.fxml` keeps the badge at its content width, while `Workspace.css` defines its colours. The stage name remains visible so that users do not need to distinguish stages by colour alone. When a candidate is updated, `PersonListPanel` rebuilds the affected card with its current stage.

`PersonCardTest` loads the current workspace theme and checks that all five stage backgrounds are distinct, with text contrast of at least 4.5:1 in both normal and selected cards.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="700"/>

`ViewCommandParser` creates a `ViewCommand`, which extends `Command`. When viewing by full name,
`ViewCommand` delegates candidate matching to the shared `PersonLookup` helper.

The sequence diagram below illustrates successful parsing and command execution for `delete 1`.
Internal index resolution, validation failures, and result-object construction are omitted.
The shared data snapshot, save, and save-failure recovery are described under the
[Storage component](#storage-component).

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Successful parsing and execution of delete 1" />

`DeleteCommand` retrieves the currently displayed list and validates the index before selecting a `Person`.
It passes that candidate object to `Model#deletePerson`, rather than passing the index. An out-of-range
index raises a `CommandException` without deleting anything.

The following sequence diagram focuses on `EditCommand.execute` for a successful `edit 1 s/Java s/SQL`
command. Parsing, validation failures, feedback formatting, and persistence are omitted to keep the
candidate update clear.

<puml src="diagrams/EditCandidateSkillsSequenceDiagram.puml" alt="Successful skill edit through EditCommand and Model" />

Before execution, `EditCommandParser` tokenizes and validates every `s/` value. An empty or invalid skill
raises a `ParseException` before a command is constructed, without changing the model or saving data.

`EditCommand` validates the displayed index, selects the candidate, and replaces the complete skill set
while preserving all other fields, including the hiring stage and follow-up. An invalid displayed index
raises a `CommandException` without changing the model. A successful edit shows all candidates in stored
order and returns a `CommandResult`. `LogicManager` saves only if candidate data changed; a failed save
restores the original data, filter, and comparator before reporting an error, as described under the
[Storage component](#storage-component).

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to retrieve the displayed list and delete a person).
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T09-3/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

JSON loading requires exactly one root value; trailing whitespace is allowed, but additional values or malformed trailing content cause a loading error. Invalid candidate files abort startup and remain unchanged. `JsonUserPrefsStorage` also rejects null GUI settings before model initialization, so invalid preferences produce a warning and are replaced with defaults through the existing recovery path. Missing GUI settings retain their default values.

`MainApp.init` acquires a `DataFolderLock` before reading or writing candidate data or preferences. The lock uses `FileChannel.tryLock()` on `data/scoutly.lock` and remains held until shutdown. A competing instance skips initialization, displays a message, and exits without touching either JSON file. Initialization failure releases the lock immediately; shutdown releases it even if saving preferences fails. Process termination also releases the operating system lock. The lock file is retained, so its existence alone does not indicate that the folder is in use.

`DataFolderLock` resolves the real data-folder path and tracks active locks within the JVM. This prevents aliases from opening a second channel for an owned lock file: on some systems, closing any channel for that file can release the process's locks. The operating system lock additionally protects against separate JVMs.

`LogicManager` compares its pre-command `AddressBook` snapshot with the resulting data. It saves only when they differ. Read-only commands, changes to the displayed filter or sorting, and unchanged candidate updates therefore do not rewrite the file. Failed saves of changed data still restore the original candidate data, filter, and comparator.

JSON saves write the complete UTF-8 content to a temporary file in the destination directory, then atomically replace the saved file. A failed write or replacement preserves the previous file and removes the temporary file. If the filesystem does not support atomic replacement, saving reports an error instead of falling back to an unsafe overwrite. Existing POSIX file permissions are preserved, and read-only destinations are rejected.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Viewing candidates with matching names

`ViewCommand` resolves full names using `PersonLookup.findByName`. For ambiguous names, its callback formats only the matching candidates with their one-based indices in the current displayed list, names, and phone numbers. Phone numbers distinguish candidates even when optional email fields are empty. The command leaves candidate data and the displayed list unchanged, so the user can select a match with `view INDEX` immediately.

### Viewing a candidate profile

`view INDEX` selects a candidate by position in the currently displayed list. `view FULL_NAME` instead
uses `PersonLookup` to match normalized full names, ignoring case, within that same list.
The following sequence diagram focuses on `ViewCommand.execute` and the three full-name lookup outcomes.
Command parsing, individual profile getters, feedback formatting, and result-object creation are omitted.

<puml src="diagrams/ViewProfileSequenceDiagram.puml" alt="Full-name candidate profile lookup, including missing and ambiguous matches" />

A missing or ambiguous name raises a `CommandException`. For an ambiguous name, the message lists the
matching candidates' displayed indices, names, and phone numbers, with guidance to use `view INDEX`.
A unique match produces a `CommandResult` containing formatted profile feedback and the candidate as
a structured profile target via `CommandResult.showProfile`. The UI opens that candidate's profile
without parsing the feedback text. The command leaves candidate data, the active filter, and displayed
order unchanged, so displayed indices remain suitable for a subsequent command.

### Hiring stages

Each `Person` stores a non-null `HiringStage` enum: `APPLIED`, `SCREENED`, `INTERVIEW`, `OFFERED`, or `REJECTED`. Constructors without an explicit stage default to `APPLIED`. Stage changes affect full person equality, while identity continues to depend on the normalized name and phone number.

`AddressBookParser` routes `stage INDEX s/STAGE` to `StageCommandParser`. The parser uses `ArgumentTokenizer` with `PREFIX_STAGE` (`s/`), validates the positive displayed index, rejects repeated stage prefixes, and parses a case-insensitive stage name. Missing arguments or a missing prefix and invalid indices produce usage errors; empty or unknown stages produce a list of allowed values.

`StageCommand` resolves the candidate using the current filtered list. If the requested stage is already current, it returns `[Name] is already at [Stage].` without replacing the candidate. Otherwise, it replaces that candidate with a new immutable `Person` through `Model#setPerson`. It preserves every other field and the current filter. All transitions, including backwards moves and setting the current stage again, are allowed so recruiters can correct mistakes. `EditCommand` and `RemarkCommand` carry forward the existing stage when replacing a person.

`PersonCard` displays a stage badge, and `ViewCommand` includes it in structured profile output. `JsonAdaptedPerson` persists the stage name in the `stage` JSON field. Missing or null stages in older data default to Applied; unrecognized or blank stored stages are rejected with an `IllegalValueException`. Existing fixtures without stages exercise backward compatibility, alongside new valid-stage and invalid-stage fixtures.

### Hiring-stage summary

`SummaryCommandParser` accepts `summary` without arguments; `AddressBookParser` routes it to `SummaryCommand`. Extra parameters are rejected with command usage instructions.

`SummaryCommand` reads `Model#getAddressBook().getPersonList()` so the summary includes all candidates, independently of the active filter. It counts stages in an `EnumMap` in one pass and formats every `HiringStage` in enum order, using zero for absent stages, followed by the total. Counts are recalculated on every invocation. The command does not replace candidates or modify the displayed list, filter, or order.
### Deleting a candidate by full name

`delete FULL_NAME` searches only the currently displayed candidates. `DeleteCommand` delegates the lookup
to the shared `PersonLookup` helper, which normalizes whitespace and compares entire names ignoring case.
A candidate hidden by the current filter cannot be selected by name.

The following sequence diagram focuses on `DeleteCommand.execute` after a full name has been parsed.
It keeps the unique, missing, and ambiguous lookup outcomes. Command parsing, ambiguity-message
formatting callbacks, result-object construction, and persistence are omitted.

<puml src="diagrams/DeleteByNameSequenceDiagram.puml" alt="Full-name deletion with unique, missing and ambiguous candidate matches" />

A missing name raises a `CommandException`. For multiple matches, `PersonLookup` invokes the ambiguity-message
callback supplied by `DeleteCommand`. The message includes each match's actual displayed index, stored name,
and phone number, and asks the user to choose with `delete INDEX`. Neither error path deletes a candidate or changes
the displayed list.

Only a unique match reaches `Model#deletePerson`, passing the resolved `Person`. The command returns a
`CommandResult` identifying the deleted candidate. Hidden candidates remain stored, and the active filter
and relative order of the remaining candidates are preserved. After successful command execution,
`LogicManager` saves the updated data; a failed save restores the original data, filter, and comparator
before reporting an error, as described under the [Storage component](#storage-component).

### Adding candidates and editing skills

`AddCommandParser` accepts one name, one phone number, and one or more skills. It rejects unknown prefixes and repeated name or phone fields. `Name` normalizes whitespace, requires a leading letter or number, and limits newly entered names to 100 characters. `Phone` accepts 7–15 digits with an optional leading `+`. `Skill` accepts 1–30 alphanumeric characters without spaces. Duplicate skills are removed case-insensitively, preserving the first spelling.

Candidate identity is the normalized, case-insensitive name together with the exact phone number. Matching only one field does not make a duplicate. New candidates have stage Applied and empty optional contact details, tags, and remark. `EditCommandParser` accepts skill replacements alongside contact fields. `EditCommand` preserves fields not supplied, including stage and remark. A supplied skill set replaces the previous set; a bare `s/` is rejected.

### Filtering and sorting

`find` matches full words in candidate names using OR semantics. `findskill` supports a complete single-skill match and `any` or `all` prefixed skill queries. Skill matching is case-insensitive. `list s/STAGE` uses a stage predicate. All these commands expose results through the same displayed list, so subsequent indexed commands act on those results.

`ModelManager` wraps the stored candidates in a `FilteredList` and then a `SortedList`. `list sort/name` applies a case-insensitive name comparator without changing stored order. Updating the filter clears this comparator. Plain `list` therefore shows all candidates in stored order; sorting is a display operation and is not persisted. `view`, `summary`, and successful deletion preserve the active display order and filter. Adding, editing, or updating a remark shows all candidates in stored order.

### Recruitment remarks

`RemarkCommandParser` accepts a displayed index and at most one `r/` field. Omitting the field or supplying an empty value removes the remark. Repeated prefixes produce a parse error before the model or storage is changed. `RemarkCommand` replaces the selected immutable `Person`, preserving every other field, and resets the displayed list to all candidates. Notes are persisted in the JSON `remark` field.

### Startup and saving data

`MainApp` reads preferences and `data/addressbook.json` relative to the launch directory. A missing candidate file initializes sample data. Invalid or unreadable candidate data aborts startup with an explanatory error, preserving the original file. These cases must be tested separately. Missing or unreadable preferences use defaults instead.

`LogicManager` parses and executes a command, then saves the candidate data through `StorageManager` only when the candidate data changes. Before execution it snapshots the data, active predicate, and comparator. If saving throws an `IOException`, it restores those values and reports the storage error. `JsonAdaptedPerson` supports legacy names and phone numbers already stored under the earlier constraints; this compatibility does not relax validation for newly entered names or phone numbers.

Undo, redo, and data archiving are not implemented. Deletion and clearing are permanent unless the user restores a backup.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* recruits candidates and tracks their progress through a hiring pipeline
* prefers typed commands with a desktop list and profile display
* needs to search candidates by name or skill and review hiring-stage totals
* works with candidate data saved locally and available offline

**Value proposition**: Scoutly helps recruiters record candidate details and skills, maintain recruitment notes, and track Applied, Screened, Interview, Offered, and Rejected stages using typed commands and automatic local saving.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`.

| Priority | As a … | I want to … | So that I can … |
|----------|--------|-------------|----------------|
| `* * *` | recruiter | add a candidate with a name, phone, and skills | record a new application |
| `* * *` | recruiter | list candidates and view their profiles | review candidate information |
| `* * *` | recruiter | edit candidate details and skills | keep records accurate |
| `* * *` | recruiter | find candidates by name or skill | locate relevant applicants |
| `* * *` | recruiter | update a candidate’s hiring stage | track progress and decisions |
| `* * *` | recruiter | add or remove a recruitment remark | retain interview and availability notes |
| `* * *` | recruiter | delete selected candidates or clear the pipeline | remove records I no longer need |
| `* * *` | recruiter | save changes automatically and reload them | continue work after restarting |
| `* * *` | new user | receive usage instructions and clear errors | correct commands without changing valid data |
| `* *` | recruiter | filter by stage and sort names | review a useful subset or order |
| `* *` | recruiter | summarize every hiring stage | assess the whole pipeline while keeping my current search |
| `* *` | recruiter | store optional email, address, and tags through edit | keep supporting details with the candidate |

### Use cases

(For all use cases below, the **System** is Scoutly and the **Actor** is the user, unless specified otherwise.)

**Use case: UC01, Add a candidate**

**MSS**

1. User enters `add n/NAME p/PHONE s/SKILL [s/SKILL]...`.
2. Scoutly validates the command and candidate fields.
3. Scoutly adds the candidate to the candidate pipeline and saves the updated data.
4. Scoutly displays a success message and the new candidate details.

    Use case ends.

**Extensions**

* 2a. One or more fields are missing or invalid.

    * 2a1. Scoutly displays an error message explaining the expected format.
    * 2a2. The use case ends without changing the candidate pipeline.

* 2b. A candidate with the same normalized name and phone number already exists.

    * 2b1. Scoutly displays a duplicate candidate error.
    * 2b2. The use case ends without changing the candidate pipeline.

**Use case: UC02, Find and inspect candidates**

**MSS**

1. User enters a `find` command with one or more name keywords.
2. Scoutly searches candidate names without regard to letter case.
3. Scoutly displays the matching candidates with their displayed indexes.
4. User enters `view INDEX` or `view FULL_NAME` to inspect a candidate in the displayed results.
5. Scoutly displays the candidate's profile, including hiring stage, without changing the data or search results.

    Use case ends.

**Extensions**

* 2a. No candidate name matches the keywords.

    * 2a1. Scoutly displays an empty result message.
    * 2a2. The use case ends.

* 1a. The command has no keywords or has an invalid format.

    * 1a1. Scoutly displays an error message.
    * 1a2. The use case ends without changing the candidate pipeline.

* 4a. The profile index or full name is invalid, missing, or outside the displayed results.

    * 4a1. Scoutly displays usage instructions or a lookup error.
    * 4a2. The use case resumes at step 4 without changing candidate data or the displayed list.

* 4b. More than one displayed candidate matches the full name, ignoring case.

    * 4b1. Scoutly lists the matching candidates with their displayed indices, names, and phone numbers, then asks the user to select a candidate using `view INDEX`.
    * 4b2. The use case resumes at step 4 without changing candidate data or the displayed list.

**Use case: UC03, Edit a candidate**

**MSS**

1. User enters `list` or `find` to display candidates.
2. Scoutly displays the candidates with their current indexes.
3. User enters an `edit` command with the index and replacement fields.
4. Scoutly validates the index and replacement fields.
5. Scoutly updates the selected candidate and saves the updated data.
6. Scoutly displays a success message with the updated candidate details.

    Use case ends.

**Extensions**

* 4a. The index is not a positive integer or is outside the displayed list.

    * 4a1. Scoutly displays an invalid index message.
    * 4a2. The use case resumes at step 3.

* 4b. A replacement field is invalid or would create a duplicate candidate with the same normalized name and phone number.

    * 4b1. Scoutly displays an error message.
    * 4b2. The use case ends without changing the selected candidate.

**Use case: UC04, Remove candidates**

**MSS**

1. User enters `list` or `find` to display candidates.
2. Scoutly displays the candidates with their current indexes.
3. User enters `delete` with the index of a candidate to remove.
4. Scoutly validates the index and removes the selected candidate.
5. Scoutly saves the updated data and displays the deleted candidate details.

    Use case ends.

**Extensions**

* 2a. The displayed list is empty.

    * 2a1. Scoutly displays an empty list.
    * 2a2. The use case ends.

* 4a. The index is invalid.

    * 4a1. Scoutly displays an invalid index message.
    * 4a2. The use case resumes at step 3.

* 3a. User enters `clear` instead of deleting one candidate.

    * 3a1. Scoutly removes all candidates and saves the empty candidate pipeline.
    * 3a2. The use case ends.

**Use case: UC05, Continue work after restarting**

**MSS**

1. User adds, edits, or deletes candidates.
2. Scoutly saves the changed candidate pipeline to its local JSON data file.
3. User exits Scoutly and starts it again later.
4. Scoutly loads the local data file.
5. Scoutly displays the saved candidates.

    Use case ends.

**Extensions**

* 2a. The data file cannot be written.

    * 2a1. Scoutly reports the storage error to the user.
    * 2a2. The use case ends with the in memory data unchanged.

* 4a. The candidate data file is missing.

    * 4a1. Scoutly initializes the sample candidates.
    * 4a2. The use case ends.

* 4b. The candidate data file is invalid or unreadable.

    * 4b1. Scoutly aborts startup and reports the loading problem.
    * 4b2. The existing file is preserved. The user must correct it or restore a valid backup before restarting.
    * 4b3. The use case ends.

**Use case: UC06, Update a hiring stage**

**MSS**

1. Recruiter lists or searches candidates and identifies the displayed index.
2. Recruiter enters `stage INDEX s/STAGE`.
3. Scoutly validates the index and stage, updates only that candidate, and saves the change.
4. Scoutly reports the updated stage and preserves the active filter and other candidate fields.

    Use case ends.

**Extensions**

* 3a. The index or stage is invalid, or the stage prefix is missing or repeated.
    * 3a1. Scoutly reports an error and leaves candidate data unchanged.
* 3b. The candidate is already at the requested stage.
    * 3b1. Scoutly reports that stage without changing the candidate.
* 4a. A stage-filtered candidate no longer matches after the update.
    * 4a1. The candidate disappears from those results and remains stored in the pipeline.

**Use case: UC07, Review candidates by skill and pipeline totals**

**MSS**

1. Recruiter enters `findskill all s/Java s/SQL`.
2. Scoutly displays only candidates with both skills and renumbers the displayed indexes.
3. Recruiter enters `view INDEX` to inspect one result.
4. Recruiter enters `summary`.
5. Scoutly reports stage counts and a total for all stored candidates while keeping the skill-search results visible.

    Use case ends.

**Extensions**

* 2a. No candidate matches the skills.
    * 2a1. Scoutly displays an empty result message. `summary` still counts all stored candidates.
* 1a. A skill is invalid or the query format is incorrect.
    * 1a1. Scoutly reports validation or usage instructions without changing the current results.

### Nonfunctional Requirements

1. Scoutly must run on Windows, Linux, and macOS with the Java version specified by the project setup, currently Java `25` or above.
2. Scoutly must respond to normal commands within 2 seconds for a contact list containing up to 1000 contacts on a supported computer.
3. Scoutly must save contact changes automatically after every successful command that changes data.
4. Scoutly must store contact data in a local JSON text file that a technically proficient user can read and edit.
5. Scoutly must preserve all valid contacts when saving and loading the local data file, and must report invalid data instead of silently treating it as valid.
6. Scoutly must support one local user at a time and must not require a network connection for normal contact management. At most one instance may use a given data folder at a time; separate home folders may be used independently.
7. A user who is comfortable typing regular English text must be able to complete the common add, edit, find, list, and delete tasks using commands without relying on mouse navigation.
8. Invalid commands, invalid candidate fields, duplicate candidates, and invalid indexes must produce a clear user visible error without changing the existing valid candidate data.
9. Scoutly must be distributable and launchable as a JAR without requiring a project specific installer.
10. Scoutly must provide the same command behavior through its documented CLI on every supported operating system.

### Glossary

* **Address**: The postal or location information stored as one field of a candidate.
* **Command**: A keyword and its arguments entered by the user to request one operation from Scoutly.
* **Candidate**: A recruitment record containing a name, phone number, skills, hiring stage, and optional email, address, remark, and tags.
* **Displayed index**: The positive number shown beside a candidate in the current displayed list and used by commands such as `edit` and `delete`.
* **Email**: The electronic mail address stored in a candidate record.
* **GUI**: The graphical user interface that displays candidates and command results.
* **JSON data file**: The local text file used by Scoutly to persist candidate data between launches.
* **Mainstream OS**: A commonly used desktop operating system such as Windows, Linux, Unix, or macOS.
* **Name keyword**: A word supplied to `find` for matching against candidate names without regard to letter case.
* **Parameter**: A value supplied to a command, such as an index, field value, or name keyword.
* **Phone**: The telephone number stored in a candidate record.
* **Tag**: An optional label attached to a candidate for grouping or identification.

--------------------------------------------------------------------------------------------------------------------

## Candidate follow-up reminders

`FollowUp` is an immutable value object stored on `Person`. It contains an optional
calendar date, a note of at most 200 characters and a completion flag. The empty state
cannot contain a note or be completed. Date validation requires an exact `YYYY-MM-DD`
format, a real calendar date and a year from 1 to 9999. Past dates are permitted.

`FollowUpCommandParser` parses `followup INDEX d/DATE [r/NOTE]`, `followup INDEX done`
and `followup INDEX clear`. Duplicate prefixes and mixed operations are rejected.
`FollowUpCommand` resolves the index against the displayed list, replaces the candidate
with `Person.withFollowUp`, and then restores the full list. Completing an absent or
already completed follow-up fails without changing data. Rescheduling replaces the note
and resets completion. Candidate edits and remark updates preserve the follow-up.

`FollowUpsCommandParser` parses an optional view, defaulting to pending.
`FollowUpFilter` supplies predicates for pending, overdue, today, upcoming, completed
and all follow-ups. Each predicate captures the command's local reference date so that
every candidate is evaluated against the same day. Listing always searches the entire
address book, retains its order and reports counts for the resulting view.

`JsonAdaptedFollowUp` validates stored fields before constructing the model object.
`JsonAdaptedPerson` defaults missing or null follow-up data to the empty state so older
files load successfully. A present follow-up object with missing fields, an invalid
date, an invalid note or inconsistent state is rejected. `PersonCard` displays the
date, status and note, and removes the label from layout when no follow-up exists.

Tests cover calendar boundaries, note validation, lifecycle transitions, filtered
indices, failed commands without mutations, all six views, preservation during other
edits, JSON round trips, older files, malformed stored objects and the JavaFX card label.
Run `./gradlew test checkstyleMain checkstyleTest` to verify the feature.

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Verify Java 25 with `java -version`, then run `java -jar scoutly.jar` (or `java -jar scoutly-mac-arm64.jar` on Apple Silicon).<br>
      Expected: The GUI opens with a set of sample candidates. The window size may not be optimal.

1. Recovering from null preferences

   1. In a disposable home folder with a valid candidate file, close Scoutly and replace the contents of `preferences.json` with the JSON literal `null` (without quotes). Keep a copy of `data/addressbook.json` for comparison.

   1. Launch the JAR from that folder.<br>
      Expected: Scoutly logs a warning and opens with default window preferences. Existing candidates remain visible, and the candidate file is unchanged. The preferences file is rewritten with valid default values.

   1. Close and relaunch the application.<br>
      Expected: Scoutly starts normally using the saved preferences.

   1. Close Scoutly, then replace the candidate file contents with `null` and launch again.<br>
      Expected: Startup aborts with a data-loading error to prevent overwriting invalid candidate data. The candidate file still contains `null`; restore the original file before continuing.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app using the same terminal command and launch folder.<br>
       Expected: The most recent window size and location are retained.

1. Preventing concurrent instances

   1. Launch Scoutly in a disposable home folder. Add `Maya Rao` using `add n/Maya Rao p/91234567 s/Java`.

   1. Launch the same JAR again from the same folder.<br>
      Expected: The second instance displays `Scoutly is already running with this data folder. Please use the existing window.` Dismissing the message closes that instance. The candidate file and preferences remain unchanged.

   1. In the original window, add `Leo Tan` using `add n/Leo Tan p/81234567 s/SQL`, then run `exit` and relaunch.<br>
      Expected: Both Maya Rao and Leo Tan remain stored, and the application opens normally.

   1. Repeat after forcibly terminating the original process, without deleting `data/scoutly.lock`.<br>
      Expected: The application reopens normally and preserves the previously saved candidates.

   1. Launch another copy from a different home folder.<br>
      Expected: Both instances can operate independently.

### Deleting a candidate

1. Deleting a candidate while all candidates are shown

   1. Prerequisites: List all candidates using the `list` command, with multiple candidates in the list.

   1. Test case: `delete 1`<br>
      Expected: The first candidate is deleted from the list. The status message shows the deleted candidate's details.

   1. Test case: `delete 0`<br>
      Expected: No candidate is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete -1`, and `delete 2147483648`. Also try an index larger than the displayed list size.<br>
      Expected: Similar to previous.

1. Deleting a candidate by full name

   1. Prerequisites: Use a test address book containing Alice Pauline and Benson Meier, with no other candidates matching those names when case is ignored. Run `list`. Restore these records before each independent test below.

   1. Test case: `delete alice pauline`<br>
      Expected: Alice Pauline's record is deleted, and the success message shows their details. Restart the app and confirm that the record remains deleted.

   1. Test case: `delete Alice`<br>
      Expected: No record is deleted. An error reports that no candidate with this full name exists in the displayed list.

   1. Test case: Run `find Benson`, then `delete Alice Pauline`.<br>
      Expected: No record is deleted because Alice Pauline is hidden. Run `list` to confirm the record still exists.

   1. Test case: Run `find Alice`, then `delete Alice Pauline`.<br>
      Expected: Alice Pauline is deleted from the filtered results and the address book. Run `list` to confirm Benson Meier remains.

1. Handling ambiguous names

   1. Prerequisites: Use a test address book containing Alice Pauline and ALICE PAULINE, with different phone numbers, leaving email empty. The current model requires different phone numbers for candidates whose normalized names match when case is ignored. Run `list` and note their displayed indexes.

   1. Test case: `delete Alice Pauline`<br>
      Expected: Neither record is deleted and the displayed list remains unchanged. The error lists both candidates' actual displayed indexes, names, and phone numbers, then asks the user to use `delete INDEX`. No unrelated candidates appear in the error.

   1. Test case: Use `delete INDEX`, replacing `INDEX` with one of the indexes shown in the error.<br>
      Expected: Only the selected candidate is deleted; the other matching candidate remains.

1. Handling ambiguous names in a filtered list

   1. Prerequisites: Use a test address book containing Alice Pauline, Alice Tan, and ALICE PAULINE in that order, with different phone numbers, leaving email empty. Other candidates may also be present. Run `find Alice` so the displayed list contains these three candidates.

   1. Test case: `delete alice pauline`<br>
      Expected: No record is deleted and the filter remains unchanged. The error lists Alice Pauline at index 1 and ALICE PAULINE at index 3, with their respective phone numbers. Alice Tan is not listed in the error. The second match must be numbered 3, matching its displayed position.

   1. Test case: `delete 3`<br>
      Expected: ALICE PAULINE is deleted. Alice Pauline and Alice Tan remain visible. Run `list` to confirm that candidates hidden by the filter remain stored.

### Viewing candidates with matching names

1. Prerequisites: In a test address book, run `add n/Amy Bee p/85355255 s/Java`, `add n/Amy Tan p/82345678 s/SQL`, and `add n/AMY BEE p/91234567 s/Python`. Ensure there are no other candidates whose names contain the word Amy. Run `list` and note each candidate's displayed index.
1. Test case: `view amy bee`<br>
   Expected: The error lists only Amy Bee and AMY BEE, with their actual displayed indices and phone numbers 85355255 and 91234567. It asks the user to use `view INDEX`. Candidate data and the displayed list remain unchanged. This also works when both candidates have no email or address.
1. Test case: Run `view INDEX` using the index shown for AMY BEE.<br>
   Expected: The profile contains AMY BEE and phone 91234567. Candidate data and the displayed list remain unchanged.
1. Test case: Run `find Amy`, then `view AMY BEE`.<br>
   Expected: The error shows Amy Bee at index 1 and AMY BEE at index 3, with their respective phone numbers. Amy Tan is absent from the error. The filter and list order remain unchanged.
1. Test case: `view 3`<br>
   Expected: The profile shows AMY BEE and phone 91234567. The filtered list still contains all three Amy candidates in the same order. Run `list` to confirm any candidates hidden by the filter remain stored.

### Hiring stages

1. Run `list`, then `stage 1 s/Interview` and `view 1`.<br>
   Expected: The first candidate's card and profile show Interview, and other details remain unchanged.
1. Run `stage 1 s/rejected`, `stage 1 s/Applied`, then `stage 1 s/Applied` again.<br>
   Expected: Case-insensitive names and backwards changes succeed. The repeated Applied command reports `[Name] is already at Applied.` and leaves the candidate unchanged.
1. Find a candidate by name, then run `stage 1 s/Offered`.<br>
   Expected: Only the first displayed search result changes; the search filter remains active.
1. Try `stage`, `stage 0 s/Interview`, `stage 1 s/Hired`, and an index larger than the displayed list.<br>
   Expected: Clear usage, stage, or index errors, with no candidate changes.
1. Change a stage, edit the candidate's phone, add a remark, then restart the app.<br>
   Expected: The stage remains unchanged through the edits and restart.
1. In a temporary copy of the data file, remove a person's `stage` property and restart.<br>
   Expected: That candidate defaults to Applied. Restore the original test data after testing.

### Hiring-stage summary

1. Start with an empty pipeline and run `summary`.<br>
   Expected: All five stages and the total are zero, in pipeline order.
1. Add candidates and use `stage INDEX s/STAGE` to assign different stages, then run `summary`.<br>
   Expected: Each count matches the stored candidates and the total equals their sum.
1. Run `find NAME` to hide some candidates, then run `summary`.<br>
   Expected: Counts still cover all stored candidates. The search results and their displayed order are unchanged, including when the search has no matches.
1. Add or delete a candidate, or change a stage, then run `summary` again.<br>
   Expected: Counts reflect the current data immediately.
1. Try `summary all` and `summary s/Interview`.<br>
   Expected: Usage instructions are shown; candidate data and the active filter are unchanged.

### Remarks and duplicate fields

1. In a disposable pipeline, run `add n/Alice p/91234567 s/Java`, then `remark 1 r/Keep this note`.
1. Run `remark 1 r/Contacted r/Interview tomorrow` and `remark 1 r/Contacted r/`.<br>
   Expected: Both commands reject the repeated `r/` field. `view 1` still shows `Keep this note`. Restart and confirm the note is preserved.
1. Run `remark 1`.<br>
   Expected: The note is removed. The candidate’s skills and hiring stage remain unchanged.

### Sorting and displayed indexes

1. In an empty disposable pipeline, add Zoe before Amy with different phone numbers and valid skills.
1. Run `list sort/name`.<br>
   Expected: Amy is first and Zoe is second. `view 1` identifies Amy.
1. Run `list`.<br>
   Expected: Stored order returns: Zoe is first and Amy is second. `view 1` now identifies Zoe.
1. Restart Scoutly from the same folder.<br>
   Expected: Both records remain stored; the alphabetical display sort is not persisted.

### Saving and loading data

Use a disposable app folder and back up its data file before each independent test. Close the app before modifying files.

1. Add a candidate, set its stage to Interview, add a remark, and edit its skills and optional contact details. Exit and relaunch from the same folder.<br>
   Expected: `view INDEX` shows all saved fields unchanged.
1. Move `data/addressbook.json` out of the disposable folder, then relaunch.<br>
   Expected: The sample candidates are initialized. This is not a loading error. Restore the backed-up file with the app closed.
1. Replace the disposable data file with `{invalid-json`, then relaunch.<br>
   Expected: Startup aborts with an error explaining that the file could not be loaded. The invalid file remains unchanged; the application does not continue with an empty pipeline. Restore the backup before relaunching.
1. Replace the data file with valid JSON containing an invalid stage, such as `Hired`, then relaunch.<br>
   Expected: Startup aborts and preserves the file. Correct the stage or restore the backup before relaunching.

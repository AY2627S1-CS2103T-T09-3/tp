---
  layout: default.md
  title: "Home"
---

# Scoutly

[![CI Status](https://github.com/AY2627S1-CS2103T-T09-3/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-T09-3/tp/actions/workflows/gradle.yml)

Scoutly is a desktop candidate pipeline and recruitment management application for recruiters who prefer typing commands. It combines a command line with a graphical interface to help recruiters manage candidate details and track progress through the hiring process.

## Current features

- Add candidates with their phone numbers and skills, and edit their details.
- Find candidates by name or skill; filter by hiring stage or sort alphabetically.
- View candidate profiles and record recruitment remarks.
- Track Applied, Screened, Interview, Offered, and Rejected stages.
- Summarize the full hiring pipeline, including candidates hidden by a search.
- Delete individual candidates or a batch of displayed indexes.
- Save candidate data locally and reload it on the next launch.

## Get started

Download the appropriate JAR from [Scoutly releases](https://github.com/AY2627S1-CS2103T-T09-3/tp/releases) and launch it with Java 25 from a writable folder. Apple Silicon Macs use `addressbook-mac-arm64.jar`; Windows, Linux, and Intel Macs use `addressbook.jar`.

Enter commands in the command box. Read feedback and candidate profiles in the result area, and use the numbered candidate list to select records. For example:

```text
add n/Jordan Lee p/91234567 s/Java s/SQL
find Jordan
view 1
stage 1 s/Interview
summary
```

Indexes refer to the currently displayed list. Check the index again after commands that change the filter or order. The User Guide explains each command and how to back up your data.

## Documentation

- [Project website](https://ay2627s1-cs2103t-t09-3.github.io/tp/)
- [User Guide](https://ay2627s1-cs2103t-t09-3.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-t09-3.github.io/tp/DeveloperGuide.html)
- [About Us](https://ay2627s1-cs2103t-t09-3.github.io/tp/AboutUs.html)

## Acknowledgements

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), and [JUnit5](https://github.com/junit-team/junit5).

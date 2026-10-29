# Scoutly

[![CI Status](https://github.com/AY2627S1-CS2103T-T09-3/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-T09-3/tp/actions/workflows/gradle.yml)

Scoutly is a desktop candidate pipeline and recruitment management application for recruiters who prefer typing commands. It combines a command line with a graphical interface to help recruiters manage candidate details and track progress through the hiring process.

![Planned Scoutly interface showing a candidate list, hiring stages, and candidate profile](docs/images/Ui.png)

*Original design reference. The implemented workspace now includes live stage totals, compact candidate cards, and a dedicated profile panel.*

## Current features

- Add, list, and delete candidates.
- View a candidate’s profile.
- Update a candidate’s hiring stage: Applied, Screened, Interview, Offered, or Rejected.
- View live hiring-stage totals alongside search result counts.
- Inspect candidate profiles in a scrollable panel with contact-copy actions.
- Recall command history and complete command names or hiring stages using the keyboard.
- Search the offline command reference and insert starter templates.
- Receive clear feedback for successful commands and invalid input.

Use `stage INDEX s/STAGE`, for example `stage 1 s/Interview`, to update a candidate's hiring stage. New candidates default to Applied; stages appear on candidate cards and in `view` output and are saved across restarts. Dashboard tiles count all stored candidates independently of searches.

## Documentation

- [Project website](https://ay2627s1-cs2103t-t09-3.github.io/tp/)
- [User Guide](https://ay2627s1-cs2103t-t09-3.github.io/tp/UserGuide.html)
- [Developer Guide](https://ay2627s1-cs2103t-t09-3.github.io/tp/DeveloperGuide.html)
- [About Us](https://ay2627s1-cs2103t-t09-3.github.io/tp/AboutUs.html)

## Acknowledgements

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

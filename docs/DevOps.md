---
  layout: default.md
  title: "DevOps guide"
  pageNav: 3
---

# DevOps guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Build automation

This project uses Gradle for **build automation and dependency management**. **We recommend reading [this Gradle tutorial from se-edu/guides](https://se-education.org/guides/tutorials/gradle.html).**


The following commands perform common Gradle tasks.


* **`clean`**: Deletes the files created during the previous build tasks (e.g. files in the `build` folder).<br>
  For example: `./gradlew clean`

* **`shadowJar`**: Uses the Shadow plugin to create the fat JAR file `build/libs/addressbook.jar`.<br>
  For example: `./gradlew shadowJar` (Windows, Linux, and Intel Mac).<br>
  For Apple Silicon Mac, use `./gradlew shadowJar -PjavafxMacPlatform=mac-aarch64` to create
  `build/libs/addressbook-mac-arm64.jar`. The default value of `javafxMacPlatform` is `mac`;
  only `mac` and `mac-aarch64` are accepted.

* **`run`**: Builds and runs the application.<br>
  **`runShadow`**: Builds the application as a fat JAR, then runs it.

* **`checkstyleMain`**: Runs the code style check for the main code base.<br>
  **`checkstyleTest`**: Runs the code style check for the test code base.

* **`test`**
  * `./gradlew test`: Runs all tests.
  * `./gradlew clean test`: Cleans the project before running all tests

--------------------------------------------------------------------------------------------------------------------

## Continuous integration (CI)

This project uses GitHub Actions for CI. The necessary workflow configuration files are in `.github/workflows`. No further setup is required.

### Code coverage

As part of CI, Gradle generates JaCoCo coverage reports from the tests, and CI uploads the coverage data to Codecov. Codecov then provides information about test coverage.

However, because Codecov is known to run into intermittent problems (e.g., report upload fails) due to issues on the Codecov service side, the CI is configured to pass even if the Codecov task failed. Therefore, developers are advised to check the code coverage levels periodically and take corrective actions if the coverage level falls below desired levels.

To enable Codecov for forks of this project, follow the steps given in [this se-edu guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository-wide checks

In addition to Gradle checks, CI runs repository-wide checks. Unlike Gradle checks, which cover files used in the build, these checks cover every repository file and enforce rules that are hard to apply on development machines, such as line-ending requirements.

These checks are implemented as POSIX shell scripts, and thus can only be run on POSIX-compliant operating systems such as macOS and Linux. To run all checks locally on these operating systems, execute the following in the repository root directory:

`./.github/run-checks.sh`

Any warnings or errors will be printed out to the console.

**If adding new checks:**

* Checks are implemented as executable `check-*` scripts within the `.github` directory. The `run-checks.sh` script will automatically pick up and run files named as such. That is, you can add more such files if you need and the CI will do the rest.

* Check scripts should print out errors in the format `SEVERITY:FILENAME:LINE: MESSAGE`
  * SEVERITY is either ERROR or WARN.
  * FILENAME is the path to the file relative to the current directory.
  * LINE is the line of the file where the error occurred and MESSAGE is the message explaining the error.

* Check scripts must exit with a non-zero exit code if any errors occur.

--------------------------------------------------------------------------------------------------------------------

## Making a release

Here are the steps to create a new release.

1. Update the version number in [`MainApp.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java).
1. Generate both fat JAR files, without running `clean` between the two commands:
   ```sh
   ./gradlew clean check shadowJar
   ./gradlew shadowJar -PjavafxMacPlatform=mac-aarch64
   ```
   This creates `build/libs/addressbook.jar` for Windows, Linux, and Intel Mac, and
   `build/libs/addressbook-mac-arm64.jar` for Apple Silicon Mac. The Mac native libraries have identical
   filenames across architectures and must not be combined into one fat JAR.
1. Launch each JAR on its target platform with Java 25. For Apple Silicon, verify
   `java -jar build/libs/addressbook-mac-arm64.jar` with a plain ARM64 JDK, without separately installed JavaFX.
   A JDK with JavaFX bundled can hide missing or incompatible native libraries in the JAR.
1. Tag the repo with the version number. e.g. `v0.1`
1. [Create a new release using GitHub](https://help.github.com/articles/creating-releases/). Upload both JAR files
   and explain which one Apple Silicon users should download.

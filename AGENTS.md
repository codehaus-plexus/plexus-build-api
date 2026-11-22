# Agent guide for plexus-build-api

`org.codehaus.plexus:plexus-build-api` lets Maven plugins report file changes, messages and
progress to whatever runs the build: IDEs use it for incremental builds (which files were touched),
markers pointing at a file and line, and notifications about updated files. The project has no
`src/site` pages; the site is generated from the POM and the Javadoc, and the README is the user
guide.

## Build and test

- CI calls the shared `codehaus-plexus/.github` `maven.yml@master` workflow, which runs
  `mvn verify javadoc:javadoc` on JDK 8, 21 and 25 and on Linux, Windows and macOS. A push to
  master also deploys a snapshot.
- Main code must use the Java 8 API (`javaVersion` 8 from the `plexus` parent).
- Unit tests use JUnit 5.
- `sisu-maven-plugin` builds the component index, and the JAR sets
  `Automatic-Module-Name: org.codehaus.plexus.build`; keep that name stable.
- Run `mvn spotless:apply` on JDK 17 to 21 before pushing; it also formats Markdown.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- Public API added or changed in `BuildContext`, `Messages` or `Progress`: its Javadoc, and the
  matching section of `README.md`, including the example code.
- The Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.


Plexus Build API
================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/codehaus-plexus/plexus-build-api.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-build-api.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.plexus/plexus-build-api)
[![Build Status](https://github.com/codehaus-plexus/plexus-build-api/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/codehaus-plexus/plexus-build-api/actions/workflows/maven.yml?query=branch%3Amaster)

This API lets IDEs integrate with Maven more deeply than the regular Maven plugin API allows. It requires
Java 8 or later. The [site](https://codehaus-plexus.github.io/plexus-build-api/) has the Javadoc,
and contributor notes are in [AGENTS.md](AGENTS.md).

It supports

- incremental builds e.g. allows to query which files have been touched since last build
- fine-grained error/info markers (referring to specific files in particular line numbers)
- notifications about updated files

Current Implementations
-----------------------

### Default Implementation

The default implementation shipping with this artifact is supposed to impose minimal overhead. It doesn't support incremental build and acts directly on the file system. Errors and warnings are just logged through SLF4J.

### M2Eclipse

[M2Eclipse](https://www.eclipse.org/m2e/) is using this API for supporting both incremental builds and fully integrated error markers in Eclipse. They maintain information for Mojo developers at [Making Maven Plugins Compatible](https://www.eclipse.org/m2e/documentation/m2e-making-maven-plugins-compat.html).
Currently only versions up to 0.0.7 (with old Maven coordinates `org.sonatype.plexus:plexus-build-api`) are supported, this limitation is tracked in [Issue 944](https://github.com/eclipse-m2e/m2e-core/issues/944).

History
-------

The project was relocated from <https://github.com/sonatype/sisu-build-api>. Also its Maven coordinates changed from `org.sonatype.plexus:plexus-build-api` to `org.codehaus.plexus:plexus-build-api`, the API is still the same, though.

## Provided APIs

### Messages API

The Messages API provides a modern, flexible way to create and manage build messages/markers that inform users in an IDE about issues in their files. It uses a builder pattern for constructing messages in a more convenient and extensible way compared to the legacy BuildContext message methods.

**Key Features:**
- Builder pattern for flexible message construction
- Clear separation of concerns from resource operations
- Support for error, warning, and info messages
- File path-based message management
- Optional line and column information
- Optional exception cause association

**Example Usage:**

```java
@Inject
private Messages messages;

public void execute() {
    // Create an error message
    messages.error(Paths.get("/path/to/file.java"))
        .line(42)
        .column(10)
        .cause(exception)
        .create("Syntax error");
    
    // Create a warning message
    messages.warning(Paths.get("/path/to/file.java"))
        .line(15)
        .create("Deprecated method used");
    
    // Clear messages for a specific file
    messages.clear(Paths.get("/path/to/file.java"));
    
    // Clear all messages
    messages.clearAll();
}
```

### BuildContext

`BuildContext` is the original API: it tells a mojo which files changed since the last build, and writes
output files so that an IDE learns about them.

```java
@Inject
private BuildContext buildContext;

public void execute() throws IOException {
    if (buildContext.hasDelta(sourceFile)) {
        try (OutputStream out = buildContext.newFileOutputStream(targetFile)) {
            // write the generated file
        }
    }

    // state kept between two executions of the same mojo in an incremental build
    List<String> previous = buildContext.getValue("extracted");
    buildContext.setValue("extracted", current);
}
```

`getValue` returns `null` when the build is not incremental, and its return type is inferred from the
assignment target: a stored value of another type fails with a `ClassCastException` at the call site.

### Progress

The API allows a mojo to report progress in a way that is suitable to be shown as a progressbar as well as check if the user wants the mojo to gracefully abort its current operation.
This can be useful for example when processing some files in a loop so the user can directly see the amount of progress and possibly ask to abort if it takes too long.

```java
@Inject
private Progress progress;

public void execute() {
    progress.startTask("Processing files", files.size());
    for (File file : files) {
        if (progress.isCancelRequested()) {
            return;
        }
        // process the file
        progress.worked(1);
    }
}
```

### IDE connection to the Maven process

This API is usually not used by mojos but for IDE integration: if enabled as a Maven core extension, plexus-build-api supplies a way to communicate with the running Maven build and get events.
The default implementation opens a TCP connection to a port specified by the system property `plexus.build.ipc.port` using key/value encoded message format. If no such value is given all messages are silently discarded.

## Resources API

`org.codehaus.plexus.build.resources.Resources` is a `Path`-based API for resource operations, separate from
`BuildContext`'s logging and scanner concerns. `DefaultResources` delegates to `BuildContext`, so it can be adopted
without waiting for the build tools that provide the new API.

Use `hasDelta(Path)` as a hint for user-editable sources. For an input and an output, prefer `isUptodate(Path, Path)`
and `newOutputStream(Path)`, which update the file only when the content changes. `copy(Path, Path)` combines both
and marks generated files through `markDerived(Path)`.

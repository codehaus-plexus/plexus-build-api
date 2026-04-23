Plexus Build API
================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/codehaus-plexus/plexus-build-api.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-build-api.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.plexus/plexus-build-api)
![Build Status](https://github.com/codehaus-plexus/plexus-build-api/actions/workflows/maven.yml/badge.svg)

This API allows IDEs to integrate with Maven deeper than it would be possible by just using regular Maven/Mojo API.

It supports

- incremental builds e.g. allows to query which files have been touched since last build
- fine-grained error/info markers (referring to specific files in particular line numbers)
- notifications about updated files

Current Implementations
-----------------------

### Default Implementation

The default implementation shipping with this artifact is supposed to impose minimal overhead. It doesn't support incremental build and acts directly on the file system. Errors and warning are just logged through SLF4J.

### M2Eclipse

[M2Eclipse](https://www.eclipse.org/m2e/) is using this API for supporting both incremental builds and fully integrated error markers in Eclipse. They maintain information for Mojo developers at [Making Maven Plugins Compatible](https://www.eclipse.org/m2e/documentation/m2e-making-maven-plugins-compat.html).
Currently only versions up to 0.0.7 (with old Maven coordinates `org.sonatype.plexus:plexus-build-api`) are supported, this limitation is tracked in [Issue 944](https://github.com/eclipse-m2e/m2e-core/issues/944).

History
-------

The project was relocated from <https://github.com/sonatype/sisu-build-api>. Also its Maven coordinates changed from `org.sonatype.plexus:plexus-build-api` to `org.codehaus.plexus:plexus-build-api`, the API is still the same, though.

## Provided APIs

### Resources API

The Resources API provides a modern, Path-based interface for managing build resources. It separates resource management concerns from logging/messaging functionality and provides better control over file operations during the build process.

**Key Features:**
- Modern `java.nio.file.Path` instead of `java.io.File`
- Change detection with `hasDelta()` (best effort hint)
- Reliable input/output freshness checking with `isUptodate()`
- Optimized output streams that only update files when content changes
- Support for marking generated/derived files for IDE integration
- Convenient copy operation that respects up-to-date checks
- Relative path conversion via `getPath()`

**Example Usage:**

```java
@Inject
private Resources resources;

public void execute() {
    // Convert relative paths to absolute paths
    Path source = resources.getPath("src/main/java/Example.java");
    Path target = resources.getPath("target/classes/Example.class");
    
    // Smart copy - only copies when target is stale
    resources.copy(source, target);
    
    // Check if file has changed (best effort hint)
    if (resources.hasDelta(source)) {
        // Process the file
    }
    
    // Reliable check for input/output scenarios
    if (!resources.isUptodate(target, source)) {
        // Regenerate target from source
    }
    
    // Write to a file with change detection
    Path generated = resources.getPath("target/generated/Proto.java");
    try (OutputStream out = resources.newOutputStream(generated, true)) {
        // Write content - file marked as derived for IDE warnings
        out.write(content);
    }
    
    // Mark a file as generated/derived
    resources.markDerived(generated);
}
```

**When to use `hasDelta()` vs `isUptodate()`:**
- Use `hasDelta()` as a hint for user-editable source files where you want to detect changes
- Use `isUptodate()` when there's a clear input/output relationship, as it handles cases where target files may be deleted or modified outside the build process

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

### Progress

The API allows a mojo to report progress in a way that is suitable to be shown as a progressbar as well as check if the user wants the mojo to gracefully abort its current operation.
This can be useful for example when processing some files in a loop so the user can directly see the amount of progress and possibly ask to abort if it takes to long.

### IDE connection to maven process

This API is usually not used by mojos but for IDE integration, if enabled as a maven-core extension plexus-build-api supply a way to communicate with the running maven build and get events.
The default implementation open a tcp connections to a port specified by the system property `plexus.build.ipc.port` using key/value encoded message format. If no such value is given all messages are silently discarded.

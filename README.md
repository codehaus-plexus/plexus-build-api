Plexus Build API
================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/codehaus-plexus/plexus-classworlds.svg?label=License)](http://www.apache.org/licenses/)
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

### IDE connection to maven process

This API is usually not used by mojos but for IDE integration, if enabled as a maven-core extension plexus-build-api supply a way to communicate with the running maven build and get events.
The default implementation open a tcp connections to a port specified by the system property `plexus.build.ipc.port` using key/value encoded message format. If no such value is given all messages are silently discarded.

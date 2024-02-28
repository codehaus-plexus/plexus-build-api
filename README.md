Plexus Build API
=======================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/codehaus-plexus/plexus-classworlds.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-build-api.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.plexus/plexus-build-api)
![Build Status](https://github.com/codehaus-plexus/plexus-build-api/actions/workflows/maven.yml/badge.svg)

This API allows IDEs to integrate with Maven deeper than it would be possible by just using regular Maven/Mojo API.

## Marker and File Delta support
It supports

- incremental builds e.g. allows to query which files have been touched since last build
- fine-grained error/info markers (referring to specific files in particular line numbers)
- notifications about updated files

## Process support

This API is located in the package `org.codehaus.plexus.build.process` and allows the IDE to interact with a mojo that starts new processes and has the following purposes:

- allow representation of processes in the UI e.g with a name and its current state together with a way to optionally terminate a running process
- optionally decorate the process dependeing on the use case e.g. add additional environment variables, supply classpath entries or provide listeners 

The main entry point for this is the `ProcessContext` that can be injected in a mojo in the follwoing way:

```
public MyMojo extends AbstractMojo {
   
   @Component
   private ProcessManager processContext;
   
   public void execute() {
        
        ProcessContext context = ...

        BuildProcess process = processContext.newProcess(context);
   }
}
```

As one can see we have a `ProcessManager` that is capable of creating a Process for the user to see it in the IDE and the `BuildProcess` can be used to notify the IDE about process changes, e.g. if it is terminated.
There is also a `ProcessContext` passed to the call that allow the mojo to supply basic information like the name or if it can be terminated but also can implements different callbacks that might (or might not) be called to furhter decorate the process.
The follwoing callbacks are currently supported:

### ProcessCallback

`ProcessCallback` are quite bare callback that allow to request for adding (or replacing) an environment variable with `ProcessCallback#setEnvironmentVariable`,
any context that support should implement the interface. Even though a request is made it could be reqjected, e.g because the context don't allow this because
it uses a variable with the same name already and don'T want to replace it.

A basic implementation of this is provided with `ProcessBuilderContext` what implements this using a ProcessBuilder, so if processes are currently implemented that way it can be reused.

### JavaCallback

`JavaCallback` offers specialized way to add options to a launch that is running in a JVM (either directly or forked). The possible callbacks are

- setSystemProperty for requesting to add (or replace) a system property
- setVMOption  for requesting to add (or replace) a VM option
- addClasspathEntry for requesting to add a new jar on the classpath

There is currently no implementation added here but one can reuse `ProcessBuilderContext` for performin neccesary actions.

### JUnitCallback

`JUnitCallback` offer specialized option to a launch that is executing test using JUnit framework. The possible callbacks are

- addTestClasspathEntry for requesting to add a new jar on the test classpath
- addTestExecutionListener for reuqest to add a new listener that is notified about test events
- getJUnitVersion allows to query for the used JUnit version

Current Implementations
-----

### Default Implementation

- Marker and File Delta support is supposed to impose minimal overhead. It doesn't support incremental build and acts directly on the file system. Errors and warning are just logged through SLF4J.
- Process support is simply a no-op, it never will call any callbacks and just discards the name, all calls on the listener will simply do nothing

## Process support

### M2Eclipse

[M2Eclipse](https://www.eclipse.org/m2e/) is using this API for supporting both incremental builds and fully integrated error markers in Eclipse. They maintain information for Mojo developers at [Making Maven Plugins Compatible](https://www.eclipse.org/m2e/documentation/m2e-making-maven-plugins-compat.html).
Currently only versions up to 0.0.7 (with old Maven coordinates `org.sonatype.plexus:plexus-build-api`) are supported, this limitation is tracked in [Issue 944](https://github.com/eclipse-m2e/m2e-core/issues/944).

History
-----

The project was relocated from <https://github.com/sonatype/sisu-build-api>. Also its Maven coordinates changed from `org.sonatype.plexus:plexus-build-api` to `org.codehaus.plexus:plexus-build-api`, the API is still the same, though.

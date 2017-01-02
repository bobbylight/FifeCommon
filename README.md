FifeCommon is a base library for building Java Swing applications.  It handles the following tasks:

* Application lifecycle (bootstrap, plugin loading, cleanup, shutdown)
* Preference loading and saving
* User-configurable key bindings
* Standard modals for Options, Help, About, Printing
* File chooser (richer feature set and improved usability over JFileChooser)
* Dockable windows

For a working example of an application built with library, see
[RText](https://github.com/bobbylight/RText).

Available in the [Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.fifesoft.rtext%7Cfife.common%7C2.5.6%7Cjar) (`com.fifesoft.rtext:fife.common:XXX`).

# Building

FifeCommon uses [Gradle](http://gradle.org/) to build.  To compile, run
all unit tests, and create the jar, run:

    ./gradlew build

Note that FifeCommon only requires Java 6.  To that end, the boot classpath will be set to accommodate
this if a variable `java6CompileBootClasspath` is set to the location of `rt.jar` in a Java 6 JDK.
This can be added to `<maven-home>/gradle.properties` if desired, to avoid diffs in the project's
`gradle.properties`.  For example:

    On Windows:
      java6CompileBootClasspath=C:/java/jdk1.8.0_102/jre/lib/rt.jar
    On OS X:
      java6CompileBootClasspath=/Library/Java/JavaVirtualMachines/jdk1.8.0_102.jdk/Contents/Home/jre/lib/rt.jar


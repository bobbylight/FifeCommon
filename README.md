# FifeCommon
![Java Build](https://github.com/bobbylight/FifeCommon/actions/workflows/gradle.yml/badge.svg)
![Java Build](https://github.com/bobbylight/FifeCommon/actions/workflows/codeql-analysis.yml/badge.svg)

FifeCommon is a base library for building Java Swing applications.  It handles the following tasks:

* Application lifecycle (bootstrap, plugin loading, cleanup, shutdown)
* Preference loading and saving
* User-configurable key bindings
* Standard modals for Options, Help, About, Printing
* File chooser (richer feature set and improved usability over JFileChooser)
* Dockable windows

For a working example of an application built with library, see the following projects:
* [Edisen](https://github.com/bobbylight/Edisen) - A work-in-progress NES IDE
* [RText](https://github.com/bobbylight/RText) - A programmer's text editor

FifeCommon is available in the
[Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.fifesoft.rtext%7Cfife.common%7C5.0.0%7Cjar) (`com.fifesoft.rtext:fife.common:XXX`).

## Building

FifeCommon uses [Gradle](https://gradle.org/) to build.  To compile, run
all unit tests, and create the jar, run:

    ./gradlew build --warning-mode all

Note that FifeCommon requires Java 21 or later to build.

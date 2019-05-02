[![Build Status](https://travis-ci.org/bobbylight/FifeCommon.svg?branch=master)](https://travis-ci.org/bobbylight/FifeCommon)

FifeCommon is a base library for building Java Swing applications.  It handles the following tasks:

* Application lifecycle (bootstrap, plugin loading, cleanup, shutdown)
* Preference loading and saving
* User-configurable key bindings
* Standard modals for Options, Help, About, Printing
* File chooser (richer feature set and improved usability over JFileChooser)
* Dockable windows

For a working example of an application built with library, see
[RText](https://github.com/bobbylight/RText).

Available in the [Maven Central repository](http://search.maven.org/#artifactdetails%7Ccom.fifesoft.rtext%7Cfife.common%7C3.0.1%7Cjar) (`com.fifesoft.rtext:fife.common:XXX`).

# Building

FifeCommon uses [Gradle](http://gradle.org/) to build.  To compile, run
all unit tests, and create the jar, run:

    ./gradlew build

Note that FifeCommon requires Java 11 or later to build.

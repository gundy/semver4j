SemVer4J -- Evaluate node SemVer expressions in Java
====================================================

## Build status

[![Build Status](https://travis-ci.org/gundy/semver4j.svg?branch=master)](https://travis-ci.org/gundy/semver4j)

## SemVer

SemVer stands for "Semantic Versioning". The [SemVer specification](http://semver.org/) defines
a set of rules for versioning software, such that the kind of things that can change between
version numbers are well defined, and easily understood.

SemVer4J supports validation of, and comparison between between validated SemVer versions,
according to the rules given in the [SemVer specification](http://semver.org).

### Usage

    SemVer.valid("1.2.3") // true
    SemVer.valid("a.b.c") // false

    Version versionA = Version.fromString("1.2.3");
    Version versionB = Version.fromString("1.2.1-alpha.1+build");
    versionA.compareTo(versionB) // 1  (A>B)

## SemVer4J and npm range expressions

SemVer4J supports the same range expression language that is used in [node's SemVer library](https://github.com/npm/node-semver).  This is the library npm uses to determine which dependencies to download based on the range expressions given in `package.json`.

### Usage

    SemVer.satisfies("1.2.3", "1.x || >=2.5.0 || 5.0.0 - 7.2.3") // true

    SemVer.maxSatisfying(Arrays.asList("1.2.5", "0.3.5", "1.2.3", "1.1.4"), ">1.0") // "1.2.5"

Please refer to the link above for full details about the syntax and constructs that the
language supports.

## Dependencies

SemVer4J uses the ANTLR parser generator and runtime for parsing SemVer versions, and for parsing
the expression language used for ranges.

If you find that you have conflicts, or if you simply want to prevent additional downstream
dependencies for your software, SemVer4J provides a `nodeps` package that bundles (and relocated)
the ANTLR dependencies using the maven shade plugin.

## Strict vs Loose semantics

No attempt has been made to differentiate between node's strict and loose parsing
rules.  All parsing is done with what might be considered "loose" semantics in node's library.
The reason for this is to attempt to make SemVer4J as tolerant as possible of various
parse-related issues.

For example, the following are equivalent, and will result in version having the same value:

    version = Version.fromString("1.2.3-beta")
    version = Version.fromString("1.2.3beta")
    version = Version.fromString("v1.2.3-beta")
    version = Version.fromString("=v1.2.3-beta")
    version = Version.fromString("=1.2.3-beta")
    version = Version.fromString("=1.2.3beta")
    // etc

Calling version.toString() on any of the above will result in the canonical form:

    version.toString(); // "1.2.3-beta"


## Technical details

The ANTLR grammars in `src/main/antlr4` may provide some useful hints at the specific
grammar and implementation that SemVer4J uses.

Range expressions are parsed using ANTLR4's visitor pattern, in the `SemVerRangeExpressionVisitor`
class. Visit methods are provided for each of the important classifying / leaf nodes of the parse tree,
and for operators such as logical OR (||) and AND (no specific operator), which combine the
output from their child nodes.

Hopefully the code should be straightforward enough to follow at a high level, although the
specific requirements from node's range specification do make certain things quite complex.

The positive and negative test cases used have been borrowed from Node's own SemVer library
(linked above).  This doesn't guarantee any sort of equivalence, but should at least help
to ensure that SemVer4J is reasonably compatible with node's implementation.


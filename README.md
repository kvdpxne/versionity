# versionity

Semantic Versioning 2.0.0 for Java 25.

`versionity` is a small, dependency-free library for parsing, comparing and
reasoning about versions. It implements the official SemVer 2.0.0
specification, supports both npm-style and Maven-style ranges, and exposes
a compact integer representation for the common case where versions are
small.

## Table of contents

- [Features](#features)
- [Quick start](#quick-start)
- [Requirements](#requirements)
- [Installation](#installation)
- [Parse modes](#parse-modes)
- [Extracting versions from arbitrary strings](#extracting-versions-from-arbitrary-strings)
- [The `int` vs `long` decision](#the-int-vs-long-decision)
- [CompactVersion vs SemVerVersion](#compactversion-vs-semverversion)
- [The packed integer representation](#the-packed-integer-representation)
- [Version ranges](#version-ranges)
- [Module descriptor interop](#module-descriptor-interop)
- [Serialization](#serialization)
- [API stability](#api-stability)
- [Error handling](#error-handling)
- [Module](#module)
- [License](#license)

## Features

- Full SemVer 2.0.0 support: pre-release identifiers, build metadata and
  the official precedence chain.
- Two implementations behind a single interface:
  - `CompactVersion` – packs `major.minor.patch` into a single `int`
    (`[0, 999]` per component).
  - `SemVerVersion` – full SemVer with `long`-wide components.
- Ranges in two syntaxes:
  - npm-style: `^1.2.3`, `~1.2.3`, `1.x`, `>=1.0.0 <2.0.0`,
    `1.2.3 - 2.3.4`, `*`.
  - Maven-style: `[1.0.0,2.0.0)`, `(,2.0.0]`, `[1.0.0,)`.
- Unions of ranges via `VersionRangeSet` (`^1.0.0 || ~2.0.0`).
- Immutable, thread-safe, JPMS module.
- Zero runtime dependencies.

## Quick start

```java
import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;

Version a = Versions.parse("1.0.0-alpha");
Version b = Versions.parse("1.0.0");
Version c = Versions.parse("1.0.0+build.1");

a.isOlderThan(b);                     // true  (pre-release < release)
b.isEqual(c);                         // true  (build metadata ignored)
b.isLaterThan(Versions.of(0, 9, 0));  // true

VersionRange range = Versions.parseRange("[1.0.0,2.0.0)");
range.contains(b);                       // true
range.contains(Versions.parse("2.0.0")); // false
```

## Requirements

- Java 25 or later.
- No runtime dependencies.

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
 implementation("me.kvd.versionity:versionity:0.1.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
 implementation 'me.kvd.versionity:versionity:0.1.0'
}
```

### Maven

```xml
<dependency>
 <groupId>me.kvd.versionity</groupId>
 <artifactId>versionity</artifactId>
 <version>0.1.0</version>
</dependency>
```

## Parse modes

```java
Versions.parse("1.2.3",  ParseMode.STRICT); // ok
Versions.parse("1.2",    ParseMode.STRICT); // InvalidVersionException
Versions.parse("1.2",    ParseMode.LOOSE);  // 1.2.0
Versions.parse("v1.2.3", ParseMode.LOOSE);  // 1.2.3
Versions.parse("1.2.3",  ParseMode.NPM);    // 1.2.3
```

|  Mode  |                                   Behaviour                                   |
|:------:|:-----------------------------------------------------------------------------:|
| STRICT | SemVer 2.0.0 exactly. Default.                                                |
| LOOSE  | Trims whitespace, strips v/V/= prefixes, pads missing minor/patch with zeros. |
| NPM    | Currently identical to LOOSE. Reserved for future npm-specific quirks.        |

`Versions.parse(String)` is equivalent to `Versions.parse(s, ParseMode.STRICT)`.

## Extracting versions from arbitrary strings

```java
Versions.coerce("v1.2");             // Optional[1.2.0]
Versions.coerce("release 1.2.3");    // Optional[1.2.3]
Versions.coerce("1.2.3-beta");       // Optional[1.2.3]  (pre-release dropped)
Versions.coerce("no version");       // Optional.empty()
```

`coerce` scans the input for the first `MAJOR[.MINOR[.PATCH]]` sequence and  
builds a version from it. This mirrors `semver.coerce` from the npm  
ecosystem.

## The `int` vs `long` decision

The SemVer 2.0.0 specification places **no upper bound** on the numeric  
components `major`, `minor` and `patch`. A version such as  
`99999999999999999999.0.0` is technically valid.

`versionity` deliberately limits each component to a Java `long`  
(max. `9_223_372_036_854_775_807`). This is a documented, intentional  
restriction:

-   **Why `long` and not `BigInteger`?**  `long` is 10–100× faster, produces  
    simpler bytecode, integrates with `Long.compare`, and covers every  
    realistic version number for the next few centuries. No mainstream  
    ecosystem (Maven, Gradle, npm, Cargo, pip) approaches even  
    `1_000_000_000` on a single component.

-   **Why not `int`?** An `int` restricts the major component to  
    `2_147_483_647`, which looks accidental rather than deliberate. `long`  
    signals a deliberate bound.

-   **What if I need arbitrary precision?** That is a genuine gap. Fork  
    `SemVerVersion` and swap `long` for `BigInteger`. The rest of the  
    library will work as long as the result implements `Version`.


`CompactVersion` still uses an `int` for its packed representation: the  
`[0, 999]` range guarantees that `major * 1_000_000 + minor * 1_000 + patch`  
fits in an `int`.

## CompactVersion vs SemVerVersion

|                 |       CompactVersion       |      SemVerVersion     |
|:---------------:|:--------------------------:|:----------------------:|
| Component range | [0, 999]                   | [0, Long.MAX_VALUE]    |
| Pre-release     | no                         | yes                    |
| Build metadata  | no                         | yes                    |
| Memory          | 1 int                      | 3 long + 2 lists       |
| Use case        | hot path, hashing, storage | parsing external input |

You rarely need to think about this: `Versions.of(long, long, long)`  
automatically picks `CompactVersion` when all three components are in  
`[0, 999]`, and `SemVerVersion` otherwise. `Versions.parse(String)` always  
returns `SemVerVersion` because the input may contain pre-release data.

## The packed integer representation

```java
int packed = Versions.toCompactInt(Versions.of(1, 2, 3)); // 1_002_003
Version v  = Versions.fromPacked(packed);                 // 1.2.3
```

`toCompactInt` throws `UnsupportedVersionException` if the version has  
pre-release identifiers, build metadata, or a component above `999`.

## Version ranges

### npm-style

```java
Versions.parseRange("1.2.3");             // exact
Versions.parseRange("=1.2.3");            // exact
Versions.parseRange(">1.2.3");
Versions.parseRange(">=1.2.3");
Versions.parseRange("<2.0.0");
Versions.parseRange("<=2.0.0");
Versions.parseRange("^1.2.3");            // >=1.2.3 <2.0.0
Versions.parseRange("^0.2.3");            // >=0.2.3 <0.3.0
Versions.parseRange("^0.0.3");            // >=0.0.3 <0.0.4
Versions.parseRange("~1.2.3");            // >=1.2.3 <1.3.0
Versions.parseRange("1.x");               // >=1.0.0 <2.0.0
Versions.parseRange("1.2.x");             // >=1.2.0 <1.3.0
Versions.parseRange("*");                 // any
Versions.parseRange(">=1.0.0 <2.0.0");    // AND
Versions.parseRange("1.2.3 - 2.3.4");     // hyphen range
```

### Maven-style

```java
Versions.parseRange("[1.0.0,2.0.0]");     // closed
Versions.parseRange("[1.0.0,2.0.0)");     // half-open
Versions.parseRange("(1.0.0,2.0.0]");     // half-open
Versions.parseRange("(1.0.0,2.0.0)");     // open
Versions.parseRange("[1.0.0,)");          // at least
Versions.parseRange("(,2.0.0]");          // at most
```

### Unions

```java
VersionRange.Set set = Versions.parseRangeSet("^1.2.3 || ~2.0.0");
set.contains(Versions.parse("1.5.0"));  // true
set.contains(Versions.parse("2.0.5"));  // true
set.contains(Versions.parse("3.0.0"));  // false
```

Both `VersionRange` and `VersionRangeSet` implement `VersionConstraint`.

## Module descriptor interop

```java
ModuleDescriptor.Version mv = Versions.toModuleVersion(Versions.parse("1.2.3"));
Version v = Versions.fromModuleVersion(mv);
```

`fromModuleVersion` throws `InvalidVersionException` when the JDK's string  
form is not a valid SemVer version (for example, `1.0.0-01`, which the JDK  
accepts but SemVer does not).

`toModuleVersion` rarely throws: the JDK's parser is lenient and stores  
numeric segments that overflow `int` as textual identifiers. The  
round-trip preserves the value either way.

## Serialization

`Version` extends `java.io.Serializable`. Both implementations round-trip  
through `ObjectOutputStream`/`ObjectInputStream`.

## API stability

The public API revision lives in `me.kvd.versionity.api.ApiVersion`:

```java
if (ApiVersion.CURRENT != 2) {
  throw new IllegalStateException("Incompatible versionity API.");
}
```

Current value: **1**. See `CHANGELOG.md` for the revision history.

## Error handling

All validation failures throw `InvalidVersionException`, which extends  
`VersionException`, which extends `RuntimeException`. Callers that prefer  
to avoid exceptions can use the `tryParse` and `coerce` methods, both of  
which return `Optional`:

## Module

```
module me.kvd.versionity {
  exports me.kvd.versionity;
  exports me.kvd.versionity.api;
  exports me.kvd.versionity.api.exception;
}
```

`me.kvd.versionity.internal` is intentionally not exported.

## License

MIT License. See [`LICENSE`](LICENSE) for the full text.

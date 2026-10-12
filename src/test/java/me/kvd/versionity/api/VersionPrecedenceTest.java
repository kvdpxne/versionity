package me.kvd.versionity.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import me.kvd.versionity.Versions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link VersionComparator} implements the precedence rules
 * described in section 11 of the SemVer 2.0.0 specification.
 */
@DisplayName("SemVer 2.0.0 precedence")
class VersionPrecedenceTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Nested
  @DisplayName("component ordering")
  class ComponentOrdering {

    @Test
    @DisplayName("major dominates minor and patch")
    void majorDominates() {
      assertTrue(Version.PRECEDENCE.compare(v("2.0.0"), v("1.999.999")) > 0);
      assertTrue(Version.PRECEDENCE.compare(v("1.999.999"), v("2.0.0")) < 0);
    }

    @Test
    @DisplayName("minor dominates patch")
    void minorDominates() {
      assertTrue(Version.PRECEDENCE.compare(v("1.2.0"), v("1.1.999")) > 0);
      assertTrue(Version.PRECEDENCE.compare(v("1.1.999"), v("1.2.0")) < 0);
    }

    @Test
    @DisplayName("patch is compared last")
    void patchLast() {
      assertTrue(Version.PRECEDENCE.compare(v("1.2.3"), v("1.2.2")) > 0);
      assertTrue(Version.PRECEDENCE.compare(v("1.2.2"), v("1.2.3")) < 0);
    }

    @Test
    @DisplayName("example from the specification: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1")
    void specExample() {
      final var ordered = List.of(
        v("1.0.0"), v("2.0.0"), v("2.1.0"), v("2.1.1"));
      for (int i = 0; i < ordered.size() - 1; i++) {
        final var a = ordered.get(i);
        final var b = ordered.get(i + 1);
        assertTrue(Version.PRECEDENCE.compare(a, b) < 0,
          () -> a + " < " + b);
      }
    }

    @Test
    @DisplayName("large components beyond int range compare correctly")
    void largeComponents() {
      assertTrue(
        Version.PRECEDENCE.compare(
          v("3000000000.0.0"),
          v("2999999999.999.999")
        ) > 0
      );
    }
  }

  @Nested
  @DisplayName("pre-release vs release")
  class PreReleaseVersusRelease {

    @Test
    @DisplayName("release has higher precedence than its pre-release")
    void releaseBeatsPreRelease() {
      assertTrue(Version.PRECEDENCE.compare(v("1.0.0"), v("1.0.0-alpha")) > 0);
    }

    @Test
    @DisplayName("example from the specification: 1.0.0-alpha < 1.0.0")
    void specExample() {
      assertTrue(Version.PRECEDENCE.compare(v("1.0.0-alpha"), v("1.0.0")) < 0);
    }

    @Test
    @DisplayName("two releases with same core compare equal")
    void twoReleasesEqual() {
      assertEquals(0, Version.PRECEDENCE.compare(v("1.2.3"), v("1.2.3")));
    }
  }

  @Nested
  @DisplayName("build metadata is ignored")
  class BuildMetadataIgnored {

    @Test
    @DisplayName("differing build metadata compare equal")
    void differingBuildMetadata() {
      assertEquals(
        0,
        Version.PRECEDENCE.compare(v("1.0.0+build.1"), v("1.0.0+build.2"))
      );
    }

    @Test
    @DisplayName("build metadata does not break a pre-release comparison")
    void buildDoesNotAffectPreRelease() {
      assertTrue(
        Version.PRECEDENCE.compare(
          v("1.0.0-alpha+build.1"),
          v("1.0.0")
        ) < 0
      );
    }

    @Test
    @DisplayName("equals still distinguishes build metadata")
    void equalsStillDistinguishes() {
      assertNotEquals(v("1.0.0+build.1"), v("1.0.0+build.2"));
    }
  }

  @Nested
  @DisplayName("official precedence chain")
  class OfficialChain {

    @Test
    @DisplayName("1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta"
      + " < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0")
    void specChain() {
      final List<Version> ordered = List.of(
        v("1.0.0-alpha"),
        v("1.0.0-alpha.1"),
        v("1.0.0-alpha.beta"),
        v("1.0.0-beta"),
        v("1.0.0-beta.2"),
        v("1.0.0-beta.11"),
        v("1.0.0-rc.1"),
        v("1.0.0")
      );
      for (int i = 0; i < ordered.size() - 1; i++) {
        final var a = ordered.get(i);
        final var b = ordered.get(i + 1);

        assertTrue(Version.PRECEDENCE.compare(a, b) < 0, () -> a + " < " + b);
      }
    }

    @Test
    @DisplayName("chain holds under sorting")
    void chainUnderSorting() {
      final List<Version> shuffled = new ArrayList<>(List.of(
        v("1.0.0"),
        v("1.0.0-beta.11"),
        v("1.0.0-alpha"),
        v("1.0.0-rc.1"),
        v("1.0.0-beta"),
        v("1.0.0-alpha.beta"),
        v("1.0.0-beta.2"),
        v("1.0.0-alpha.1")
      ));
      shuffled.sort(Version.PRECEDENCE);

      assertEquals(
        List.of(
          "1.0.0-alpha",
          "1.0.0-alpha.1",
          "1.0.0-alpha.beta",
          "1.0.0-beta",
          "1.0.0-beta.2",
          "1.0.0-beta.11",
          "1.0.0-rc.1",
          "1.0.0"
        ),
        shuffled.stream().map(Version::toString).toList()
      );
    }
  }

  @Nested
  @DisplayName("comparator contract")
  class Contract {

    @Test
    @DisplayName("reflexive: compare(a, a) == 0")
    void reflexive() {
      final Version a = v("1.2.3-alpha+build");
      assertEquals(0, Version.PRECEDENCE.compare(a, a));
    }

    @Test
    @DisplayName("antisymmetric: sign(compare(a,b)) == -sign(compare(b,a))")
    void antisymmetric() {
      final List<Version> pool = List.of(
        v("1.0.0"),
        v("1.0.0-alpha"),
        v("1.0.0-alpha.1"),
        v("1.0.0+build"),
        v("2.0.0"),
        v("1.0.1"),
        v("1.1.0")
      );
      for (final Version a : pool) {
        for (final Version b : pool) {
          assertEquals(
            Integer.signum(Version.PRECEDENCE.compare(a, b)),
            -Integer.signum(Version.PRECEDENCE.compare(b, a)),
            () -> a + " vs " + b
          );
        }
      }
    }

    @Test
    @DisplayName("transitive across the whole precedence chain")
    void transitive() {
      final List<Version> pool = List.of(
        v("1.0.0-alpha"),
        v("1.0.0-alpha.1"),
        v("1.0.0-alpha.beta"),
        v("1.0.0-beta"),
        v("1.0.0-beta.2"),
        v("1.0.0-beta.11"),
        v("1.0.0-rc.1"),
        v("1.0.0")
      );
      for (final Version a : pool) {
        for (final Version b : pool) {
          for (final Version c : pool) {
            if (Version.PRECEDENCE.compare(a, b) <= 0
              && Version.PRECEDENCE.compare(b, c) <= 0) {
              assertTrue(
                Version.PRECEDENCE.compare(a, c) <= 0,
                () -> a + " <= " + b + " <= " + c
              );
            }
          }
        }
      }
    }

    @Test
    @DisplayName("null arguments throw NullPointerException")
    void nullArguments() {
      assertThrows(
        NullPointerException.class,
        () -> Version.PRECEDENCE.compare(null, v("1.0.0"))
      );
      assertThrows(
        NullPointerException.class,
        () -> Version.PRECEDENCE.compare(v("1.0.0"), null)
      );
      assertThrows(
        NullPointerException.class,
        () -> Version.PRECEDENCE.compare(null, null)
      );
    }

    @Test
    @DisplayName("INSTANCE is a singleton shared with Version.PRECEDENCE")
    void singleton() {
      assertSame(VersionComparator.INSTANCE, Version.PRECEDENCE);
      assertSame(VersionComparator.INSTANCE, VersionComparator.INSTANCE);
    }
  }

  @Nested
  @DisplayName("cross-implementation ordering")
  class CrossImplementation {

    @Test
    @DisplayName("CompactVersion 1.0.0 equals SemVerVersion 1.0.0")
    void compactEqualsSemVer() {
      assertEquals(
        0,
        Version.PRECEDENCE.compare(Versions.of(1, 0, 0), v("1.0.0"))
      );
    }

    @Test
    @DisplayName("CompactVersion 1.0.0 beats SemVerVersion 1.0.0-alpha")
    void compactBeatsPreRelease() {
      assertTrue(
        Version.PRECEDENCE.compare(Versions.of(1, 0, 0), v("1.0.0-alpha")) > 0
      );
    }

    @Test
    @DisplayName("mixed list sorts correctly")
    void mixedListSorts() {
      final List<Version> mixed = new ArrayList<>(List.of(
        v("1.0.0-alpha"),
        Versions.of(1, 0, 1),
        v("1.0.0"),
        Versions.of(1, 1, 0)
      ));
      mixed.sort(Version.PRECEDENCE);

      assertEquals(
        List.of("1.0.0-alpha", "1.0.0", "1.0.1", "1.1.0"),
        mixed.stream().map(Version::toString).toList()
      );
    }
  }

  @Nested
  @DisplayName("collection integration")
  class Collections {

    @Test
    @DisplayName("TreeSet with PRECEDENCE collapses versions differing only in build metadata")
    void treeSetCollapsesBuildMetadata() {
      final var set = new TreeSet<Version>(Version.PRECEDENCE);
      set.add(v("1.0.0+build.1"));
      set.add(v("1.0.0+build.2"));
      assertEquals(1, set.size());
    }

    @Test
    @DisplayName("TreeSet with PRECEDENCE keeps pre-release separate from release")
    void treeSetKeepsPreRelease() {
      final var set = new TreeSet<Version>(Version.PRECEDENCE);
      set.add(v("1.0.0-alpha"));
      set.add(v("1.0.0"));
      assertEquals(2, set.size());
    }

    @Test
    @DisplayName("HashMap keeps build-metadata variants as distinct keys")
    void hashMapKeepsBuildMetadata() {
      final var map = new HashMap<Version, String>();
      map.put(v("1.0.0"), "release");
      map.put(v("1.0.0+build"), "build");

      assertEquals(2, map.size());
      assertEquals("release", map.get(v("1.0.0")));
      assertEquals("build", map.get(v("1.0.0+build")));
    }

    @Test
    @DisplayName("TreeMap with PRECEDENCE keeps the last value for equal precedence")
    void treeMapUsesPrecedence() {
      final var map = new TreeMap<Version, String>(Version.PRECEDENCE);

      map.put(v("1.0.0"), "release");
      map.put(v("1.0.0-alpha"), "pre");
      map.put(v("1.0.0+build"), "build");   // same precedence as 1.0.0 -> overwrites

      // 1.0.0 and 1.0.0+build share precedence, so only two entries remain.
      assertEquals(2, map.size());

      // The build-metadata variant was inserted last, so its value wins.
      assertEquals("build", map.get(v("1.0.0")));
      assertEquals("build", map.get(v("1.0.0+build")));
      assertEquals("build", map.get(v("1.0.0+build.2")));

      // Pre-release is a separate key.
      assertEquals("pre", map.get(v("1.0.0-alpha")));
    }
  }
}
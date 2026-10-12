package me.kvd.versionity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.exception.UnsupportedVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Verifies the default methods declared on {@link Version}. */
@DisplayName("Version default contract")
class VersionContractTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Nested
  @DisplayName("pre-release accessors")
  class PreReleaseAccessors {

    @Test
    @DisplayName("plain version reports empty identifiers")
    void plain() {
      final Version version = Versions.of(1, 2, 3);
      assertTrue(version.preRelease().isEmpty());
      assertTrue(version.buildMetadata().isEmpty());
      assertFalse(version.isPreRelease());
    }

    @Test
    @DisplayName("pre-release version reports its identifiers")
    void preRelease() {
      final Version version = v("1.0.0-alpha.1+build.7");
      assertEquals(2, version.preRelease().size());
      assertEquals(2, version.buildMetadata().size());
      assertTrue(version.isPreRelease());
    }

    @Test
    @DisplayName("build-only version is not pre-release")
    void buildOnly() {
      assertFalse(v("1.0.0+build").isPreRelease());
    }

    @Test
    @DisplayName("returned lists are immutable")
    void immutable() {
      final Version version = v("1.0.0-alpha+build");
      assertThrows(
          UnsupportedOperationException.class,
          () -> version.preRelease().add("x")
      );
      assertThrows(
          UnsupportedOperationException.class,
          () -> version.buildMetadata().add("x")
      );
    }
  }

  @Nested
  @DisplayName("null-tolerant convenience methods")
  class NullTolerant {

    @Test
    @DisplayName("comparison helpers return false for null")
    void comparisonHelpers() {
      final Version version = v("1.0.0");
      assertFalse(version.isLaterThan(null));
      assertFalse(version.isLaterThanOrEqual(null));
      assertFalse(version.isEqual(null));
      assertFalse(version.isOlderThan(null));
      assertFalse(version.isOlderThanOrEqual(null));
      assertFalse(version.isCompatibleWith(null));
    }
  }

  @Nested
  @DisplayName("compatibility rules")
  class Compatibility {

    @Test
    @DisplayName("major 0 requires matching minor")
    void majorZero() {
      final Version base = v("0.5.0");
      assertTrue(base.isCompatibleWith(v("0.5.0")));
      assertTrue(base.isCompatibleWith(v("0.5.99")));
      assertTrue(base.isCompatibleWith(v("0.5.0-alpha")));
      assertFalse(base.isCompatibleWith(v("0.6.0")));
      assertFalse(base.isCompatibleWith(v("0.4.99")));
      assertFalse(base.isCompatibleWith(v("1.5.0")));
    }

    @Test
    @DisplayName("major > 0 requires only matching major")
    void majorPositive() {
      final Version base = v("1.5.0");
      assertTrue(base.isCompatibleWith(v("1.0.0")));
      assertTrue(base.isCompatibleWith(v("1.99.99")));
      assertTrue(base.isCompatibleWith(v("1.5.0-alpha")));
      assertFalse(base.isCompatibleWith(v("2.0.0")));
      assertFalse(base.isCompatibleWith(v("0.99.99")));
    }
  }

  @Nested
  @DisplayName("bump operations")
  class Bumps {

    @Test
    @DisplayName("bumpMajor resets minor and patch")
    void bumpMajor() {
      final Version bumped = v("1.2.3").bumpMajor();
      assertEquals(2, bumped.major());
      assertEquals(0, bumped.minor());
      assertEquals(0, bumped.patch());
    }

    @Test
    @DisplayName("bumpMinor resets patch")
    void bumpMinor() {
      final Version bumped = v("1.2.3").bumpMinor();
      assertEquals(1, bumped.major());
      assertEquals(3, bumped.minor());
      assertEquals(0, bumped.patch());
    }

    @Test
    @DisplayName("bumpPatch increments patch only")
    void bumpPatch() {
      final Version bumped = v("1.2.3").bumpPatch();
      assertEquals(1, bumped.major());
      assertEquals(2, bumped.minor());
      assertEquals(4, bumped.patch());
    }

    @Test
    @DisplayName("bumps drop pre-release and build metadata")
    void dropsExtensions() {
      final Version bumped = v("1.2.3-alpha+build").bumpPatch();
      assertFalse(bumped.isPreRelease());
      assertTrue(bumped.buildMetadata().isEmpty());
    }

    @Test
    @DisplayName("bumpMajor at Long.MAX_VALUE throws")
    void bumpMajorOverflow() {
      assertThrows(
          UnsupportedVersionException.class,
          () -> Versions.of(Long.MAX_VALUE, 0, 0).bumpMajor()
      );
    }

    @Test
    @DisplayName("bumpMinor at Long.MAX_VALUE throws")
    void bumpMinorOverflow() {
      assertThrows(
          UnsupportedVersionException.class,
          () -> Versions.of(0, Long.MAX_VALUE, 0).bumpMinor()
      );
    }

    @Test
    @DisplayName("bumpPatch at Long.MAX_VALUE throws")
    void bumpPatchOverflow() {
      assertThrows(
          UnsupportedVersionException.class,
          () -> Versions.of(0, 0, Long.MAX_VALUE).bumpPatch()
      );
    }
  }
}
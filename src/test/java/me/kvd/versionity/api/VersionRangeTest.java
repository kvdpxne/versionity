package me.kvd.versionity.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Verifies {@link VersionRange} construction and containment. */
@DisplayName("VersionRange")
class VersionRangeTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Nested
  @DisplayName("factories")
  class Factories {

    @Test
    @DisplayName("exactly matches one version only")
    void exactly() {
      final VersionRange range = VersionRange.exactly(v("1.2.3"));
      assertTrue(range.contains(v("1.2.3")));
      assertFalse(range.contains(v("1.2.4")));
      assertFalse(range.contains(v("1.2.2")));
    }

    @Test
    @DisplayName("atLeast is inclusive below, unbounded above")
    void atLeast() {
      final VersionRange range = VersionRange.atLeast(v("1.0.0"));
      assertTrue(range.contains(v("1.0.0")));
      assertTrue(range.contains(v("999.0.0")));
      assertFalse(range.contains(v("0.9.9")));
    }

    @Test
    @DisplayName("atMost is inclusive above, unbounded below")
    void atMost() {
      final VersionRange range = VersionRange.atMost(v("2.0.0"));
      assertTrue(range.contains(v("0.0.0")));
      assertTrue(range.contains(v("2.0.0")));
      assertFalse(range.contains(v("2.0.1")));
    }

    @Test
    @DisplayName("between is inclusive on both sides")
    void between() {
      final VersionRange range = VersionRange.between(v("1.0.0"), v("2.0.0"));
      assertTrue(range.contains(v("1.0.0")));
      assertTrue(range.contains(v("2.0.0")));
      assertTrue(range.contains(v("1.5.0")));
      assertFalse(range.contains(v("0.9.9")));
      assertFalse(range.contains(v("2.0.1")));
    }

    @Test
    @DisplayName("ALL accepts every version")
    void all() {
      assertTrue(VersionRange.ALL.contains(v("0.0.0")));
      assertTrue(VersionRange.ALL.contains(v("999.999.999")));
      assertTrue(VersionRange.ALL.contains(v("1.0.0-alpha")));
    }
  }

  @Nested
  @DisplayName("constructor validation")
  class Validation {

    @Test
    @DisplayName("minimum greater than maximum is rejected")
    void minimumGreaterThanMaximum() {
      assertThrows(
          InvalidVersionException.class,
          () -> new VersionRange(v("2.0.0"), true, v("1.0.0"), true)
      );
    }

    @Test
    @DisplayName("equal bounds require both sides inclusive")
    void equalBounds() {
      assertThrows(
          InvalidVersionException.class,
          () -> new VersionRange(v("1.0.0"), true, v("1.0.0"), false)
      );
      assertThrows(
          InvalidVersionException.class,
          () -> new VersionRange(v("1.0.0"), false, v("1.0.0"), true)
      );
      assertThrows(
          InvalidVersionException.class,
          () -> new VersionRange(v("1.0.0"), false, v("1.0.0"), false)
      );

      final VersionRange closed =
          new VersionRange(v("1.0.0"), true, v("1.0.0"), true);
      assertTrue(closed.contains(v("1.0.0")));
    }

    @Test
    @DisplayName("both bounds null is allowed (unbounded)")
    void bothNull() {
      final VersionRange range = new VersionRange(null, false, null, false);
      assertTrue(range.contains(v("0.0.0")));
      assertTrue(range.contains(v("999.0.0")));
    }

    @Test
    @DisplayName("null argument to contains throws")
    void nullContains() {
      assertThrows(
          NullPointerException.class,
          () -> VersionRange.ALL.contains(null)
      );
    }
  }

  @Nested
  @DisplayName("exclusivity")
  class Exclusivity {

    @Test
    @DisplayName("exclusive lower bound rejects the bound itself")
    void exclusiveLower() {
      final VersionRange range =
          new VersionRange(v("1.0.0"), false, v("2.0.0"), true);
      assertFalse(range.contains(v("1.0.0")));
      assertTrue(range.contains(v("1.0.1")));
    }

    @Test
    @DisplayName("exclusive upper bound rejects the bound itself")
    void exclusiveUpper() {
      final VersionRange range =
          new VersionRange(v("1.0.0"), true, v("2.0.0"), false);
      assertTrue(range.contains(v("1.9.9")));
      assertFalse(range.contains(v("2.0.0")));
    }
  }
}
package me.kvd.versionity.internal.range;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the intersection of two ranges.
 */
@DisplayName("RangeIntersector")
class RangeIntersectorTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Test
  @DisplayName("intersection of two overlapping ranges")
  void overlap() {
    final var a = VersionRange.between(v("1.0.0"), v("3.0.0"));
    final var b = VersionRange.between(v("2.0.0"), v("4.0.0"));

    final var result = RangeIntersector.intersect(a, b);
    assertTrue(result.contains(v("2.0.0")));
    assertTrue(result.contains(v("3.0.0")));
    assertFalse(result.contains(v("1.5.0")));
    assertFalse(result.contains(v("3.5.0")));
  }

  @Test
  @DisplayName("intersection with ALL is identity")
  void withAll() {
    final var a = VersionRange.between(v("1.0.0"), v("2.0.0"));
    final var result = RangeIntersector.intersect(a, VersionRange.ALL);
    assertEquals(0, Version.PRECEDENCE.compare(
      result.minimum(), a.minimum()));
    assertEquals(0, Version.PRECEDENCE.compare(
      result.maximum(), a.maximum()));
  }

  @Test
  @DisplayName("intersection at a single point")
  void singlePoint() {
    final var a = VersionRange.atMost(v("2.0.0"));
    final var b = VersionRange.atLeast(v("2.0.0"));

    final var result = RangeIntersector.intersect(a, b);
    assertTrue(result.contains(v("2.0.0")));
    assertFalse(result.contains(v("1.9.9")));
    assertFalse(result.contains(v("2.0.1")));
  }

  @Test
  @DisplayName("intersection preserves exclusivity of the tighter bound")
  void exclusivity() {
    final var a = new VersionRange(v("1.0.0"), false, null, false);
    final var b = new VersionRange(v("1.0.0"), true, v("3.0.0"), true);

    final var result = RangeIntersector.intersect(a, b);
    assertFalse(result.contains(v("1.0.0")));
    assertTrue(result.contains(v("1.0.1")));
  }
}
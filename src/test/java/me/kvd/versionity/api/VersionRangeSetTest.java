package me.kvd.versionity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies {@link VersionRange.Set} construction and containment. */
@DisplayName("VersionRangeSet")
class VersionRangeSetTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Test
  @DisplayName("union of two disjoint ranges")
  void union() {
    final var set = new VersionRange.Set(List.of(
        VersionRange.between(v("1.0.0"), v("1.9.9")),
        VersionRange.between(v("3.0.0"), v("3.9.9"))
    ));

    assertTrue(set.contains(v("1.5.0")));
    assertTrue(set.contains(v("3.5.0")));
    assertFalse(set.contains(v("2.0.0")));
  }

  @Test
  @DisplayName("empty range list is rejected")
  void emptyRejected() {
    assertThrows(
        InvalidVersionException.class,
        () -> new VersionRange.Set(List.of())
    );
  }

  @Test
  @DisplayName("null list is rejected")
  void nullRejected() {
    assertThrows(
        NullPointerException.class,
        () -> new VersionRange.Set(null)
    );
  }

  @Test
  @DisplayName("list is defensively copied")
  void defensiveCopy() {
    final var source = new java.util.ArrayList<VersionRange>();
    source.add(VersionRange.ALL);

    final var set = new VersionRange.Set(source);
    source.clear();

    assertEquals(1, set.ranges().size());
    assertTrue(set.contains(v("1.0.0")));
  }

  @Test
  @DisplayName("returned list is immutable")
  void immutableList() {
    final var set = new VersionRange.Set(List.of(VersionRange.ALL));
    assertThrows(
        UnsupportedOperationException.class,
        () -> set.ranges().add(VersionRange.ALL)
    );
  }

  @Test
  @DisplayName("null argument to contains throws")
  void nullContains() {
    final var set = new VersionRange.Set(List.of(VersionRange.ALL));
    assertThrows(NullPointerException.class, () -> set.contains(null));
  }
}
package me.kvd.versionity.internal.range;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies Maven-style range parsing.
 */
@DisplayName("MavenRangeParser")
class MavenRangeParserTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Test
  @DisplayName("closed range [a,b]")
  void closed() {
    final var range = MavenRangeParser.parse("[1.0.0,2.0.0]");
    assertTrue(range.contains(v("1.0.0")));
    assertTrue(range.contains(v("2.0.0")));
    assertTrue(range.contains(v("1.5.0")));
    assertFalse(range.contains(v("2.0.1")));
  }

  @Test
  @DisplayName("half-open [a,b)")
  void halfOpenLower() {
    final var range = MavenRangeParser.parse("[1.0.0,2.0.0)");
    assertTrue(range.contains(v("1.0.0")));
    assertFalse(range.contains(v("2.0.0")));
  }

  @Test
  @DisplayName("half-open (a,b]")
  void halfOpenUpper() {
    final var range = MavenRangeParser.parse("(1.0.0,2.0.0]");
    assertFalse(range.contains(v("1.0.0")));
    assertTrue(range.contains(v("2.0.0")));
  }

  @Test
  @DisplayName("open (a,b)")
  void open() {
    final var range = MavenRangeParser.parse("(1.0.0,2.0.0)");
    assertFalse(range.contains(v("1.0.0")));
    assertFalse(range.contains(v("2.0.0")));
    assertTrue(range.contains(v("1.5.0")));
  }

  @Test
  @DisplayName("unbounded above [a,)")
  void unboundedAbove() {
    final var range = MavenRangeParser.parse("[1.0.0,)");
    assertTrue(range.contains(v("99.0.0")));
    assertFalse(range.contains(v("0.9.9")));
  }

  @Test
  @DisplayName("unbounded below (,b]")
  void unboundedBelow() {
    final var range = MavenRangeParser.parse("(,2.0.0]");
    assertTrue(range.contains(v("0.1.0")));
    assertFalse(range.contains(v("2.0.1")));
  }

  @Test
  @DisplayName("both bounds empty is rejected")
  void bothBoundsEmpty() {
    assertThrows(InvalidVersionException.class, () -> MavenRangeParser.parse("(,)"));
  }

  @Test
  @DisplayName("malformed literal is rejected")
  void malformed() {
    assertThrows(InvalidVersionException.class, () -> MavenRangeParser.parse("[1.0.0"));
    assertThrows(InvalidVersionException.class, () -> MavenRangeParser.parse("[1.0.0,2.0.0"));
  }
}
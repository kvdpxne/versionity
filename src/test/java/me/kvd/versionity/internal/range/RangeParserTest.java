package me.kvd.versionity.internal.range;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the dispatcher between Maven- and npm-style parsers.
 */
@DisplayName("RangeParser")
class RangeParserTest {

  @Nested
  @DisplayName("single range dispatch")
  class Dispatch {

    @Test
    @DisplayName("leading [ dispatches to Maven")
    void mavenLower() {
      final var range = RangeParser.parse("[1.0.0,2.0.0)");
      assertTrue(range.contains(Versions.parse("1.0.0")));
      assertFalse(range.contains(Versions.parse("2.0.0")));
    }

    @Test
    @DisplayName("leading ( dispatches to Maven")
    void mavenUpper() {
      final var range = RangeParser.parse("(1.0.0,2.0.0]");
      assertFalse(range.contains(Versions.parse("1.0.0")));
      assertTrue(range.contains(Versions.parse("2.0.0")));
    }

    @Test
    @DisplayName("otherwise dispatches to npm")
    void npm() {
      final var range = RangeParser.parse("^1.2.3");
      assertTrue(range.contains(Versions.parse("1.5.0")));
    }

    @Test
    @DisplayName("null or empty is rejected")
    void nullOrEmpty() {
      assertThrows(InvalidVersionException.class, () -> RangeParser.parse(null));
      assertThrows(InvalidVersionException.class, () -> RangeParser.parse(""));
    }

    @Test
    @DisplayName("union operator is rejected with a helpful message")
    void unionRejected() {
      final var ex = assertThrows(
        InvalidVersionException.class,
        () -> RangeParser.parse("^1.0.0 || ^2.0.0")
      );
      assertTrue(ex.getMessage().contains("parseRangeSet"));
    }
  }

  @Nested
  @DisplayName("range set parsing")
  class RangeSet {

    @Test
    @DisplayName("single alternative behaves like a range set of one")
    void single() {
      final var set = RangeParser.parseSet("^1.2.3");
      assertTrue(set.contains(Versions.parse("1.5.0")));
      assertFalse(set.contains(Versions.parse("2.0.0")));
    }

    @Test
    @DisplayName("two alternatives join into a union")
    void union() {
      final var set = RangeParser.parseSet("^1.2.3 || ~2.0.0");
      assertTrue(set.contains(Versions.parse("1.5.0")));
      assertTrue(set.contains(Versions.parse("2.0.5")));
      assertFalse(set.contains(Versions.parse("2.1.0")));
      assertFalse(set.contains(Versions.parse("3.0.0")));
    }

    @Test
    @DisplayName("empty alternative is rejected")
    void emptyAlternative() {
      assertThrows(
        InvalidVersionException.class,
        () -> RangeParser.parseSet("^1.0.0 || ")
      );
      assertThrows(
        InvalidVersionException.class,
        () -> RangeParser.parseSet(" || ^1.0.0")
      );
    }

    @Test
    @DisplayName("null or empty input is rejected")
    void nullOrEmpty() {
      assertThrows(InvalidVersionException.class, () -> RangeParser.parseSet(null));
      assertThrows(InvalidVersionException.class, () -> RangeParser.parseSet(""));
    }

    @Test
    @DisplayName("mixing Maven and npm alternatives is allowed")
    void mixedAlternatives() {
      final var set = RangeParser.parseSet("[1.0.0,2.0.0) || ^3.0.0");
      assertTrue(set.contains(Versions.parse("1.5.0")));
      assertTrue(set.contains(Versions.parse("3.5.0")));
      assertFalse(set.contains(Versions.parse("2.5.0")));
    }
  }
}
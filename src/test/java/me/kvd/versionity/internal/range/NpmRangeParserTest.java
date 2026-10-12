package me.kvd.versionity.internal.range;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies npm-style range parsing.
 */
@DisplayName("NpmRangeParser")
class NpmRangeParserTest {

  private static Version v(final String s) {
    return Versions.parse(s);
  }

  @Nested
  @DisplayName("comparators")
  class Comparators {

    @Test
    @DisplayName("bare version is exact")
    void bare() {
      final var range = NpmRangeParser.parse("1.2.3");
      assertTrue(range.contains(v("1.2.3")));
      assertFalse(range.contains(v("1.2.4")));
    }

    @Test
    @DisplayName("= is exact")
    void equals() {
      final var range = NpmRangeParser.parse("=1.2.3");
      assertTrue(range.contains(v("1.2.3")));
    }

    @Test
    @DisplayName("> excludes the bound")
    void greater() {
      final var range = NpmRangeParser.parse(">1.2.3");
      assertFalse(range.contains(v("1.2.3")));
      assertTrue(range.contains(v("1.2.4")));
    }

    @Test
    @DisplayName(">= includes the bound")
    void greaterOrEqual() {
      final var range = NpmRangeParser.parse(">=1.2.3");
      assertTrue(range.contains(v("1.2.3")));
      assertTrue(range.contains(v("99.0.0")));
    }

    @Test
    @DisplayName("< excludes the bound")
    void less() {
      final var range = NpmRangeParser.parse("<2.0.0");
      assertFalse(range.contains(v("2.0.0")));
      assertTrue(range.contains(v("1.9.9")));
    }

    @Test
    @DisplayName("<= includes the bound")
    void lessOrEqual() {
      final var range = NpmRangeParser.parse("<=2.0.0");
      assertTrue(range.contains(v("2.0.0")));
      assertFalse(range.contains(v("2.0.1")));
    }

    @Test
    @DisplayName("space-separated AND")
    void and() {
      final var range = NpmRangeParser.parse(">=1.0.0 <2.0.0");
      assertTrue(range.contains(v("1.5.0")));
      assertFalse(range.contains(v("2.0.0")));
      assertFalse(range.contains(v("0.9.9")));
    }
  }

  @Nested
  @DisplayName("caret")
  class Caret {

    @Test
    @DisplayName("^1.2.3 locks major")
    void caretMajor() {
      final var range = NpmRangeParser.parse("^1.2.3");
      assertTrue(range.contains(v("1.2.3")));
      assertTrue(range.contains(v("1.9.9")));
      assertFalse(range.contains(v("2.0.0")));
      assertFalse(range.contains(v("1.2.2")));
    }

    @Test
    @DisplayName("^0.2.3 locks minor")
    void caretZeroMinor() {
      final var range = NpmRangeParser.parse("^0.2.3");
      assertTrue(range.contains(v("0.2.3")));
      assertTrue(range.contains(v("0.2.99")));
      assertFalse(range.contains(v("0.3.0")));
    }

    @Test
    @DisplayName("^0.0.3 locks patch")
    void caretZeroZeroMinor() {
      final var range = NpmRangeParser.parse("^0.0.3");
      assertTrue(range.contains(v("0.0.3")));
      assertFalse(range.contains(v("0.0.4")));
    }

    @Test
    @DisplayName("^0.0 has upper bound 0.1.0")
    void caretZeroZero() {
      final var range = NpmRangeParser.parse("^0.0");
      assertTrue(range.contains(v("0.0.0")));
      assertTrue(range.contains(v("0.0.99")));
      assertFalse(range.contains(v("0.1.0")));
    }
  }

  @Nested
  @DisplayName("tilde")
  class Tilde {

    @Test
    @DisplayName("~1.2.3 locks minor")
    void tilde() {
      final var range = NpmRangeParser.parse("~1.2.3");
      assertTrue(range.contains(v("1.2.3")));
      assertTrue(range.contains(v("1.2.99")));
      assertFalse(range.contains(v("1.3.0")));
    }

    @Test
    @DisplayName("~1.2 is identical to ~1.2.0")
    void tildePartial() {
      final var range = NpmRangeParser.parse("~1.2");
      assertTrue(range.contains(v("1.2.0")));
      assertFalse(range.contains(v("1.3.0")));
    }

    @Test
    @DisplayName("~1 behaves like ^1")
    void tildeMajorOnly() {
      final var range = NpmRangeParser.parse("~1");
      assertTrue(range.contains(v("1.99.0")));
      assertFalse(range.contains(v("2.0.0")));
    }
  }

  @Nested
  @DisplayName("wildcards and partials")
  class Wildcards {

    @Test
    @DisplayName("1.x locks major")
    void majorWildcard() {
      final var range = NpmRangeParser.parse("1.x");
      assertTrue(range.contains(v("1.0.0")));
      assertTrue(range.contains(v("1.99.99")));
      assertFalse(range.contains(v("2.0.0")));
    }

    @Test
    @DisplayName("1.2.x locks minor")
    void minorWildcard() {
      final var range = NpmRangeParser.parse("1.2.x");
      assertTrue(range.contains(v("1.2.0")));
      assertTrue(range.contains(v("1.2.99")));
      assertFalse(range.contains(v("1.3.0")));
    }

    @Test
    @DisplayName("bare 1 behaves like 1.x")
    void bareMajor() {
      final var range = NpmRangeParser.parse("1");
      assertTrue(range.contains(v("1.5.0")));
      assertFalse(range.contains(v("2.0.0")));
    }

    @Test
    @DisplayName("* matches everything")
    void any() {
      final var range = NpmRangeParser.parse("*");
      assertTrue(range.contains(v("0.0.0")));
      assertTrue(range.contains(v("999.999.999")));
    }

    @Test
    @DisplayName("x and X match everything")
    void aliases() {
      assertTrue(NpmRangeParser.parse("x").contains(v("1.0.0")));
      assertTrue(NpmRangeParser.parse("X").contains(v("1.0.0")));
    }
  }

  @Nested
  @DisplayName("hyphen")
  class Hyphen {

    @Test
    @DisplayName("full hyphen is inclusive on both sides")
    void full() {
      final var range = NpmRangeParser.parse("1.2.3 - 2.3.4");
      assertTrue(range.contains(v("1.2.3")));
      assertTrue(range.contains(v("2.3.4")));
      assertFalse(range.contains(v("2.3.5")));
    }

    @Test
    @DisplayName("partial right side expands the upper bound")
    void partialRight() {
      final var range = NpmRangeParser.parse("1.2 - 2.3");
      assertTrue(range.contains(v("1.2.0")));
      assertTrue(range.contains(v("2.3.99")));
      assertFalse(range.contains(v("2.4.0")));
    }
  }

  @Nested
  @DisplayName("error handling")
  class Errors {

    @Test
    @DisplayName("empty token throws")
    void empty() {
      assertThrows(InvalidVersionException.class, () -> NpmRangeParser.parse(""));
    }

    @Test
    @DisplayName("negative component throws")
    void negative() {
      assertThrows(InvalidVersionException.class, () -> NpmRangeParser.parse("-1.0.0"));
    }

    @Test
    @DisplayName("too many components throws")
    void tooMany() {
      assertThrows(
        InvalidVersionException.class,
        () -> NpmRangeParser.parse("1.2.3.4")
      );
    }
  }
}
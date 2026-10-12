package me.kvd.versionity.internal.parser;

import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies lenient parsing.
 */
@DisplayName("LooseParser")
class LooseParserTest {

  @ParameterizedTest
  @CsvSource({
    "' 1.2.3 ',    1.2.3",
    "v1.2.3,       1.2.3",
    "V1.2.3,       1.2.3",
    "=1.2.3,       1.2.3",
    "1,            1.0.0",
    "1.2,          1.2.0",
    "1-alpha,      1.0.0-alpha",
    "v1.2+build,   1.2.0+build",
    "1.2.3-alpha,  1.2.3-alpha",
    "vv1.2.3,      1.2.3",
    "==1.2.3,      1.2.3",
  })
  @DisplayName("normalises prefixes, whitespace and missing components")
  void normalises(final String input, final String expected) {
    assertEquals(expected, LooseParser.parse(input).toString());
  }

  @Test
  @DisplayName("null input throws NullPointerException")
  void nullInput() {
    assertThrows(NullPointerException.class, () -> LooseParser.parse(null));
  }

  @Test
  @DisplayName("empty after prefix stripping throws")
  void emptyAfterPrefix() {
    assertThrows(InvalidVersionException.class, () -> LooseParser.parse(""));
    assertThrows(InvalidVersionException.class, () -> LooseParser.parse("v"));
    assertThrows(InvalidVersionException.class, () -> LooseParser.parse("="));
  }

  @Test
  @DisplayName("still rejects malformed cores")
  void malformed() {
    assertThrows(InvalidVersionException.class, () -> LooseParser.parse("1.2.3.4"));
    assertThrows(InvalidVersionException.class, () -> LooseParser.parse("1.x"));
  }
}
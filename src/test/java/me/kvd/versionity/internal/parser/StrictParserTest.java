package me.kvd.versionity.internal.parser;

import java.util.List;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies strict SemVer 2.0.0 parsing.
 */
@DisplayName("StrictParser")
class StrictParserTest {

  @Test
  @DisplayName("plain version has no extensions")
  void plain() {
    final var version = StrictParser.parse("1.2.3");
    assertTrue(version.preRelease().isEmpty());
    assertTrue(version.buildMetadata().isEmpty());
  }

  @Test
  @DisplayName("pre-release is preserved")
  void preRelease() {
    final var version = StrictParser.parse("1.2.3-alpha.1");
    assertEquals(List.of("alpha", "1"), version.preRelease());
  }

  @Test
  @DisplayName("build metadata is preserved")
  void buildMetadata() {
    final var version = StrictParser.parse("1.2.3+build.7");
    assertEquals(List.of("build", "7"), version.buildMetadata());
  }

  @Test
  @DisplayName("full form is preserved")
  void full() {
    final var version = StrictParser.parse("1.2.3-alpha.1+build.7");
    assertEquals(List.of("alpha", "1"), version.preRelease());
    assertEquals(List.of("build", "7"), version.buildMetadata());
  }

  @Test
  @DisplayName("null input throws NullPointerException")
  void nullInput() {
    assertThrows(NullPointerException.class, () -> StrictParser.parse(null));
  }

  @Test
  @DisplayName("empty input throws InvalidVersionException")
  void emptyInput() {
    assertThrows(InvalidVersionException.class, () -> StrictParser.parse(""));
  }

  @Test
  @DisplayName("isValid matches parse")
  void isValid() {
    assertTrue(StrictParser.isValid("1.2.3"));
    assertTrue(StrictParser.isValid("1.0.0-x-y-z.--"));
    assertFalse(StrictParser.isValid("1.2"));
    assertFalse(StrictParser.isValid("v1.2.3"));
    assertFalse(StrictParser.isValid(null));
  }
}
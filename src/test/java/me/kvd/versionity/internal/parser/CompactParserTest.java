package me.kvd.versionity.internal.parser;

import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies two- and three-component compact parsing.
 */
@DisplayName("CompactParser")
class CompactParserTest {

  @Test
  @DisplayName("major.minor parses with implicit patch 0")
  void twoComponents() {
    final var version = CompactParser.parse("1.2");
    assertEquals(1, version.major());
    assertEquals(2, version.minor());
    assertEquals(0, version.patch());
  }

  @Test
  @DisplayName("major.minor.patch parses")
  void threeComponents() {
    final var version = CompactParser.parse("1.2.3");
    assertEquals(1, version.major());
    assertEquals(2, version.minor());
    assertEquals(3, version.patch());
  }

  @Test
  @DisplayName("rejects null or empty")
  void nullOrEmpty() {
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse(null));
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse(""));
  }

  @Test
  @DisplayName("rejects wrong number of components")
  void wrongComponentCount() {
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse("1"));
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse("1.2.3.4"));
  }

  @Test
  @DisplayName("rejects non-numeric components")
  void nonNumeric() {
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse("a.b.c"));
  }

  @Test
  @DisplayName("rejects component above 999")
  void outOfRange() {
    assertThrows(InvalidVersionException.class, () -> CompactParser.parse("1000.0.0"));
  }

  @Test
  @DisplayName("isValid matches parse")
  void isValid() {
    assertTrue(CompactParser.isValid("1.2"));
    assertTrue(CompactParser.isValid("1.2.3"));
    assertFalse(CompactParser.isValid("1"));
    assertFalse(CompactParser.isValid("1.2.3.4"));
    assertFalse(CompactParser.isValid(null));
  }
}
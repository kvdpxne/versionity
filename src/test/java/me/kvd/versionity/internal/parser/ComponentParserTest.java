package me.kvd.versionity.internal.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Verifies numeric component parsing. */
@DisplayName("ComponentParser")
class ComponentParserTest {

  @Test
  @DisplayName("plain long is parsed")
  void plain() {
    assertEquals(42L, ComponentParser.parse("x", "42"));
  }

  @Test
  @DisplayName("Long.MAX_VALUE is parsed")
  void maxLong() {
    assertEquals(Long.MAX_VALUE, ComponentParser.parse("x", "9223372036854775807"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "abc",
      "1.2",
      "99999999999999999999"
  })
  @DisplayName("invalid input throws InvalidVersionException")
  void invalid(final String input) {
    final var ex = assertThrows(
        InvalidVersionException.class,
        () -> ComponentParser.parse("major", input)
    );
    assertTrue(ex.getMessage().contains("major"));
  }
}
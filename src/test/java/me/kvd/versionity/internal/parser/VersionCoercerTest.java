package me.kvd.versionity.internal.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies extraction of version-like patterns from arbitrary strings.
 */
@DisplayName("VersionCoercer")
class VersionCoercerTest {

  @ParameterizedTest
  @CsvSource({
    "v1.2,           1.2.0",
    "v1.2.3,         1.2.3",
    "release 1.2.3,  1.2.3",
    "1.2.3-beta,     1.2.3",
    "1.2.3+meta,     1.2.3",
    "1.2.3.4,        1.2.3",
    "version 42,     42.0.0",
    "build-7.5.1,    7.5.1",
  })
  @DisplayName("extracts the first pattern")
  void extracts(final String input, final String expected) {
    assertEquals(expected, VersionCoercer.coerce(input).orElseThrow().toString());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "no version", "alpha.beta", "v", "="})
  @DisplayName("returns empty when no numeric pattern is found")
  void noPattern(final String input) {
    assertTrue(VersionCoercer.coerce(input).isEmpty());
  }

  @Test
  @DisplayName("null returns empty")
  void nullInput() {
    assertTrue(VersionCoercer.coerce(null).isEmpty());
  }

  @Test
  @DisplayName("oversized pattern returns empty")
  void oversized() {
    assertTrue(VersionCoercer.coerce("99999999999999999999").isEmpty());
  }
}
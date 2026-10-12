package me.kvd.versionity.internal.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that npm parsing matches lenient parsing in this release.
 */
@DisplayName("NpmParser")
class NpmParserTest {

  @ParameterizedTest
  @CsvSource({
    "v1.2.3,     1.2.3",
    "1.2,        1.2.0",
    "1,          1.0.0",
    "1.2.3-beta, 1.2.3-beta",
  })
  @DisplayName("behaves like LooseParser")
  void behavesLikeLoose(final String input, final String expected) {
    assertEquals(expected, NpmParser.parse(input).toString());
  }
}
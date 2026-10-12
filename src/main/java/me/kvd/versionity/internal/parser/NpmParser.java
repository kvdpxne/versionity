package me.kvd.versionity.internal.parser;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Parses npm-flavoured version strings.
 *
 * <p>Currently identical to {@link LooseParser}. The distinction is kept
 * so that npm-specific quirks can be added later without changing call
 * sites.</p>
 *
 * @since 0.1.0
 */
public final class NpmParser {

  private NpmParser() {
    throw new UnsupportedOperationException(
      "NpmParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses the given string in npm style.
   *
   * @param input the input; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws NullPointerException    if {@code input} is {@code null}.
   * @throws InvalidVersionException if the input cannot be parsed.
   * @since 0.1.0
   */
  public static Version parse(final String input) {
    return LooseParser.parse(input);
  }
}
package me.kvd.versionity.internal.parser;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.internal.CompactVersion;

/**
 * Parses compact version strings of the form {@code "major.minor"} or
 * {@code "major.minor.patch"} into {@link CompactVersion} instances.
 *
 * @since 0.1.0
 */
public final class CompactParser {

  private CompactParser() {
    throw new UnsupportedOperationException(
      "CompactParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses the given compact string.
   *
   * @param input the input; must not be {@code null} or empty.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the input is malformed or any
   *                                 component is out of range.
   * @since 0.1.0
   */
  public static Version parse(final String input) {
    if (null == input || input.isEmpty()) {
      throw new InvalidVersionException(
        "Compact version string must not be null or empty."
      );
    }

    final var parts = input.split("\\.", -1);
    if (2 > parts.length || 3 < parts.length) {
      throw new InvalidVersionException(
        "Compact version must have 2 or 3 dot-separated components: \""
          + input + "\""
      );
    }

    final long major;
    final long minor;
    final long patch;

    try {
      major = Long.parseLong(parts[0]);
      minor = Long.parseLong(parts[1]);
      patch = 2 == parts.length ? 0L : Long.parseLong(parts[2]);
    } catch (final NumberFormatException exception) {
      throw new InvalidVersionException(
        "Compact version contains a non-numeric or oversized component: \""
          + input + "\"",
        exception
      );
    }

    return new CompactVersion(major, minor, patch);
  }

  /**
   * Checks whether the given string is a valid compact version.
   *
   * @param input the input; may be {@code null}.
   * @return {@code true} if the input is valid.
   * @since 0.1.0
   */
  public static boolean isValid(final String input) {
    try {
      parse(input);
      return true;
    } catch (final RuntimeException exception) {
      return false;
    }
  }
}
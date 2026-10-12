package me.kvd.versionity.internal.range;

import java.util.ArrayList;
import java.util.List;

import me.kvd.versionity.api.VersionRange;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Dispatches a range literal to the appropriate parser.
 *
 * @since 0.1.0
 */
public final class RangeParser {

  private RangeParser() {
    throw new UnsupportedOperationException(
        "RangeParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses a single range literal.
   *
   * @param range the literal; must not be {@code null} or empty.
   * @return the parsed range; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed or contains
   *                                 the union operator {@code ||}.
   * @since 0.1.0
   */
  public static VersionRange parse(final String range) {
    if (null == range || range.isEmpty()) {
      throw new InvalidVersionException(
          "Version range must not be null or empty."
      );
    }

    final String trimmed = range.trim();

    if (trimmed.contains("||")) {
      throw new InvalidVersionException(
          "Range contains the union operator '||'; use "
              + "Versions.parseRangeSet instead: \"" + range + "\""
      );
    }

    final char first = trimmed.charAt(0);
    if ('[' == first || '(' == first) {
      return MavenRangeParser.parse(trimmed);
    }
    return NpmRangeParser.parse(trimmed);
  }

  /**
   * Parses a range set literal, splitting on the npm-style union operator
   * {@code ||}.
   *
   * @param input the literal; must not be {@code null} or empty.
   * @return the parsed set; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed.
   * @since 0.1.0
   */
  public static VersionRange.Set parseSet(final String input) {
    if (null == input || input.isEmpty()) {
      throw new InvalidVersionException(
          "Version range set must not be null or empty."
      );
    }

    final String[] parts = input.split("\\|\\|", -1);
    final List<VersionRange> list = new ArrayList<>(parts.length);

    for (final String part : parts) {
      final String trimmed = part.trim();
      if (trimmed.isEmpty()) {
        throw new InvalidVersionException(
            "Empty alternative in version range set: \"" + input + "\""
        );
      }
      list.add(parse(trimmed));
    }

    return new VersionRange.Set(list);
  }
}
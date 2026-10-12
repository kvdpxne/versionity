package me.kvd.versionity.internal.range;

import java.util.regex.Pattern;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.internal.parser.StrictParser;

/**
 * Parses Maven-style version ranges: {@code [1.0.0,2.0.0)},
 * {@code (,2.0.0]}, {@code [1.0.0,)}.
 *
 * @since 0.1.0
 */
public final class MavenRangeParser {

  private static final Pattern RANGE_PATTERN = Pattern.compile(
      "^\\s*([\\[(])\\s*([^,\\s]*)\\s*,\\s*([^\\])\\s]*)\\s*([\\])])\\s*$"
  );

  private MavenRangeParser() {
    throw new UnsupportedOperationException(
        "MavenRangeParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses a Maven-style range.
   *
   * @param trimmed the trimmed literal; must start with {@code [} or
   *                {@code (}.
   * @return the parsed range; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed.
   * @since 0.1.0
   */
  public static VersionRange parse(final String trimmed) {
    final var matcher = RANGE_PATTERN.matcher(trimmed);
    if (!matcher.matches()) {
      throw new InvalidVersionException(
          "Malformed Maven-style version range: \"" + trimmed + "\""
      );
    }

    final boolean minInclusive = '[' == matcher.group(1).charAt(0);
    final boolean maxInclusive = ']' == matcher.group(4).charAt(0);

    final String minText = matcher.group(2);
    final String maxText = matcher.group(3);

    final Version minimum = minText.isEmpty()
        ? null
        : StrictParser.parse(minText);
    final Version maximum = maxText.isEmpty()
        ? null
        : StrictParser.parse(maxText);

    if (null == minimum && null == maximum) {
      throw new InvalidVersionException(
          "Version range must have at least one bound: \"" + trimmed + "\""
      );
    }

    return new VersionRange(minimum, minInclusive, maximum, maxInclusive);
  }
}
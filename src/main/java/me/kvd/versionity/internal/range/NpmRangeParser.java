package me.kvd.versionity.internal.range;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.internal.VersionFactory;
import me.kvd.versionity.internal.parser.StrictParser;

/**
 * Parses npm-style version ranges: {@code ^1.2.3}, {@code ~1.2.3},
 * {@code 1.x}, {@code *}, {@code >=1.0.0 <2.0.0}, {@code 1.2.3 - 2.3.4}.
 *
 * @since 0.1.0
 */
public final class NpmRangeParser {

  private NpmRangeParser() {
    throw new UnsupportedOperationException(
        "NpmRangeParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses an npm-style range into a single interval.
   *
   * @param input the trimmed literal; must not start with {@code [} or
   *              {@code (}.
   * @return the parsed range; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed.
   * @since 0.1.0
   */
  public static VersionRange parse(final String input) {
    if ("*".equals(input) || "x".equals(input) || "X".equals(input)) {
      return VersionRange.ALL;
    }

    final int hyphenIdx = input.indexOf(" - ");
    if (-1 != hyphenIdx) {
      final String left = input.substring(0, hyphenIdx).trim();
      final String right = input.substring(hyphenIdx + 3).trim();
      return parseHyphen(left, right);
    }

    final String[] parts = input.split("\\s+");
    if (1 < parts.length) {
      VersionRange result = VersionRange.ALL;
      for (final String part : parts) {
        result = RangeIntersector.intersect(result, parseSingle(part));
      }
      return result;
    }

    return parseSingle(input);
  }

  private static VersionRange parseSingle(final String token) {
    if (token.isEmpty()) {
      throw new InvalidVersionException("Empty version comparator.");
    }

    final char op = token.charAt(0);

    if ('^' == op) {
      return parseCaret(token.substring(1));
    }
    if ('~' == op) {
      return parseTilde(token.substring(1));
    }
    if ('>' == op) {
      if (1 < token.length() && '=' == token.charAt(1)) {
        return parseComparator(">=", token.substring(2));
      }
      return parseComparator(">", token.substring(1));
    }
    if ('<' == op) {
      if (1 < token.length() && '=' == token.charAt(1)) {
        return parseComparator("<=", token.substring(2));
      }
      return parseComparator("<", token.substring(1));
    }
    if ('=' == op) {
      return parseComparator("=", token.substring(1));
    }

    return parseBareOrWildcard(token);
  }

  private static VersionRange parseCaret(final String input) {
    final NpmPartialVersion p = parsePartial(input);

    if (null == p.major()) {
      return VersionRange.ALL;
    }

    final long major = p.major();
    final long minor = null == p.minor() ? 0L : p.minor();
    final long patch = null == p.patch() ? 0L : p.patch();

    final Version lower = VersionFactory.create(major, minor, patch);

    final Version upper;
    if (0L != major) {
      upper = VersionFactory.create(major + 1L, 0L, 0L);
    } else if (null == p.minor()) {
      upper = VersionFactory.create(1L, 0L, 0L);
    } else if (0L != minor) {
      upper = VersionFactory.create(0L, minor + 1L, 0L);
    } else if (null == p.patch()) {
      upper = VersionFactory.create(0L, 1L, 0L);
    } else {
      upper = VersionFactory.create(0L, 0L, patch + 1L);
    }

    return new VersionRange(lower, true, upper, false);
  }

  private static VersionRange parseTilde(final String input) {
    final NpmPartialVersion p = parsePartial(input);

    if (null == p.major()) {
      return VersionRange.ALL;
    }

    final long major = p.major();
    final long minor = null == p.minor() ? 0L : p.minor();
    final long patch = null == p.patch() ? 0L : p.patch();

    final Version lower = VersionFactory.create(major, minor, patch);

    final Version upper;
    if (null == p.minor()) {
      upper = VersionFactory.create(major + 1L, 0L, 0L);
    } else {
      upper = VersionFactory.create(major, minor + 1L, 0L);
    }

    return new VersionRange(lower, true, upper, false);
  }

  private static VersionRange parseComparator(final String op, final String input) {
    final Version v = parseComparatorVersion(input);

    return switch (op) {
      case ">=" -> new VersionRange(v, true, null, false);
      case ">" -> new VersionRange(v, false, null, false);
      case "<=" -> new VersionRange(null, false, v, true);
      case "<" -> new VersionRange(null, false, v, false);
      case "=" -> VersionRange.exactly(v);
      default -> throw new InvalidVersionException("Unknown operator: " + op);
    };
  }

  private static VersionRange parseBareOrWildcard(final String token) {
    final NpmPartialVersion p = parsePartial(token);

    if (null == p.major()) {
      return VersionRange.ALL;
    }

    if (null == p.minor()) {
      return new VersionRange(
          VersionFactory.create(p.major(), 0L, 0L), true,
          VersionFactory.create(p.major() + 1L, 0L, 0L), false
      );
    }

    if (null == p.patch()) {
      return new VersionRange(
          VersionFactory.create(p.major(), p.minor(), 0L), true,
          VersionFactory.create(p.major(), p.minor() + 1L, 0L), false
      );
    }

    return VersionRange.exactly(
        VersionFactory.create(p.major(), p.minor(), p.patch())
    );
  }

  private static VersionRange parseHyphen(final String left, final String right) {
    final Version lower = parseComparatorVersion(left);

    Version upper;
    boolean upperInclusive;

    try {
      upper = StrictParser.parse(right);
      upperInclusive = true;
    } catch (final InvalidVersionException exception) {
      final NpmPartialVersion p = parsePartial(right);
      if (null == p.major()) {
        return VersionRange.atLeast(lower);
      }
      if (null == p.minor()) {
        upper = VersionFactory.create(p.major() + 1L, 0L, 0L);
        upperInclusive = false;
      } else if (null == p.patch()) {
        upper = VersionFactory.create(p.major(), p.minor() + 1L, 0L);
        upperInclusive = false;
      } else {
        upper = VersionFactory.create(p.major(), p.minor(), p.patch());
        upperInclusive = true;
      }
    }

    return new VersionRange(lower, true, upper, upperInclusive);
  }

  private static Version parseComparatorVersion(final String s) {
    try {
      return StrictParser.parse(s);
    } catch (final InvalidVersionException exception) {
      final NpmPartialVersion p = parsePartial(s);
      return VersionFactory.create(
          null == p.major() ? 0L : p.major(),
          null == p.minor() ? 0L : p.minor(),
          null == p.patch() ? 0L : p.patch()
      );
    }
  }

  private static NpmPartialVersion parsePartial(final String input) {
    if (input.isEmpty()) {
      throw new InvalidVersionException("Empty version component.");
    }

    final String[] parts = input.split("\\.", -1);
    if (3 < parts.length) {
      throw new InvalidVersionException(
          "Too many components in version: \"" + input + "\""
      );
    }

    final Long major = 0 < parts.length ? parseWildcardOrLong(parts[0]) : null;
    final Long minor = 1 < parts.length ? parseWildcardOrLong(parts[1]) : null;
    final Long patch = 2 < parts.length ? parseWildcardOrLong(parts[2]) : null;

    return new NpmPartialVersion(major, minor, patch);
  }

  private static Long parseWildcardOrLong(final String s) {
    if ("x".equals(s) || "X".equals(s) || "*".equals(s)) {
      return null;
    }
    try {
      final long value = Long.parseLong(s);
      if (0L > value) {
        throw new InvalidVersionException(
            "Negative version component: \"" + s + "\""
        );
      }
      return value;
    } catch (final NumberFormatException exception) {
      throw new InvalidVersionException(
          "Invalid version component: \"" + s + "\"", exception
      );
    }
  }
}
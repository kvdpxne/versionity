package me.kvd.versionity.internal.parser;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.internal.SemVerVersion;

/**
 * Parses strict SemVer 2.0.0 strings.
 *
 * @since 0.1.0
 */
public final class StrictParser {

  /**
   * Official SemVer 2.0.0 pattern.
   *
   * @since 0.1.0
   */
  private static final Pattern SEMVER_PATTERN = Pattern.compile(
    "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)"
      + "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
      + "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
      + "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
  );

  private StrictParser() {
    throw new UnsupportedOperationException(
      "StrictParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses the given string as a strict SemVer 2.0.0 version.
   *
   * @param input the input; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws NullPointerException    if {@code input} is {@code null}.
   * @throws InvalidVersionException if the input is not a valid SemVer.
   * @since 0.1.0
   */
  public static Version parse(final String input) {
    Objects.requireNonNull(input, "input");

    final var matcher = SEMVER_PATTERN.matcher(input);
    if (!matcher.matches()) {
      throw new InvalidVersionException(
        "Version is not a valid SemVer 2.0.0 string: \"" + input + "\""
      );
    }

    final var major = ComponentParser.parse("major", matcher.group(1));
    final var minor = ComponentParser.parse("minor", matcher.group(2));
    final var patch = ComponentParser.parse("patch", matcher.group(3));

    final var preRelease = null == matcher.group(4)
      ? List.<String>of()
      : List.of(matcher.group(4).split("\\.", -1));

    final var buildMetadata = null == matcher.group(5)
      ? List.<String>of()
      : List.of(matcher.group(5).split("\\.", -1));

    return new SemVerVersion(major, minor, patch, preRelease, buildMetadata);
  }

  /**
   * Checks whether the given string is a strict SemVer 2.0.0 version.
   *
   * @param input the input; may be {@code null}.
   * @return {@code true} if the input is valid.
   * @since 0.1.0
   */
  public static boolean isValid(final String input) {
    if (null == input) {
      return false;
    }
    if (!SEMVER_PATTERN.matcher(input).matches()) {
      return false;
    }
    try {
      parse(input);
      return true;
    } catch (final InvalidVersionException exception) {
      return false;
    }
  }
}
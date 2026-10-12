package me.kvd.versionity.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Full Semantic Versioning 2.0.0 implementation of {@link Version}.
 *
 * <p>This class is an implementation detail. Use
 * {@link me.kvd.versionity.Versions} to create instances.</p>
 *
 * @since 0.1.0
 */
public record SemVerVersion(
  long major,
  long minor,
  long patch,
  List<String> preRelease,
  List<String> buildMetadata
) implements Version {

  private static final Pattern IDENTIFIER_PATTERN =
    Pattern.compile("[0-9A-Za-z-]+");

  /**
   * Compact canonical constructor.
   *
   * @throws InvalidVersionException if any field violates SemVer rules.
   * @since 0.1.0
   */
  public SemVerVersion {
    if (0L > major) {
      throw new InvalidVersionException(
        "Major version must be non-negative, got: " + major
      );
    }
    if (0L > minor) {
      throw new InvalidVersionException(
        "Minor version must be non-negative, got: " + minor
      );
    }
    if (0L > patch) {
      throw new InvalidVersionException(
        "Patch version must be non-negative, got: " + patch
      );
    }

    preRelease = validatePreRelease(preRelease);
    buildMetadata = validateBuildMetadata(buildMetadata);
  }

  /**
   * Validates and defensively copies the pre-release identifiers.
   *
   * @param identifiers the identifiers; may be {@code null}.
   * @return an immutable list; never {@code null}.
   * @throws InvalidVersionException if any identifier is invalid.
   * @since 0.1.0
   */
  private static List<String> validatePreRelease(final List<String> identifiers) {
    if (null == identifiers || identifiers.isEmpty()) {
      return List.of();
    }

    final var copy = new ArrayList<String>(identifiers.size());
    for (final var identifier : identifiers) {
      if (null == identifier || identifier.isEmpty()) {
        throw new InvalidVersionException(
          "Pre-release identifier must not be null or empty."
        );
      }
      if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
        throw new InvalidVersionException(
          "Pre-release identifier contains invalid characters: \""
            + identifier + "\""
        );
      }
      if (isNumeric(identifier) && 1 < identifier.length()
        && '0' == identifier.charAt(0)) {
        throw new InvalidVersionException(
          "Numeric pre-release identifier must not have leading zeroes: \""
            + identifier + "\""
        );
      }
      copy.add(identifier);
    }

    return List.copyOf(copy);
  }

  /**
   * Validates and defensively copies the build metadata identifiers.
   *
   * @param identifiers the identifiers; may be {@code null}.
   * @return an immutable list; never {@code null}.
   * @throws InvalidVersionException if any identifier is invalid.
   * @since 0.1.0
   */
  private static List<String> validateBuildMetadata(
    final List<String> identifiers
  ) {
    if (null == identifiers || identifiers.isEmpty()) {
      return List.of();
    }

    final var copy = new ArrayList<String>(identifiers.size());
    for (final var identifier : identifiers) {
      if (null == identifier || identifier.isEmpty()) {
        throw new InvalidVersionException(
          "Build metadata identifier must not be null or empty."
        );
      }
      if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
        throw new InvalidVersionException(
          "Build metadata identifier contains invalid characters: \""
            + identifier + "\""
        );
      }
      copy.add(identifier);
    }

    return List.copyOf(copy);
  }

  /**
   * Checks whether the given string consists solely of decimal digits.
   *
   * @param s the string; must not be {@code null}.
   * @return {@code true} if the string is non-empty and numeric.
   * @since 0.1.0
   */
  private static boolean isNumeric(final String s) {
    if (s.isEmpty()) {
      return false;
    }
    for (var i = 0; i < s.length(); i++) {
      if (!Character.isDigit(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.1.0
   */
  @Override
  public String toString() {
    final var builder = new StringBuilder();
    builder.append(this.major)
      .append('.')
      .append(this.minor)
      .append('.')
      .append(this.patch);

    if (!this.preRelease.isEmpty()) {
      builder.append('-').append(String.join(".", this.preRelease));
    }
    if (!this.buildMetadata.isEmpty()) {
      builder.append('+').append(String.join(".", this.buildMetadata));
    }
    return builder.toString();
  }
}
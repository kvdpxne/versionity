package me.kvd.versionity.internal.parser;

import java.util.Optional;
import java.util.regex.Pattern;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.internal.VersionFactory;

/**
 * Extracts the first version-like pattern from an arbitrary string.
 *
 * @since 0.1.0
 */
public final class VersionCoercer {

  private static final Pattern COERCE_PATTERN = Pattern.compile(
    "(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?"
  );

  private VersionCoercer() {
    throw new UnsupportedOperationException(
      "VersionCoercer is a utility class and cannot be instantiated."
    );
  }

  /**
   * Extracts the first version-like pattern from the given input.
   *
   * @param input the string to scan; may be {@code null}.
   * @return the extracted version, or {@link Optional#empty()}.
   * @since 0.1.0
   */
  public static Optional<Version> coerce(final String input) {
    if (null == input) {
      return Optional.empty();
    }

    final var matcher = COERCE_PATTERN.matcher(input);
    if (!matcher.find()) {
      return Optional.empty();
    }

    try {
      final var major = Long.parseLong(matcher.group(1));
      final var minor = null == matcher.group(2) ? 0L : Long.parseLong(matcher.group(2));
      final var patch = null == matcher.group(3) ? 0L : Long.parseLong(matcher.group(3));

      return Optional.of(VersionFactory.create(major, minor, patch));
    } catch (final NumberFormatException exception) {
      return Optional.empty();
    }
  }
}
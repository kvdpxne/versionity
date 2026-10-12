package me.kvd.versionity.internal;

import java.util.List;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Selects the appropriate {@link Version} implementation for a set of
 * components.
 *
 * <p>If every component lies in {@code [0, 999]} and no extensions are
 * requested, a {@link CompactVersion} is returned. Otherwise a
 * {@link SemVerVersion} is returned.</p>
 *
 * @since 0.1.0
 */
public final class VersionFactory {

  private VersionFactory() {
    throw new UnsupportedOperationException(
      "VersionFactory is a utility class and cannot be instantiated."
    );
  }

  /**
   * Creates a version from numeric components, without extensions.
   *
   * @param major the major component; must be non-negative.
   * @param minor the minor component; must be non-negative.
   * @param patch the patch component; must be non-negative.
   * @return a version; never {@code null}.
   * @throws InvalidVersionException if any component is negative.
   * @since 0.1.0
   */
  public static Version create(
    final long major,
    final long minor,
    final long patch
  ) {
    checkNonNegative("Major", major);
    checkNonNegative("Minor", minor);
    checkNonNegative("Patch", patch);

    if (Version.COMPACT_MAX >= major
      && Version.COMPACT_MAX >= minor
      && Version.COMPACT_MAX >= patch) {
      return new CompactVersion(major, minor, patch);
    }
    return new SemVerVersion(major, minor, patch, List.of(), List.of());
  }

  /**
   * Creates a full SemVer version, forcing the {@link SemVerVersion}
   * implementation even when every component fits in the compact range.
   *
   * @param major         the major component; must be non-negative.
   * @param minor         the minor component; must be non-negative.
   * @param patch         the patch component; must be non-negative.
   * @param preRelease    the pre-release identifiers; may be {@code null}.
   * @param buildMetadata the build metadata identifiers; may be
   *                      {@code null}.
   * @return a version; never {@code null}.
   * @throws InvalidVersionException if any component or identifier is
   *                                 invalid.
   * @since 0.1.0
   */
  public static Version createFull(
    final long major,
    final long minor,
    final long patch,
    final List<String> preRelease,
    final List<String> buildMetadata
  ) {
    return new SemVerVersion(major, minor, patch, preRelease, buildMetadata);
  }

  /**
   * Checks that a component is non-negative.
   *
   * @param name  the component name.
   * @param value the component value.
   * @throws InvalidVersionException if the value is negative.
   * @since 0.1.0
   */
  private static void checkNonNegative(final String name, final long value) {
    if (0L > value) {
      throw new InvalidVersionException(
        name + " version must be non-negative, got: " + value
      );
    }
  }
}
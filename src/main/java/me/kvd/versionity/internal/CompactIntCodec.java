package me.kvd.versionity.internal;

import java.util.Objects;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.api.exception.UnsupportedVersionException;

/**
 * Encodes and decodes the packed integer representation of a version
 * ({@code major * 1_000_000 + minor * 1_000 + patch}).
 *
 * @since 0.1.0
 */
public final class CompactIntCodec {

  private CompactIntCodec() {
    throw new UnsupportedOperationException(
      "CompactIntCodec is a utility class and cannot be instantiated."
    );
  }

  /**
   * Creates a compact version from a packed integer.
   *
   * @param packed the packed integer; must be non-negative.
   * @return a compact version; never {@code null}.
   * @throws InvalidVersionException if {@code packed} is negative or any
   *                                 unpacked component exceeds
   *                                 {@link Version#COMPACT_MAX}.
   * @since 0.1.0
   */
  public static Version fromPacked(final int packed) {
    return CompactVersion.fromPacked(packed);
  }

  /**
   * Returns the packed integer representation of the given version.
   *
   * @param version the version; must not be {@code null}.
   * @return the packed integer.
   * @throws NullPointerException        if {@code version} is {@code null}.
   * @throws UnsupportedVersionException if the version has pre-release
   *                                     identifiers, build metadata, or a
   *                                     component outside {@code [0, 999]}.
   * @since 0.1.0
   */
  public static int toCompactInt(final Version version) {
    Objects.requireNonNull(version, "version");

    if (version instanceof CompactVersion compact) {
      return compact.toCompactInt();
    }

    if (!version.preRelease().isEmpty() || !version.buildMetadata().isEmpty()) {
      throw new UnsupportedVersionException(
        "Version with pre-release or build metadata cannot be represented "
          + "as a compact integer: " + version
      );
    }
    if (Version.COMPACT_MAX < version.major()
      || Version.COMPACT_MAX < version.minor()
      || Version.COMPACT_MAX < version.patch()) {
      throw new UnsupportedVersionException(
        "Component exceeds compact range [0, " + Version.COMPACT_MAX
          + "]: " + version
      );
    }
    return (int) (version.major() * 1_000_000L
      + version.minor() * 1_000L
      + version.patch());
  }
}
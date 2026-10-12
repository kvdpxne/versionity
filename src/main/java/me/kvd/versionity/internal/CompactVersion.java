package me.kvd.versionity.internal;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Compact {@link Version} implementation that stores
 * {@code major.minor.patch} as a single {@code int}.
 *
 * <p>Each component must be in range {@code [0, 999]}, so the maximum
 * representable version is {@code 999.999.999} and the maximum packed
 * integer is {@code 999_999_999}.</p>
 *
 * <p>This class is an implementation detail. Use
 * {@link me.kvd.versionity.Versions} to create instances.</p>
 *
 * @since 0.1.0
 */
public record CompactVersion(
  long major,
  long minor,
  long patch
) implements Version {

  /**
   * Minimum value of a single component.
   *
   * @since 0.1.0
   */
  public static final long MIN_COMPONENT = 0L;

  /**
   * Maximum value of a single component.
   *
   * @since 0.1.0
   */
  public static final long MAX_COMPONENT = 999L;

  private static final long FACTOR = 1_000L;
  private static final long FACTOR_SQUARED = FACTOR * FACTOR;

  /**
   * Compact canonical constructor.
   *
   * @throws InvalidVersionException if any component is out of range.
   * @since 0.1.0
   */
  public CompactVersion {
    checkComponent("major", major);
    checkComponent("minor", minor);
    checkComponent("patch", patch);
  }

  /**
   * Creates a compact version from a packed integer.
   *
   * @param packed the packed integer; must be non-negative and its unpacked
   *               components must be within {@code [0, 999]}.
   * @return a new compact version; never {@code null}.
   * @throws InvalidVersionException if {@code packed} is invalid.
   * @since 0.1.0
   */
  public static CompactVersion fromPacked(final int packed) {
    if (0 > packed) {
      throw new InvalidVersionException(
        "Packed version must be non-negative, got: " + packed
      );
    }
    return new CompactVersion(
      packed / FACTOR_SQUARED,
      (packed / FACTOR) % FACTOR,
      packed % FACTOR
    );
  }

  /**
   * Validates a single component against the compact range.
   *
   * @param name      the component name used in the error message.
   * @param component the component value.
   * @throws InvalidVersionException if the component is out of range.
   * @since 0.1.0
   */
  private static void checkComponent(final String name, final long component) {
    if (MIN_COMPONENT > component || MAX_COMPONENT < component) {
      throw new InvalidVersionException(
        "Component " + name + " must be in [" + MIN_COMPONENT + ", "
          + MAX_COMPONENT + "], got: " + component
      );
    }
  }

  /**
   * Returns the packed integer representation of this version:
   * {@code major * 1_000_000 + minor * 1_000 + patch}.
   *
   * @return the packed integer representation.
   * @since 0.1.0
   */
  public int toCompactInt() {
    return (int) (this.major * FACTOR_SQUARED + this.minor * FACTOR + this.patch);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.1.0
   */
  @Override
  public String toString() {
    return this.major + "." + this.minor + "." + this.patch;
  }
}
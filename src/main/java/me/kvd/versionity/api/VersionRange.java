package me.kvd.versionity.api;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Immutable interval of {@link Version} instances.
 *
 * <p>Use {@link me.kvd.versionity.Versions#parseRange(String)} to parse a
 * range from a string. This type only carries the data and answers
 * {@link #contains(Version)}.</p>
 *
 * @param minimum          the lower bound; may be {@code null}.
 * @param minimumInclusive whether the lower bound is inclusive.
 * @param maximum          the upper bound; may be {@code null}.
 * @param maximumInclusive whether the upper bound is inclusive.
 * @since 0.1.0
 */
public record VersionRange(
  Version minimum,
  boolean minimumInclusive,
  Version maximum,
  boolean maximumInclusive
) implements VersionConstraint {

  /**
   * Range matching every possible version.
   *
   * @since 0.1.0
   */
  public static final VersionRange ALL =
    new VersionRange(null, false, null, false);

  private static final Pattern MAVEN_RANGE_PATTERN = Pattern.compile(
    "^\\s*([\\[(])\\s*([^,\\s]*)\\s*,\\s*([^])\\s]*)\\s*([])])\\s*$"
  );

  /**
   * Canonical constructor.
   *
   * @throws InvalidVersionException if both bounds are present and the
   *                                 minimum has higher precedence than the
   *                                 maximum.
   * @since 0.1.0
   */
  public VersionRange {
    if (null != minimum && null != maximum) {
      final var comparison = Version.PRECEDENCE.compare(minimum, maximum);
      if (0 < comparison) {
        throw new InvalidVersionException(
          "Minimum version has higher precedence than maximum: "
            + minimum + " > " + maximum
        );
      }
      if (0 == comparison && (!minimumInclusive || !maximumInclusive)) {
        throw new InvalidVersionException(
          "Minimum equals maximum but the range is not closed on both "
            + "sides: " + minimum
        );
      }
    }
  }

  /**
   * Creates a range matching exactly the given version.
   *
   * @param version the version; must not be {@code null}.
   * @return a single-version range; never {@code null}.
   * @since 0.1.0
   */
  public static VersionRange exactly(final Version version) {
    Objects.requireNonNull(version, "version");
    return new VersionRange(version, true, version, true);
  }

  /**
   * Creates an unbounded-above range matching any version at or above the
   * given minimum.
   *
   * @param minimum the lower bound; must not be {@code null}.
   * @return the range; never {@code null}.
   * @since 0.1.0
   */
  public static VersionRange atLeast(final Version minimum) {
    Objects.requireNonNull(minimum, "minimum");
    return new VersionRange(minimum, true, null, false);
  }

  /**
   * Creates an unbounded-below range matching any version at or below the
   * given maximum.
   *
   * @param maximum the upper bound; must not be {@code null}.
   * @return the range; never {@code null}.
   * @since 0.1.0
   */
  public static VersionRange atMost(final Version maximum) {
    Objects.requireNonNull(maximum, "maximum");
    return new VersionRange(null, false, maximum, true);
  }

  /**
   * Creates a closed range.
   *
   * @param minimum the lower bound; must not be {@code null}.
   * @param maximum the upper bound; must not be {@code null}.
   * @return the range; never {@code null}.
   * @since 0.1.0
   */
  public static VersionRange between(final Version minimum, final Version maximum) {
    return new VersionRange(minimum, true, maximum, true);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.1.0
   */
  @Override
  public boolean contains(final Version version) {
    Objects.requireNonNull(version, "version");

    if (null != this.minimum) {
      final var comparison = Version.PRECEDENCE.compare(version, this.minimum);
      if (0 > comparison) {
        return false;
      }
      if (0 == comparison && !this.minimumInclusive) {
        return false;
      }
    }

    if (null != this.maximum) {
      final var comparison = Version.PRECEDENCE.compare(version, this.maximum);
      if (0 < comparison) {
        return false;
      }
      if (0 == comparison && !this.maximumInclusive) {
        return false;
      }
    }

    return true;
  }

  /**
   * Union of {@link VersionRange} instances, matching the npm-style
   * {@code ||} operator.
   *
   * @param ranges the intervals; must not be {@code null} or empty.
   * @since 0.1.0
   */
  public record Set(List<VersionRange> ranges)
    implements VersionConstraint {

    /**
     * Canonical constructor. Validates that the set is non-empty and copies
     * the list defensively.
     *
     * @throws InvalidVersionException if {@code ranges} is {@code null} or
     *                                 empty.
     * @since 0.1.0
     */
    public Set {
      Objects.requireNonNull(ranges, "ranges");
      if (ranges.isEmpty()) {
        throw new InvalidVersionException(
          "VersionRangeSet must contain at least one range."
        );
      }
      ranges = List.copyOf(ranges);
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.1.0
     */
    @Override
    public boolean contains(final Version version) {
      Objects.requireNonNull(version, "version");
      for (final var range : this.ranges) {
        if (range.contains(version)) {
          return true;
        }
      }
      return false;
    }
  }
}
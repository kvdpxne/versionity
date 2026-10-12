package me.kvd.versionity.internal.range;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;

/**
 * Computes the intersection of two {@link VersionRange} instances.
 *
 * @since 0.1.0
 */
public final class RangeIntersector {

  private RangeIntersector() {
    throw new UnsupportedOperationException(
        "RangeIntersector is a utility class and cannot be instantiated."
    );
  }

  /**
   * Intersects two ranges.
   *
   * @param a the first range; must not be {@code null}.
   * @param b the second range; must not be {@code null}.
   * @return the intersection; never {@code null}.
   * @since 0.1.0
   */
  public static VersionRange intersect(final VersionRange a, final VersionRange b) {
    Version newMin = a.minimum();
    boolean newMinInclusive = a.minimumInclusive();

    if (null == newMin) {
      newMin = b.minimum();
      newMinInclusive = b.minimumInclusive();
    } else if (null != b.minimum()) {
      final int cmp = Version.PRECEDENCE.compare(a.minimum(), b.minimum());
      if (0 > cmp) {
        newMin = b.minimum();
        newMinInclusive = b.minimumInclusive();
      } else if (0 == cmp) {
        newMinInclusive = a.minimumInclusive() && b.minimumInclusive();
      }
    }

    Version newMax = a.maximum();
    boolean newMaxInclusive = a.maximumInclusive();

    if (null == newMax) {
      newMax = b.maximum();
      newMaxInclusive = b.maximumInclusive();
    } else if (null != b.maximum()) {
      final int cmp = Version.PRECEDENCE.compare(a.maximum(), b.maximum());
      if (0 < cmp) {
        newMax = b.maximum();
        newMaxInclusive = b.maximumInclusive();
      } else if (0 == cmp) {
        newMaxInclusive = a.maximumInclusive() && b.maximumInclusive();
      }
    }

    return new VersionRange(newMin, newMinInclusive, newMax, newMaxInclusive);
  }
}
package me.kvd.versionity.api;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Total order on {@link Version} instances following Semantic Versioning
 * 2.0.0 precedence rules.
 *
 * <p>Build metadata is ignored for precedence, as required by the
 * specification. Consequently two versions that differ only in build
 * metadata compare as equal even though {@code equals} may return
 * {@code false}.</p>
 *
 * <p>Pre-release identifiers are compared by {@link PreReleaseComparator},
 * which implements the rules described in section 11 of the SemVer 2.0.0
 * specification:</p>
 * <ul>
 *   <li>numeric identifiers are compared numerically,</li>
 *   <li>numeric identifiers have lower precedence than non-numeric ones,</li>
 *   <li>non-numeric identifiers are compared lexically in ASCII order,</li>
 *   <li>a larger set of pre-release fields has higher precedence when all
 *       preceding identifiers are equal.</li>
 * </ul>
 *
 * <p>The comparator is stateless and thread-safe. Use
 * {@link Version#PRECEDENCE} to obtain the shared instance.</p>
 *
 * @since 0.1.0
 */
public final class VersionComparator
  implements Comparator<Version> {

  /**
   * Shared singleton instance.
   *
   * @since 0.1.0
   */
  public static final VersionComparator INSTANCE = new VersionComparator();

  private VersionComparator() {
    // Singleton.
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if either argument is {@code null}.
   * @since 0.1.0
   */
  @Override
  public int compare(final Version a, final Version b) {
    Objects.requireNonNull(a, "a");
    Objects.requireNonNull(b, "b");

    var result = Long.compare(a.major(), b.major());
    if (0 != result) {
      return result;
    }

    result = Long.compare(a.minor(), b.minor());
    if (0 != result) {
      return result;
    }

    result = Long.compare(a.patch(), b.patch());
    if (0 != result) {
      return result;
    }

    final var thisPre = a.preRelease();
    final var otherPre = b.preRelease();

    if (thisPre.isEmpty() && otherPre.isEmpty()) {
      return 0;
    }
    if (thisPre.isEmpty()) {
      return 1;
    }
    if (otherPre.isEmpty()) {
      return -1;
    }

    return comparePreReleaseLists(thisPre, otherPre);
  }

  /**
   * Compares two pre-release identifier lists.
   *
   * @param a the left list; must not be {@code null}.
   * @param b the right list; must not be {@code null}.
   * @return a negative integer, zero, or a positive integer.
   * @since 0.1.0
   */
  private static int comparePreReleaseLists(
    final List<String> a,
    final List<String> b
  ) {
    final var size = Math.min(a.size(), b.size());
    for (var i = 0; i < size; i++) {
      final var result = PreReleaseComparator.compare(a.get(i), b.get(i));
      if (0 != result) {
        return result;
      }
    }
    return Integer.compare(a.size(), b.size());
  }
}
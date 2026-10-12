package me.kvd.versionity.api;

/**
 * Predicate deciding whether a {@link Version} lies within some set of
 * allowed versions.
 *
 * <p>Two implementations are provided:</p>
 * <ul>
 *   <li>{@link VersionRange} – a single interval, possibly unbounded on
 *       either side.</li>
 *   <li>{@link VersionRangeSet} – a union of intervals (the npm-style
 *       {@code ||} operator).</li>
 * </ul>
 *
 * @since 0.1.0
 */
public interface VersionConstraint {

  /**
   * Checks whether the given version satisfies this constraint.
   *
   * @param version the version to check; must not be {@code null}.
   * @return {@code true} if the version is allowed.
   * @since 0.1.0
   */
  boolean contains(Version version);
}
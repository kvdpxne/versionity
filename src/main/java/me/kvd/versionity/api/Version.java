package me.kvd.versionity.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.exception.UnsupportedVersionException;

/**
 * Represents a version.
 *
 * <p>Two implementations are provided by this library:</p>
 * <ul>
 *   <li>{@code CompactVersion} – stores {@code major.minor.patch} as a
 *       single {@code int}; each component must be in range
 *       {@code [0, 999]}.</li>
 *   <li>{@code SemVerVersion} – full Semantic Versioning 2.0.0 support,
 *       including pre-release identifiers and build metadata.</li>
 * </ul>
 *
 * <p>Use {@link Versions} to create instances.</p>
 *
 * <p><strong>Precedence vs. equality.</strong> Per SemVer 2.0.0, two
 * versions that differ only in build metadata have the same precedence.
 * Precedence is exposed through {@link #PRECEDENCE} and is intentionally not
 * tied to {@code Object#equals}. Do not use {@code equals} for precedence
 * checks and always pass {@link #PRECEDENCE} to ordered collections such as
 * {@link java.util.TreeSet} or {@link java.util.TreeMap}.</p>
 *
 * <p><strong>Component range.</strong> Numeric components are represented
 * as Java {@code long}. The SemVer specification places no upper bound on
 * them; this library deliberately limits each component to
 * {@link Long#MAX_VALUE}. See the project README for the rationale.</p>
 *
 * @since 0.1.0
 */
public interface Version extends Serializable {

  /**
   * Serial version UID for the interface. Implementations are expected to
   * declare their own constant; this one exists so that {@code Version}
   * references can be deserialized across compatible releases.
   *
   * @since 0.1.0
   */
  @Serial
  long serialVersionUID = 1L;

  /**
   * Minimum value of a single component in the compact representation.
   *
   * @since 0.1.0
   */
  long COMPACT_MIN = 0L;

  /**
   * Maximum value of a single component in the compact representation.
   *
   * @since 0.1.0
   */
  long COMPACT_MAX = 999L;

  /**
   * Total order following Semantic Versioning 2.0.0 precedence rules. Build
   * metadata is ignored.
   *
   * @since 0.1.0
   */
  Comparator<Version> PRECEDENCE = VersionComparator.INSTANCE;

  /**
   * Returns the major version component.
   *
   * @return the major version component, always non-negative.
   * @since 0.1.0
   */
  long major();

  /**
   * Returns the minor version component.
   *
   * @return the minor version component, always non-negative.
   * @since 0.1.0
   */
  long minor();

  /**
   * Returns the patch version component.
   *
   * @return the patch version component, always non-negative.
   * @since 0.1.0
   */
  long patch();

  /**
   * Returns the immutable list of pre-release identifiers.
   *
   * @return the pre-release identifiers; never {@code null}, may be empty.
   * @since 0.1.0
   */
  default List<String> preRelease() {
    return List.of();
  }

  /**
   * Returns the immutable list of build metadata identifiers.
   *
   * @return the build metadata identifiers; never {@code null}, may be
   * empty.
   * @since 0.1.0
   */
  default List<String> buildMetadata() {
    return List.of();
  }

  /**
   * Checks whether this version contains pre-release identifiers.
   *
   * @return {@code true} if at least one pre-release identifier is present.
   * @since 0.1.0
   */
  default boolean isPreRelease() {
    return !this.preRelease().isEmpty();
  }

  /**
   * Checks whether this version has higher precedence than {@code other}.
   *
   * @param other the version to compare with; may be {@code null}.
   * @return {@code true} if this version is greater; {@code false} otherwise.
   * @since 0.1.0
   */
  default boolean isLaterThan(final Version other) {
    return null != other && 0 < PRECEDENCE.compare(this, other);
  }

  /**
   * Checks whether this version has higher or equal precedence than
   * {@code other}.
   *
   * @param other the version to compare with; may be {@code null}.
   * @return {@code true} if this version is greater or equal.
   * @since 0.1.0
   */
  default boolean isLaterThanOrEqual(final Version other) {
    return null != other && 0 <= PRECEDENCE.compare(this, other);
  }

  /**
   * Checks whether this version has the same precedence as {@code other}.
   *
   * @param other the version to compare with; may be {@code null}.
   * @return {@code true} if both versions have the same precedence.
   * @since 0.1.0
   */
  default boolean isEqual(final Version other) {
    return null != other && 0 == PRECEDENCE.compare(this, other);
  }

  /**
   * Checks whether this version has lower precedence than {@code other}.
   *
   * @param other the version to compare with; may be {@code null}.
   * @return {@code true} if this version is smaller.
   * @since 0.1.0
   */
  default boolean isOlderThan(final Version other) {
    return null != other && 0 > PRECEDENCE.compare(this, other);
  }

  /**
   * Checks whether this version has lower or equal precedence than
   * {@code other}.
   *
   * @param other the version to compare with; may be {@code null}.
   * @return {@code true} if this version is smaller or equal.
   * @since 0.1.0
   */
  default boolean isOlderThanOrEqual(final Version other) {
    return null != other && 0 >= PRECEDENCE.compare(this, other);
  }

  /**
   * Checks whether {@code other} is compatible with this version under
   * Semantic Versioning compatibility rules.
   *
   * <p>If this version has major {@code 0}, compatibility requires the same
   * major and minor. Otherwise, only the major must match.</p>
   *
   * @param other the version to check; may be {@code null}.
   * @return {@code true} if {@code other} is compatible.
   * @since 0.1.0
   */
  default boolean isCompatibleWith(final Version other) {
    if (null == other) {
      return false;
    }
    if (0L == this.major()) {
      return 0L == other.major() && this.minor() == other.minor();
    }
    return this.major() == other.major();
  }

  /**
   * Returns a new version with the major component incremented and the
   * remaining components reset to zero. Pre-release and build metadata are
   * dropped.
   *
   * @return a new version with a bumped major component.
   * @throws UnsupportedVersionException if the major component is already
   *                                     {@link Long#MAX_VALUE}.
   * @since 0.1.0
   */
  default Version bumpMajor() {
    if (Long.MAX_VALUE == this.major()) {
      throw new UnsupportedVersionException(
        "Cannot bump major version: long overflow."
      );
    }
    return Versions.of(this.major() + 1L, 0L, 0L);
  }

  /**
   * Returns a new version with the minor component incremented and the patch
   * component reset to zero. Pre-release and build metadata are dropped.
   *
   * @return a new version with a bumped minor component.
   * @throws UnsupportedVersionException if the minor component is already
   *                                     {@link Long#MAX_VALUE}.
   * @since 0.1.0
   */
  default Version bumpMinor() {
    if (Long.MAX_VALUE == this.minor()) {
      throw new UnsupportedVersionException(
        "Cannot bump minor version: long overflow."
      );
    }
    return Versions.of(this.major(), this.minor() + 1L, 0L);
  }

  /**
   * Returns a new version with the patch component incremented. Pre-release
   * and build metadata are dropped.
   *
   * @return a new version with a bumped patch component.
   * @throws UnsupportedVersionException if the patch component is already
   *                                     {@link Long#MAX_VALUE}.
   * @since 0.1.0
   */
  default Version bumpPatch() {
    if (Long.MAX_VALUE == this.patch()) {
      throw new UnsupportedVersionException(
        "Cannot bump patch version: long overflow."
      );
    }
    return Versions.of(this.major(), this.minor(), this.patch() + 1L);
  }
}
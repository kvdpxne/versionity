package me.kvd.versionity.internal.range;

/**
 * Internal representation of a partial version.
 *
 * <p>A {@code null} component marks a wildcard ({@code x}, {@code X},
 * {@code *}) or a component that was not supplied.</p>
 *
 * @param major the major component; may be {@code null}.
 * @param minor the minor component; may be {@code null}.
 * @param patch the patch component; may be {@code null}.
 * @since 0.1.0
 */
public record NpmPartialVersion(Long major, Long minor, Long patch) {
}
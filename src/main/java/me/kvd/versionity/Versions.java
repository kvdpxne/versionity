package me.kvd.versionity;

import java.lang.module.ModuleDescriptor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import me.kvd.versionity.api.ParseMode;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.api.exception.UnsupportedVersionException;
import me.kvd.versionity.internal.CompactIntCodec;
import me.kvd.versionity.internal.VersionFactory;
import me.kvd.versionity.internal.module.ModuleVersionBridge;
import me.kvd.versionity.internal.parser.CompactParser;
import me.kvd.versionity.internal.parser.LooseParser;
import me.kvd.versionity.internal.parser.NpmParser;
import me.kvd.versionity.internal.parser.StrictParser;
import me.kvd.versionity.internal.parser.VersionCoercer;
import me.kvd.versionity.internal.range.RangeParser;

/**
 * Public entry point of the library.
 *
 * <p>This class is the <em>composition root</em>: it is the only public
 * type that references {@code me.kvd.versionity.internal}. All other public
 * API types depend solely on the {@link Version} interface and the data
 * types in {@code me.kvd.versionity.api}.</p>
 *
 * <p>The class is stateless and cannot be instantiated.</p>
 *
 * @since 0.1.0
 */
public final class Versions {

  private Versions() {
    throw new UnsupportedOperationException(
      "Versions is a utility class and cannot be instantiated."
    );
  }

  /**
   * Creates a version from the given components. If every component fits in
   * {@code [0, 999]} a compact version is returned; otherwise a SemVer
   * version is returned.
   *
   * @param major the major component; must be non-negative.
   * @param minor the minor component; must be non-negative.
   * @param patch the patch component; must be non-negative.
   * @return a new version; never {@code null}.
   * @throws InvalidVersionException if any component is negative.
   * @since 0.1.0
   */
  public static Version of(
    final long major,
    final long minor,
    final long patch
  ) {
    return VersionFactory.create(major, minor, patch);
  }

  /**
   * Creates a full SemVer version with optional identifiers, forcing the
   * {@code SemVerVersion} implementation.
   *
   * @param major         the major component; must be non-negative.
   * @param minor         the minor component; must be non-negative.
   * @param patch         the patch component; must be non-negative.
   * @param preRelease    the pre-release identifiers; may be {@code null}.
   * @param buildMetadata the build metadata identifiers; may be
   *                      {@code null}.
   * @return a new version; never {@code null}.
   * @throws InvalidVersionException if any component or identifier is
   *                                 invalid.
   * @since 0.1.0
   */
  public static Version of(
    final long major,
    final long minor,
    final long patch,
    final List<String> preRelease,
    final List<String> buildMetadata
  ) {
    return VersionFactory.createFull(major, minor, patch, preRelease, buildMetadata);
  }

  /**
   * Creates a compact version from a packed integer.
   *
   * @param packed the packed integer; must be non-negative.
   * @return a new compact version; never {@code null}.
   * @throws InvalidVersionException if {@code packed} is invalid.
   * @since 0.1.0
   */
  public static Version fromPacked(final int packed) {
    return CompactIntCodec.fromPacked(packed);
  }

  /**
   * Returns the packed integer representation of the given version.
   *
   * @param version the version; must not be {@code null}.
   * @return the packed integer.
   * @throws NullPointerException        if {@code version} is {@code null}.
   * @throws UnsupportedVersionException if the version cannot be packed.
   * @since 0.1.0
   */
  public static int toCompactInt(final Version version) {
    return CompactIntCodec.toCompactInt(version);
  }

  /**
   * Parses a strict SemVer 2.0.0 version string. Equivalent to
   * {@code parse(input, ParseMode.STRICT)}.
   *
   * @param input the input; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the input is not valid SemVer.
   * @since 0.1.0
   */
  public static Version parse(final String input) {
    return StrictParser.parse(input);
  }

  /**
   * Alias for {@link #parse(String)}.
   *
   * @param input the input; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the input is not valid SemVer.
   * @since 0.1.0
   */
  public static Version parseSemVer(final String input) {
    return StrictParser.parse(input);
  }

  /**
   * Parses a version string in the given mode.
   *
   * @param input the input; must not be {@code null}.
   * @param mode  the mode; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws NullPointerException    if any argument is {@code null}.
   * @throws InvalidVersionException if the input is invalid.
   * @since 0.1.0
   */
  public static Version parse(final String input, final ParseMode mode) {
    Objects.requireNonNull(mode, "mode");
    return switch (mode) {
      case STRICT -> StrictParser.parse(input);
      case LOOSE -> LooseParser.parse(input);
      case NPM -> NpmParser.parse(input);
    };
  }

  /**
   * Parses a compact version string of the form {@code "major.minor"} or
   * {@code "major.minor.patch"}.
   *
   * @param input the input; must not be {@code null} or empty.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the input is malformed.
   * @since 0.1.0
   */
  public static Version parseCompact(final String input) {
    return CompactParser.parse(input);
  }

  /**
   * Extracts the first version-like pattern from the given input.
   *
   * @param input the input; may be {@code null}.
   * @return the extracted version, or {@link Optional#empty()}.
   * @since 0.1.0
   */
  public static Optional<Version> coerce(final String input) {
    return VersionCoercer.coerce(input);
  }

  /**
   * Attempts to parse the given input in strict mode.
   *
   * @param input the input; may be {@code null}.
   * @return the parsed version, or {@link Optional#empty()}.
   * @since 0.1.0
   */
  public static Optional<Version> tryParse(final String input) {
    return tryParse(input, ParseMode.STRICT);
  }

  /**
   * Attempts to parse the given input in the given mode.
   *
   * @param input the input; may be {@code null}.
   * @param mode  the mode; must not be {@code null}.
   * @return the parsed version, or {@link Optional#empty()}.
   * @since 0.1.0
   */
  public static Optional<Version> tryParse(
    final String input,
    final ParseMode mode
  ) {
    Objects.requireNonNull(mode, "mode");
    if (null == input) {
      return Optional.empty();
    }
    try {
      return Optional.of(parse(input, mode));
    } catch (final InvalidVersionException exception) {
      return Optional.empty();
    }
  }

  /**
   * Checks whether the input is a valid strict SemVer 2.0.0 version.
   *
   * @param input the input; may be {@code null}.
   * @return {@code true} if the input is valid.
   * @since 0.1.0
   */
  public static boolean isValidSemVer(final String input) {
    return StrictParser.isValid(input);
  }

  /**
   * Checks whether the input is a valid compact version.
   *
   * @param input the input; may be {@code null}.
   * @return {@code true} if the input is valid.
   * @since 0.1.0
   */
  public static boolean isValidCompact(final String input) {
    return CompactParser.isValid(input);
  }

  /**
   * Parses a version range literal in Maven-style or npm-style notation.
   *
   * @param input the literal; must not be {@code null} or empty.
   * @return the parsed range; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed or contains
   *                                 {@code ||}.
   * @since 0.1.0
   */
  public static VersionRange parseRange(final String input) {
    return RangeParser.parse(input);
  }

  /**
   * Parses a range set literal, splitting on {@code ||}.
   *
   * @param input the literal; must not be {@code null} or empty.
   * @return the parsed set; never {@code null}.
   * @throws InvalidVersionException if the literal is malformed.
   * @since 0.1.0
   */
  public static VersionRange.Set parseRangeSet(final String input) {
    return RangeParser.parseSet(input);
  }

  /**
   * Converts the version to a {@link ModuleDescriptor.Version}.
   *
   * @param version the version; must not be {@code null}.
   * @return the module descriptor version; never {@code null}.
   * @throws UnsupportedVersionException if the JDK's grammar does
   *                                     not accept this version.
   * @since 0.1.0
   */
  public static ModuleDescriptor.Version toModuleVersion(final Version version) {
    return ModuleVersionBridge.toModuleVersion(version);
  }

  /**
   * Converts a {@link ModuleDescriptor.Version} to a {@link Version}.
   *
   * @param moduleVersion the module descriptor version; must not be
   *                      {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the string form is not valid SemVer.
   * @since 0.1.0
   */
  public static Version fromModuleVersion(
    final ModuleDescriptor.Version moduleVersion
  ) {
    return ModuleVersionBridge.fromModuleVersion(moduleVersion);
  }
}
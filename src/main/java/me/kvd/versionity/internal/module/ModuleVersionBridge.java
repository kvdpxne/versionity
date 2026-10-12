package me.kvd.versionity.internal.module;

import java.lang.module.ModuleDescriptor;
import java.util.Objects;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.api.exception.UnsupportedVersionException;
import me.kvd.versionity.internal.parser.LooseParser;

/**
 * Interoperability with {@link ModuleDescriptor.Version}.
 *
 * @since 0.1.0
 */
public final class ModuleVersionBridge {

  private ModuleVersionBridge() {
    throw new UnsupportedOperationException(
      "ModuleVersionBridge is a utility class and cannot be instantiated."
    );
  }

  /**
   * Converts a {@link Version} to a
   * {@link ModuleDescriptor.Version}.
   *
   * <p>Because the JDK's parser is lenient about numeric components, this
   * method rarely throws. It only fails when the string form contains
   * characters outside the JDK's grammar, which for a well-formed
   * {@link Version} should not happen.</p>
   *
   * @param version the version; must not be {@code null}.
   * @return the module descriptor version; never {@code null}.
   * @throws NullPointerException        if {@code version} is {@code null}.
   * @throws UnsupportedVersionException if the JDK's grammar rejects the
   *                                     string form.
   * @since 0.1.0
   */
  public static ModuleDescriptor.Version toModuleVersion(final Version version) {
    Objects.requireNonNull(version, "version");
    try {
      return ModuleDescriptor.Version.parse(version.toString());
    } catch (final IllegalArgumentException exception) {
      throw new UnsupportedVersionException(
        "Version cannot be represented as a module descriptor version: "
          + version
      );
    }
  }

  /**
   * Converts a {@link ModuleDescriptor.Version} to a
   * {@link Version}.
   *
   * @param moduleVersion the module descriptor version; must not be
   *                      {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws InvalidVersionException if the string form is not a valid
   *                                 SemVer 2.0.0 version.
   * @since 0.1.0
   */
  public static Version fromModuleVersion(
    final ModuleDescriptor.Version moduleVersion
  ) {
    Objects.requireNonNull(moduleVersion, "moduleVersion");
    return LooseParser.parse(moduleVersion.toString());
  }
}
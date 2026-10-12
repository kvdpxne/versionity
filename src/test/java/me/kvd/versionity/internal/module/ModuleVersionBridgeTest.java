package me.kvd.versionity.internal.module;

import java.lang.module.ModuleDescriptor;
import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.api.exception.UnsupportedVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies interop with {@link ModuleDescriptor.Version}.
 *
 * <p><strong>Note on large components.</strong> The JDK's version grammar
 * is more permissive than SemVer: a numeric segment that does not fit in
 * a 32-bit integer is stored as a textual identifier instead of being
 * rejected. As a result, {@link ModuleVersionBridge#toModuleVersion} does
 * not throw for versions whose components exceed {@code Integer.MAX_VALUE};
 * the round-trip simply preserves the value as text. {@link
 * UnsupportedVersionException} is only thrown when the JDK's parser
 * actually rejects the string form — for example, when the version
 * contains characters outside the JDK's grammar.</p>
 */
@DisplayName("ModuleVersionBridge")
class ModuleVersionBridgeTest {

  @Test
  @DisplayName("plain version round-trips")
  void plain() {
    final Version original = Versions.parse("1.2.3");
    final var mv = ModuleVersionBridge.toModuleVersion(original);
    assertEquals("1.2.3", mv.toString());
    assertEquals(original, ModuleVersionBridge.fromModuleVersion(mv));
  }

  @Test
  @DisplayName("pre-release and build metadata round-trip")
  void full() {
    final Version original = Versions.parse("1.0.0-beta+exp.sha.5114f85");
    final var mv = ModuleVersionBridge.toModuleVersion(original);
    assertEquals(original, ModuleVersionBridge.fromModuleVersion(mv));
  }

  @Test
  @DisplayName("JDK short form '1' becomes 1.0.0")
  void jdkShortForm() {
    final var mv = ModuleDescriptor.Version.parse("1");
    final var v = ModuleVersionBridge.fromModuleVersion(mv);
    assertEquals(1, v.major());
    assertEquals(0, v.minor());
    assertEquals(0, v.patch());
  }

  @Test
  @DisplayName("JDK-only pre-release with leading zero is rejected on conversion to SemVer")
  void jdkOnlyLeadingZeroRejected() {
    final var mv = ModuleDescriptor.Version.parse("1.0.0-01");
    assertThrows(
      InvalidVersionException.class,
      () -> ModuleVersionBridge.fromModuleVersion(mv)
    );
  }

  @Test
  @DisplayName("null arguments are rejected")
  void nullArguments() {
    assertThrows(
      NullPointerException.class,
      () -> ModuleVersionBridge.toModuleVersion(null)
    );
    assertThrows(
      NullPointerException.class,
      () -> ModuleVersionBridge.fromModuleVersion(null)
    );
  }

  @Test
  @DisplayName("large component round-trips through the JDK")
  void largeComponentRoundTrip() {
    // The JDK's ModuleDescriptor.Version parser is lenient: components
    // above Integer.MAX_VALUE are stored as textual identifiers rather
    // than integers. The conversion therefore succeeds, and the value
    // must be preserved.
    final Version huge = Versions.of(3_000_000_000L, 0, 0);
    final var mv = ModuleVersionBridge.toModuleVersion(huge);
    assertEquals(huge, ModuleVersionBridge.fromModuleVersion(mv));
  }
}
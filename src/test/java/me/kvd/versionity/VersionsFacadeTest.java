package me.kvd.versionity;

import me.kvd.versionity.api.ParseMode;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.VersionRange;
import me.kvd.versionity.internal.CompactVersion;
import me.kvd.versionity.internal.SemVerVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for the facade. Detailed behaviour is verified by the
 * dedicated parser and codec specs; this class checks that delegation
 * produces the expected types and round-trips through the public surface.
 */
@DisplayName("Versions facade")
class VersionsFacadeTest {

  @Nested
  @DisplayName("factory selection")
  class FactorySelection {

    @Test
    @DisplayName("of(long, long, long) picks CompactVersion for small components")
    void smallPicksCompact() {
      assertInstanceOf(CompactVersion.class, Versions.of(1, 2, 3));
    }

    @Test
    @DisplayName("of(long, long, long) picks SemVerVersion for large components")
    void largePicksSemVer() {
      assertInstanceOf(SemVerVersion.class, Versions.of(1000, 0, 0));
    }

    @Test
    @DisplayName("of(long, long, long, List, List) always picks SemVerVersion")
    void fullAlwaysSemVer() {
      assertInstanceOf(
        SemVerVersion.class,
        Versions.of(1, 2, 3, java.util.List.of("alpha"), java.util.List.of())
      );
    }
  }

  @Nested
  @DisplayName("parse delegation")
  class ParseDelegation {

    @Test
    @DisplayName("parse(String) is strict")
    void parseStrict() {
      assertEquals("1.2.3", Versions.parse("1.2.3").toString());
    }

    @Test
    @DisplayName("parse(String, STRICT) is strict")
    void parseModeStrict() {
      assertEquals(
        "1.2.3",
        Versions.parse("1.2.3", ParseMode.STRICT).toString()
      );
    }

    @Test
    @DisplayName("parse(String, LOOSE) trims and pads")
    void parseModeLoose() {
      assertEquals(
        "1.2.0",
        Versions.parse("v1.2", ParseMode.LOOSE).toString()
      );
    }

    @Test
    @DisplayName("parse(String, NPM) behaves like LOOSE")
    void parseModeNpm() {
      assertEquals(
        "1.2.0",
        Versions.parse("1.2", ParseMode.NPM).toString()
      );
    }

    @Test
    @DisplayName("parseSemVer is an alias for parse(String)")
    void parseSemVer() {
      assertEquals("1.2.3", Versions.parseSemVer("1.2.3").toString());
    }

    @Test
    @DisplayName("parseCompact uses the compact parser")
    void parseCompact() {
      assertInstanceOf(CompactVersion.class, Versions.parseCompact("1.2"));
    }
  }

  @Nested
  @DisplayName("Optional-returning helpers")
  class OptionalHelpers {

    @Test
    @DisplayName("tryParse returns the parsed version on success")
    void tryParseSuccess() {
      assertTrue(Versions.tryParse("1.2.3").isPresent());
    }

    @Test
    @DisplayName("tryParse returns empty on failure")
    void tryParseFailure() {
      assertTrue(Versions.tryParse("garbage").isEmpty());
    }

    @Test
    @DisplayName("tryParse returns empty for null")
    void tryParseNull() {
      assertTrue(Versions.tryParse(null).isEmpty());
    }

    @Test
    @DisplayName("coerce extracts the first numeric pattern")
    void coerce() {
      assertEquals(
        "1.2.0",
        Versions.coerce("release v1.2").orElseThrow().toString()
      );
    }
  }

  @Nested
  @DisplayName("validation shortcuts")
  class ValidationShortcuts {

    @Test
    @DisplayName("isValidSemVer delegates to StrictParser")
    void isValidSemVer() {
      assertTrue(Versions.isValidSemVer("1.2.3"));
      assertFalse(Versions.isValidSemVer("1.2"));
      assertFalse(Versions.isValidSemVer(null));
    }

    @Test
    @DisplayName("isValidCompact delegates to CompactParser")
    void isValidCompact() {
      assertTrue(Versions.isValidCompact("1.2"));
      assertFalse(Versions.isValidCompact("1.2.3.4"));
      assertFalse(Versions.isValidCompact(null));
    }
  }

  @Nested
  @DisplayName("range delegation")
  class RangeDelegation {

    @Test
    @DisplayName("parseRange accepts npm syntax")
    void parseRangeNpm() {
      final VersionRange range = Versions.parseRange("^1.2.3");
      assertTrue(range.contains(Versions.parse("1.5.0")));
      assertFalse(range.contains(Versions.parse("2.0.0")));
    }

    @Test
    @DisplayName("parseRange accepts Maven syntax")
    void parseRangeMaven() {
      final VersionRange range = Versions.parseRange("[1.0.0,2.0.0)");
      assertTrue(range.contains(Versions.parse("1.5.0")));
      assertFalse(range.contains(Versions.parse("2.0.0")));
    }

    @Test
    @DisplayName("parseRangeSet supports the union operator")
    void parseRangeSet() {
      final VersionRange.Set set = Versions.parseRangeSet("^1.0.0 || ^2.0.0");
      assertTrue(set.contains(Versions.parse("1.5.0")));
      assertTrue(set.contains(Versions.parse("2.5.0")));
      assertFalse(set.contains(Versions.parse("3.0.0")));
    }
  }

  @Nested
  @DisplayName("packed integer delegation")
  class PackedIntegerDelegation {

    @Test
    @DisplayName("fromPacked and toCompactInt round-trip")
    void roundTrip() {
      final Version version = Versions.fromPacked(1_002_003);
      assertEquals(1_002_003, Versions.toCompactInt(version));
    }
  }

  @Nested
  @DisplayName("module descriptor delegation")
  class ModuleDescriptorDelegation {

    @Test
    @DisplayName("toModuleVersion and fromModuleVersion round-trip")
    void roundTrip() {
      final Version original = Versions.parse("1.2.3");
      final var mv = Versions.toModuleVersion(original);
      assertEquals(original, Versions.fromModuleVersion(mv));
    }
  }
}
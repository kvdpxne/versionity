package me.kvd.versionity.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Verifies {@link SemVerVersion} record semantics. */
@DisplayName("SemVerVersion")
class SemVerVersionTest {

  @Nested
  @DisplayName("component validation")
  class Components {

    @Test
    @DisplayName("negative components are rejected")
    void negative() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(-1, 0, 0, List.of(), List.of())
      );
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(0, -1, 0, List.of(), List.of())
      );
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(0, 0, -1, List.of(), List.of())
      );
    }

    @Test
    @DisplayName("zero components are accepted")
    void zero() {
      final var version = new SemVerVersion(0, 0, 0, List.of(), List.of());
      assertEquals(0, version.major());
    }

    @Test
    @DisplayName("components larger than int are accepted")
    void largeComponents() {
      final var version = new SemVerVersion(
          3_000_000_000L, 0, 0, List.of(), List.of()
      );
      assertEquals(3_000_000_000L, version.major());
    }
  }

  @Nested
  @DisplayName("pre-release validation")
  class PreRelease {

    @Test
    @DisplayName("null list becomes empty")
    void nullBecomesEmpty() {
      final var version = new SemVerVersion(1, 0, 0, null, null);
      assertTrue(version.preRelease().isEmpty());
      assertTrue(version.buildMetadata().isEmpty());
    }

    @Test
    @DisplayName("null element is rejected")
    void nullElement() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(1, 0, 0,
              Arrays.asList("alpha", null), List.of())
      );
    }

    @Test
    @DisplayName("empty identifier is rejected")
    void emptyIdentifier() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(1, 0, 0, List.of(""), List.of())
      );
    }

    @Test
    @DisplayName("invalid characters are rejected")
    void invalidChars() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(1, 0, 0, List.of("alpha_1"), List.of())
      );
    }

    @Test
    @DisplayName("numeric identifier with leading zero is rejected")
    void leadingZero() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(1, 0, 0, List.of("01"), List.of())
      );
    }

    @Test
    @DisplayName("zero itself is accepted")
    void zeroNumericAccepted() {
      final var version = new SemVerVersion(1, 0, 0, List.of("0"), List.of());
      assertEquals(List.of("0"), version.preRelease());
    }

    @Test
    @DisplayName("hyphen-led identifier is accepted")
    void hyphenAccepted() {
      final var version = new SemVerVersion(
          1, 0, 0, List.of("-x-y-z"), List.of()
      );
      assertTrue(version.isPreRelease());
    }

    @Test
    @DisplayName("list is defensively copied")
    void defensiveCopy() {
      final var source = new ArrayList<>(List.of("alpha"));
      final var version = new SemVerVersion(1, 0, 0, source, List.of());
      source.add("mutated");

      assertEquals(List.of("alpha"), version.preRelease());
    }
  }

  @Nested
  @DisplayName("build metadata validation")
  class BuildMetadata {

    @Test
    @DisplayName("null element is rejected")
    void nullElement() {
      assertThrows(
          InvalidVersionException.class,
          () -> new SemVerVersion(1, 0, 0, List.of(),
              Arrays.asList(null, "build"))
      );
    }

    @Test
    @DisplayName("leading zeros are allowed in build metadata")
    void leadingZeroAllowed() {
      final var version = new SemVerVersion(
          1, 0, 0, List.of(), List.of("001")
      );
      assertEquals(List.of("001"), version.buildMetadata());
    }

    @Test
    @DisplayName("list is defensively copied")
    void defensiveCopy() {
      final var source = new ArrayList<>(List.of("build"));
      final var version = new SemVerVersion(1, 0, 0, List.of(), source);
      source.add("mutated");

      assertEquals(List.of("build"), version.buildMetadata());
    }
  }

  @Nested
  @DisplayName("record semantics")
  class RecordSemantics {

    @Test
    @DisplayName("equals and hashCode use all components")
    void equalsHashCode() {
      final var a = new SemVerVersion(1, 2, 3, List.of("alpha"), List.of("build"));
      final var b = new SemVerVersion(1, 2, 3, List.of("alpha"), List.of("build"));
      final var c = new SemVerVersion(1, 2, 3, List.of("beta"), List.of("build"));

      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(a.equals(c));
    }

    @Test
    @DisplayName("toString includes extensions")
    void toStringFull() {
      final var version = new SemVerVersion(
          1, 2, 3, List.of("alpha", "1"), List.of("build", "7")
      );
      assertEquals("1.2.3-alpha.1+build.7", version.toString());
    }

    @Test
    @DisplayName("toString omits empty extensions")
    void toStringPlain() {
      final var version = new SemVerVersion(1, 2, 3, List.of(), List.of());
      assertEquals("1.2.3", version.toString());
    }
  }
}
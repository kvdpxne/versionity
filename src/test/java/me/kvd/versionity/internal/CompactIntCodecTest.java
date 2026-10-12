package me.kvd.versionity.internal;

import me.kvd.versionity.Versions;
import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import me.kvd.versionity.api.exception.UnsupportedVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies packed integer encoding and decoding.
 */
@DisplayName("CompactIntCodec")
class CompactIntCodecTest {

  @Test
  @DisplayName("0.0.0 packs to 0")
  void zero() {
    assertEquals(0, Versions.toCompactInt(Versions.of(0, 0, 0)));
  }

  @Test
  @DisplayName("999.999.999 packs to the maximum")
  void maximum() {
    assertEquals(999_999_999, Versions.toCompactInt(Versions.of(999, 999, 999)));
  }

  @Test
  @DisplayName("1.2.3 packs to 1_002_003")
  void sample() {
    assertEquals(1_002_003, Versions.toCompactInt(Versions.of(1, 2, 3)));
  }

  @Test
  @DisplayName("round-trip through packed integer")
  void roundTrip() {
    for (int packed = 0; packed < 10_000; packed++) {
      final Version restored = Versions.fromPacked(packed);
      assertEquals(packed, Versions.toCompactInt(restored));
    }
  }

  @Test
  @DisplayName("negative packed value throws")
  void negativePacked() {
    assertThrows(
      InvalidVersionException.class,
      () -> Versions.fromPacked(-1)
    );
  }

  @Test
  @DisplayName("pre-release version cannot be packed")
  void preRelease() {
    assertThrows(
      UnsupportedVersionException.class,
      () -> Versions.toCompactInt(Versions.parse("1.0.0-alpha"))
    );
  }

  @Test
  @DisplayName("build-only version cannot be packed")
  void buildOnly() {
    assertThrows(
      UnsupportedVersionException.class,
      () -> Versions.toCompactInt(Versions.parse("1.0.0+build"))
    );
  }

  @Test
  @DisplayName("component above 999 cannot be packed")
  void oversizedComponent() {
    assertThrows(
      UnsupportedVersionException.class,
      () -> Versions.toCompactInt(Versions.of(1000, 0, 0))
    );
    assertThrows(
      UnsupportedVersionException.class,
      () -> Versions.toCompactInt(Versions.of(0, 1000, 0))
    );
    assertThrows(
      UnsupportedVersionException.class,
      () -> Versions.toCompactInt(Versions.of(0, 0, 1000))
    );
  }

  @Test
  @DisplayName("null version throws")
  void nullVersion() {
    assertThrows(
      NullPointerException.class,
      () -> Versions.toCompactInt(null)
    );
  }
}
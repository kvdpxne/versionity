package me.kvd.versionity.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies implementation selection and input validation. */
@DisplayName("VersionFactory")
class VersionFactoryTest {

  @Test
  @DisplayName("small components pick CompactVersion")
  void smallPicksCompact() {
    assertInstanceOf(CompactVersion.class, VersionFactory.create(1, 2, 3));
  }

  @Test
  @DisplayName("boundary value 999 picks CompactVersion")
  void boundaryPicksCompact() {
    assertInstanceOf(CompactVersion.class, VersionFactory.create(999, 999, 999));
  }

  @Test
  @DisplayName("component above 999 picks SemVerVersion")
  void largePicksSemVer() {
    assertInstanceOf(SemVerVersion.class, VersionFactory.create(1000, 0, 0));
    assertInstanceOf(SemVerVersion.class, VersionFactory.create(0, 1000, 0));
    assertInstanceOf(SemVerVersion.class, VersionFactory.create(0, 0, 1000));
  }

  @Test
  @DisplayName("negative components are rejected before implementation selection")
  void negativeRejected() {
    final var ex = assertThrows(
        InvalidVersionException.class,
        () -> VersionFactory.create(-1, 0, 0)
    );
    assertEquals("Major version must be non-negative, got: -1", ex.getMessage());
  }

  @Test
  @DisplayName("createFull always uses SemVerVersion")
  void createFull() {
    final Version version = VersionFactory.createFull(
        1, 2, 3, List.of("alpha"), List.of("build")
    );
    assertInstanceOf(SemVerVersion.class, version);
  }

  @Test
  @DisplayName("createFull forwards list validation to SemVerVersion")
  void createFullValidates() {
    assertThrows(
        InvalidVersionException.class,
        () -> VersionFactory.createFull(1, 0, 0, List.of(""), List.of())
    );
  }
}
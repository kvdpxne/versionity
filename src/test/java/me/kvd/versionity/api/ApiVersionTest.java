package me.kvd.versionity.api;

import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the public API revision constant.
 */
@DisplayName("ApiVersion")
class ApiVersionTest {

  @Test
  @DisplayName("CURRENT is 1")
  void currentIsTwo() {
    assertEquals(1, ApiVersion.CURRENT);
  }

  @Test
  @DisplayName("the class is not instantiable")
  void notInstantiable() throws Exception {
    final var constructor = ApiVersion.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    final var thrown = assertThrows(
      InvocationTargetException.class,
      constructor::newInstance
    );
    assertEquals(UnsupportedOperationException.class, thrown.getCause().getClass());
  }
}
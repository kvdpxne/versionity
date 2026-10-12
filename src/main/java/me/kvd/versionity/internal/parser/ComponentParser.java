package me.kvd.versionity.internal.parser;

import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Parses a single numeric version component, converting
 * {@link NumberFormatException} into {@link InvalidVersionException}.
 *
 * @since 0.1.0
 */
public final class ComponentParser {

  private ComponentParser() {
    throw new UnsupportedOperationException(
      "ComponentParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses a component.
   *
   * @param name  the component name; used in the error message.
   * @param value the component text; must not be {@code null}.
   * @return the parsed value.
   * @throws InvalidVersionException if the value does not fit in a long.
   * @since 0.1.0
   */
  public static long parse(final String name, final String value) {
    try {
      return Long.parseLong(value);
    } catch (final NumberFormatException exception) {
      throw new InvalidVersionException(
        "Version component " + name + " exceeds the supported long range: "
          + "\"" + value + "\"",
        exception
      );
    }
  }
}
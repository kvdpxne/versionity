package me.kvd.versionity.api.exception;

import java.io.Serial;

/**
 * Thrown when a version string cannot be parsed because its format is
 * invalid, or when a version cannot be constructed from the provided
 * components.
 *
 * @since 0.1.0
 */
public final class InvalidVersionException
  extends VersionException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message.
   * @since 0.1.0
   */
  public InvalidVersionException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause   the cause.
   * @since 0.1.0
   */
  public InvalidVersionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
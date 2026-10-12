package me.kvd.versionity.api.exception;

import java.io.Serial;

/**
 * Base exception for all version-related failures.
 *
 * @since 0.1.0
 */
public abstract class VersionException
  extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message.
   * @since 0.1.0
   */
  protected VersionException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause   the cause.
   * @since 0.1.0
   */
  protected VersionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
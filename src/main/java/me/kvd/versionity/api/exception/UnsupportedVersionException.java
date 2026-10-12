package me.kvd.versionity.api.exception;

import java.io.Serial;

/**
 * Thrown when a valid version cannot be represented in the requested format,
 * or when an operation cannot be applied to it (for example, bumping a
 * version whose component already equals {@link Long#MAX_VALUE}).
 *
 * @since 0.1.0
 */
public final class UnsupportedVersionException
  extends VersionException {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message.
   * @since 0.1.0
   */
  public UnsupportedVersionException(final String message) {
    super(message);
  }
}
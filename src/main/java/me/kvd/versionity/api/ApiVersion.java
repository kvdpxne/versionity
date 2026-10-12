package me.kvd.versionity.api;

/**
 * Version of the public API surface exposed by this library.
 *
 * <p>Consumers may compare {@link #CURRENT} against the value they were
 * compiled with to detect an incompatible upgrade at startup:</p>
 *
 * <pre>{@code
 * if (ApiVersion.CURRENT != 1) {
 *   throw new IllegalStateException("Incompatible versionity API.");
 * }
 * }</pre>
 *
 * <p>The value is incremented whenever a public type, method or constant is
 * removed or its contract changes in a way that breaks source or binary
 * compatibility. Additive changes do not require a bump.</p>
 *
 * @since 0.1.0
 */
public final class ApiVersion {

  /**
   * Current public API revision.
   *
   * @since 0.1.0
   */
  public static final int CURRENT = 1;

  private ApiVersion() {
    throw new UnsupportedOperationException(
      "ApiVersion is a constant holder and cannot be instantiated."
    );
  }
}
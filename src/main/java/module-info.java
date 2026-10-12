/**
 * Semantic Versioning 2.0.0 library.
 *
 * <p>Exports the public API in {@code me.kvd.versionity.api}, the factory
 * {@link me.kvd.versionity.Versions} in the root package, and the exception
 * hierarchy in {@code me.kvd.versionity.api.exception}. The
 * {@code me.kvd.versionity.internal} package is intentionally not
 * exported.</p>
 *
 * @since 0.1.0
 */
module me.kvd.versionity {
  exports me.kvd.versionity;
  exports me.kvd.versionity.api;
  exports me.kvd.versionity.api.exception;
}
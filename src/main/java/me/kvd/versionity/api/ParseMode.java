package me.kvd.versionity.api;

/**
 * Strategy used by {@link me.kvd.versionity.Versions#parse(String, ParseMode)}
 * when interpreting a version string.
 *
 * @since 0.1.0
 */
public enum ParseMode {

  /**
   * Strict Semantic Versioning 2.0.0. The only accepted form is
   * {@code MAJOR.MINOR.PATCH} with optional pre-release and build metadata,
   * exactly as specified by the standard.
   *
   * @since 0.1.0
   */
  STRICT,

  /**
   * Lenient parsing intended for user input.
   *
   * <p>In addition to the strict form, this mode:</p>
   * <ul>
   *   <li>trims surrounding whitespace,</li>
   *   <li>strips leading {@code v}, {@code V} or {@code =} prefixes,</li>
   *   <li>accepts missing minor and patch components
   *       ({@code 1} &rarr; {@code 1.0.0}, {@code 1.2} &rarr;
   *       {@code 1.2.0}).</li>
   * </ul>
   *
   * @since 0.1.0
   */
  LOOSE,

  /**
   * npm-compatible parsing. Currently identical to {@link #LOOSE}; the
   * distinction exists so that future npm-specific quirks can be added
   * without changing existing call sites.
   *
   * @since 0.1.0
   */
  NPM
}
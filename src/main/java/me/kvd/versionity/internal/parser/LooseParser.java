package me.kvd.versionity.internal.parser;

import java.util.Objects;

import me.kvd.versionity.api.Version;
import me.kvd.versionity.api.exception.InvalidVersionException;

/**
 * Parses lenient version strings.
 *
 * <p>In addition to the strict form, this parser:</p>
 * <ul>
 *   <li>trims surrounding whitespace,</li>
 *   <li>strips leading {@code v}, {@code V} or {@code =} prefixes,</li>
 *   <li>pads missing minor and patch components with zeros.</li>
 * </ul>
 *
 * @since 0.1.0
 */
public final class LooseParser {

  private LooseParser() {
    throw new UnsupportedOperationException(
      "LooseParser is a utility class and cannot be instantiated."
    );
  }

  /**
   * Parses the given string leniently.
   *
   * @param input the input; must not be {@code null}.
   * @return the parsed version; never {@code null}.
   * @throws NullPointerException    if {@code input} is {@code null}.
   * @throws InvalidVersionException if the input cannot be normalised.
   * @since 0.1.0
   */
  public static Version parse(final String input) {
    Objects.requireNonNull(input, "input");

    var s = input.trim();
    var start = 0;
    while (start < s.length()) {
      final var c = s.charAt(start);
      if ('v' == c || 'V' == c || '=' == c) {
        start++;
        continue;
      }
      break;
    }
    s = s.substring(start).trim();

    if (s.isEmpty()) {
      throw new InvalidVersionException(
        "Version string is empty after stripping prefix: \"" + input + "\""
      );
    }

    s = padVersionCore(s);
    return StrictParser.parse(s);
  }

  /**
   * Pads a version core with implicit zero components.
   *
   * @param s the string; must not be {@code null}.
   * @return the padded string; never {@code null}.
   * @since 0.1.0
   */
  private static String padVersionCore(final String s) {
    var end = s.length();
    for (var i = 0; i < s.length(); i++) {
      final var c = s.charAt(i);
      if ('-' == c || '+' == c) {
        end = i;
        break;
      }
    }

    final var core = s.substring(0, end);
    final var suffix = s.substring(end);
    var dots = 0;
    for (var i = 0; i < core.length(); i++) {
      if ('.' == core.charAt(i)) {
        dots++;
      }
    }

    final String padded;
    if (0 == dots) {
      padded = core + ".0.0";
    } else if (1 == dots) {
      padded = core + ".0";
    } else {
      padded = core;
    }
    return padded + suffix;
  }
}
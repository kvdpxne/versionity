package me.kvd.versionity.api;

/**
 * Compares pre-release identifiers according to SemVer 2.0.0 precedence
 * rules.
 *
 * <p>Numeric identifiers are compared by length first and then
 * lexicographically. This is equivalent to a numeric comparison because
 * numeric identifiers cannot contain leading zeroes, and it never
 * overflows regardless of identifier length.</p>
 *
 * @since 0.1.0
 */
final class PreReleaseComparator {

  private PreReleaseComparator() {
    throw new UnsupportedOperationException(
      "PreReleaseComparator is a utility class and cannot be instantiated."
    );
  }

  static int compare(final String a, final String b) {
    final var aNumeric = isNumeric(a);
    final var bNumeric = isNumeric(b);

    if (aNumeric && bNumeric) {
      final var lengthResult = Integer.compare(a.length(), b.length());
      return 0 != lengthResult ? lengthResult : a.compareTo(b);
    }
    if (aNumeric) {
      return -1;
    }
    if (bNumeric) {
      return 1;
    }
    return a.compareTo(b);
  }

  private static boolean isNumeric(final String s) {
    if (s.isEmpty()) {
      return false;
    }
    for (var i = 0; i < s.length(); i++) {
      if (!Character.isDigit(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
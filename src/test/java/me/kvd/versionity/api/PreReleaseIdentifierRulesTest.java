package me.kvd.versionity.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies the rules that {@link PreReleaseComparator} implements for
 * comparing single pre-release identifiers, as defined by section 11 of the
 * SemVer 2.0.0 specification.
 */
@DisplayName("Pre-release identifier rules")
class PreReleaseIdentifierRulesTest {

  private static int cmp(final String a, final String b) {
    return PreReleaseComparator.compare(a, b);
  }

  @Nested
  @DisplayName("numeric versus non-numeric")
  class NumericVersusNonNumeric {

    @Test
    @DisplayName("numeric identifier has lower precedence than non-numeric")
    void numericBelowNonNumeric() {
      assertTrue(cmp("1", "alpha") < 0);
      assertTrue(cmp("0", "alpha") < 0);
      assertTrue(cmp("99999", "alpha") < 0);
    }

    @Test
    @DisplayName("non-numeric identifier has higher precedence than numeric")
    void nonNumericAboveNumeric() {
      assertTrue(cmp("alpha", "1") > 0);
      assertTrue(cmp("beta", "999999") > 0);
    }

    @Test
    @DisplayName("both non-numeric compare lexically")
    void bothNonNumeric() {
      assertTrue(cmp("alpha", "beta") < 0);
      assertTrue(cmp("beta", "alpha") > 0);
      assertEquals(0, cmp("alpha", "alpha"));
    }
  }

  @Nested
  @DisplayName("numeric identifiers")
  class Numeric {

    @Test
    @DisplayName("equal values compare equal")
    void equalValues() {
      assertEquals(0, cmp("0", "0"));
      assertEquals(0, cmp("42", "42"));
      assertEquals(0, cmp("1000000", "1000000"));
    }

    @Test
    @DisplayName("ordering is numeric, not lexicographic")
    void numericOrdering() {
      assertTrue(cmp("1", "2") < 0);
      assertTrue(cmp("2", "10") < 0);
      assertTrue(cmp("10", "100") < 0);
      assertTrue(cmp("100", "1000") < 0);
    }

    @Test
    @DisplayName("leading digit count decides order")
    void digitCountDecides() {
      assertTrue(cmp("9", "10") < 0);
      assertTrue(cmp("99", "100") < 0);
      assertTrue(cmp("999", "1000") < 0);
    }

    @Test
    @DisplayName("identifiers much larger than int do not overflow")
    void noOverflow() {
      assertTrue(
          cmp("99999999999999999999999999", "1") > 0
      );
      assertTrue(
          cmp("1", "99999999999999999999999999") < 0
      );
      assertEquals(
          0,
          cmp("99999999999999999999999999", "99999999999999999999999999")
      );
    }
  }

  @Nested
  @DisplayName("non-numeric identifiers")
  class NonNumeric {

    @Test
    @DisplayName("ASCII sort order, uppercase before lowercase")
    void asciiOrdering() {
      assertTrue(cmp("A", "a") < 0);
      assertTrue(cmp("Z", "a") < 0);
      assertTrue(cmp("a", "z") < 0);
    }

    @Test
    @DisplayName("hyphen is non-numeric and beats numeric identifiers")
    void hyphenBeatsNumeric() {
      // '-' is not a digit, so it counts as a non-numeric identifier.
      // Per SemVer 2.0.0, non-numeric identifiers have higher precedence
      // than numeric ones, regardless of ASCII order.
      assertTrue(cmp("-", "0") > 0);
      assertTrue(cmp("0", "-") < 0);
      assertTrue(cmp("-", "9") > 0);
    }

    @Test
    @DisplayName("hyphen sorts below letters in ASCII")
    void hyphenBelowLetters() {
      // Both sides are non-numeric, so ASCII lexicographic order applies.
      // '-' is 45, 'A' is 65, 'a' is 97.
      assertTrue(cmp("-", "A") < 0);
      assertTrue(cmp("-", "a") < 0);
      assertTrue(cmp("-Z", "A") < 0);
    }

    @Test
    @DisplayName("identifier may start with a digit and still be non-numeric")
    void startsWithDigitButNonNumeric() {
      assertTrue(cmp("1a", "1b") < 0);
      assertTrue(cmp("1a", "2") > 0); // non-numeric beats numeric
    }

    @Test
    @DisplayName("prefix ordering follows compareTo of the strings")
    void prefixOrdering() {
      assertTrue(cmp("alpha", "alphabeta") < 0);
      assertTrue(cmp("alpha", "alpha.1") < 0);
      assertTrue(cmp("alpha", "alpha-1") < 0);
    }
  }

  @Nested
  @DisplayName("mixed identifiers")
  class Mixed {

    @Test
    @DisplayName("non-numeric beats any numeric regardless of length")
    void nonNumericBeatsNumeric() {
      assertTrue(cmp("a", "99999999999999999999999999") > 0);
      assertTrue(cmp("99999999999999999999999999", "a") < 0);
    }

    @Test
    @DisplayName("hyphen-led identifier is non-numeric and beats numeric")
    void hyphenLedBeatsNumeric() {
      assertTrue(cmp("-x", "1") > 0);
      assertTrue(cmp("1", "-x") < 0);
    }
  }

  @Nested
  @DisplayName("chain from the specification")
  class SpecChain {

    @Test
    @DisplayName("alpha < beta < rc as non-numeric identifiers")
    void chain() {
      assertTrue(cmp("alpha", "beta") < 0);
      assertTrue(cmp("beta", "rc") < 0);
      assertTrue(cmp("rc", "alpha") > 0);
    }

    @Test
    @DisplayName("numeric pair 2 < 11 (not lexical)")
    void numericPairFromSpec() {
      // The spec example uses "1.0.0-beta.2 < 1.0.0-beta.11".
      assertTrue(cmp("2", "11") < 0);
    }

    @Test
    @DisplayName("length first, then lexicographic, matches numeric order")
    void lengthFirstMatchesNumeric() {
      final String[] ordered = {"1", "2", "9", "10", "11", "99", "100", "999", "1000"};
      for (int i = 0; i < ordered.length - 1; i++) {
        var a = ordered[i];
        var b = ordered[i + 1];
        assertTrue(cmp(a, b) < 0, () -> a + " < " + b);
      }
    }
  }
}
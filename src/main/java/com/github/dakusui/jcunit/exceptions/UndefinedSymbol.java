package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An exception to tell JCUnit that a set of symbols are no set in a tuple
 * to be checked if it satisfies constraints user defined.
 *
 * Right now (0.5.6), The only tuple generator that needs to capture {@code UndefinedSymbol}
 * is {@code IPO2CAEngine}. Refer to the class for more details.
 *
 * To build an object of this class, there is a utility method {@code Checks.checksymbols(...)}
 *
 * @see IPO2CoveringArrayEngine
 * @see Checks#checksymbols(Tuple, String...)
 */
public class UndefinedSymbol extends JCUnitCheckedException {
  public final List<String> missingSymbols;

  public UndefinedSymbol(String[] missingSymbols) {
    super(composeMessage(missingSymbols), null);
    this.missingSymbols = Collections.unmodifiableList(Arrays.asList(missingSymbols));
  }

  public UndefinedSymbol(String missingSymbol) {
    this(new String[]{missingSymbol});
  }

  private static String composeMessage(String... missingSymbols) {
    Checks.checknotnull(missingSymbols);
    Checks.checkcond(missingSymbols.length > 0, "There must be at least one missing symbol.");
    if (missingSymbols.length == 1) {
      return String.format("'%s' is missing.", missingSymbols[0]);
    }
    if (missingSymbols.length == 2) {
      return String.format("'%s' and '%s' are missing", missingSymbols[0], missingSymbols[1]);
    }
    List<String> missings = Arrays.asList(missingSymbols);
    String last = missingSymbols[missingSymbols.length - 1];
    return String.format(
        "%s, and %s are missing",
        StringUtils.join(", ", (Object[]) missings.subList(0, missingSymbols.length - 1).toArray(new String[missingSymbols.length - 1])),
        last
    );
  }
}

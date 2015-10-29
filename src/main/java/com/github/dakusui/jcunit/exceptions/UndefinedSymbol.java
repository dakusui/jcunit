package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        Utils.join(", ", (Object[])missings.subList(0, missingSymbols.length - 1).toArray(new String[missingSymbols.length - 1])),
        last
    );
  }
}

package com.github.jcunit.core.regex;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class CfgUtils {
  public static void main(String... args) {
    //(A){0,1}abc(B){1,2}
    seq(
        optional(terminal("A")),
        terminal("abc"),
        repeat(terminal("B"), 1, 2));
    //A(B(C){0,1}){0,1}
    seq(
        terminal("A"),
        optional(
            seq(terminal("B"),
                optional(terminal("C")))));

  }

  interface Symbol {
    default Symbol optional() {
      return CfgUtils.optional(this);
    }

    default Symbol repeat(int... numRepetitions) {
      return CfgUtils.repeat(this, numRepetitions);
    }
  }

  public static Symbol optional(Symbol s) {
    return repeat(s, 0, 1);
  }

  public static Symbol repeat(Symbol s, int... numRepetitions) {
    throw new UnsupportedOperationException();
  }

  public static Symbol alt(Symbol... symbols) {
    throw new UnsupportedOperationException();
  }

  public static Symbol seq(Symbol... symbols) {
    throw new UnsupportedOperationException();
  }

  public static Symbol terminal(Object value) {
    throw new UnsupportedOperationException();
  }

  public static Symbol nonTerminal(String value) {
    throw new UnsupportedOperationException();
  }

  public static Symbol empty() {
    throw new UnsupportedOperationException();
  }

  static class Builder {
    final Symbol[] symbols;

    private Builder(Symbol... symbols) {
      this.symbols = symbols;
    }

    Symbol optional() {
      return repeat(0, 1);
    }

    Symbol repeat(int... numRepetitions) {
      return CfgUtils.repeat(group(this.symbols), numRepetitions);
    }

    Symbol alt() {
      return CfgUtils.alt(this.symbols);
    }

    static Symbol $(Object value) {
      return terminal(value);
    }

    static Symbol $$(String value) {
      return nonTerminal(value);
    }

    static Builder $(Symbol... symbols) {
      return new Builder(symbols);
    }

    Symbol group() {
      return group(this.symbols);
    }

    static Symbol group(Symbol... symbols) {
      if (symbols.length == 0) {
        return empty();
      }
      if (symbols.length == 1) {
        return symbols[0];
      }
      return CfgUtils.seq(symbols);
    }


    public static void main(String... args) {
      //(A){0,1}abc(B){1,2}
      group($$("A").optional(), $("abc"), $$("B").repeat(1, 2));
      //A(B(C){0,1}){0,1}
      group($$("A"), group($$("B"), $$("C").optional()).optional());
    }
  }
}

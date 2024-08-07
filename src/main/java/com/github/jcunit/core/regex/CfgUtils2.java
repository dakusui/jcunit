package com.github.jcunit.core.regex;

import com.github.valid8j.pcond.forms.Predicates;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class CfgUtils2 {
  interface Element {
    default Element quantifier(int... numRepetitions) {
      throw new UnsupportedOperationException("Not implemented yet");
    }

    default Element optional() {
      return quantifier(0, 1);
    }
  }

  interface Symbol extends Element {
  }

  interface Sequence extends Element {
  }

  interface Alteration extends Element {
  }

  interface Empty extends Element {
  }

  static Symbol symbol(String symbol) {
    throw new UnsupportedOperationException();
  }

  static Sequence sequence(Element... elements) {
    throw new UnsupportedOperationException();
  }

  static Alteration alteration(Element... elements) {
    throw new UnsupportedOperationException();
  }

  static Alteration alt(Element... elements) {
    return alteration(elements);
  }

  static Sequence seq(Element... elements) {
    return sequence(elements);
  }

  static Symbol $(String terminalSymbol) {
    return symbol(terminalSymbol);
  }

  static Empty empty() {
    return new Empty() {
    };
  }


  interface ContextFreeGrammar {
    String startSymbol();

    Map<String, Element> productionRules();

    boolean isTerminalSymbol(String token);

    class Builder {
      Map<String, Element> productionRules = new LinkedHashMap<>();
      String startSymbol = null;
      private Predicate<String> isTerminalSymbolPredicate;

      public Builder () {
         this.predicateTokenIsTerminalSymbol(Predicates.alwaysTrue());
      }
      public Builder productionRule(String symbol, Element element) {
        requireNonNull(symbol);
        requireNonNull(element);
        Builder ret = this;
        if (this.startSymbol == null) {
          ret = ret.startSymbol(symbol);
        }
        ret.productionRules.put(symbol, element);
        return ret;
      }

      public Builder startSymbol(String symbol) {
        this.startSymbol = requireNonNull(symbol);
        return this;
      }

      public Builder predicateTokenIsTerminalSymbol(Predicate<String> predicate) {
        this.isTerminalSymbolPredicate = requireNonNull(predicate);
        return this;
      }

      public ContextFreeGrammar build() {
        return new ContextFreeGrammar() {
          final String startSymbol = Builder.this.startSymbol;
          final Map<String, Element> productionRules = Collections.unmodifiableMap(new LinkedHashMap<>(Builder.this.productionRules));
          final Predicate<String> isTerminalSymbolPredicate = Builder.this.isTerminalSymbolPredicate;

          @Override
          public String startSymbol() {
            return this.startSymbol;
          }

          @Override
          public Map<String, Element> productionRules() {
            return productionRules;
          }

          @Override
          public boolean isTerminalSymbol(String token) {
            return this.isTerminalSymbolPredicate.test(token);
          }
        };
      }
    }
  }

  public static void main(String... args) {
    //(A){0,1}abc(B){1,2}
    seq($("A").optional(),
        $("abc"),
        $("B").quantifier(1, 2));
    //A(B(C){0,1}){0,1}
    seq($("A"),
        seq($("B"),
            $("C").optional()).optional());
    // ((A{0,1})|B|(C{0,1}))
    alt($("A").optional(),
        $("B"),
        $("C").optional());

    ContextFreeGrammar cfg = new ContextFreeGrammar.Builder().productionRule("Statement",
                                                                             alt(empty(),
                                                                                 seq($("("),
                                                                                     $("Statement"),
                                                                                     $("("))))
                                                             .build();
    System.out.println(cfg);
  }
}

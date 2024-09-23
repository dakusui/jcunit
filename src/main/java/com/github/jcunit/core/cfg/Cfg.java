package com.github.jcunit.core.cfg;

import com.github.jcunit.factorspace.FactorSpace;

import static java.util.Arrays.asList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface Cfg {
  void startSymbol();

  FactorSpace toFactorSpace();

  static Alternation alteration(Element... elements) {
    return () -> asList(elements);
  }

  static Concatenation concatenation(Element... elements) {
    return () -> asList(elements);
  }

  class Impl implements Cfg {

    @Override
    public void startSymbol() {

    }

    @Override
    public FactorSpace toFactorSpace() {
      throw new UnsupportedOperationException();
    }
  }

  class Builder {
    public Builder add(String head, Element expansion) {
      return this;
    }

    public Cfg build() {
      return new Cfg.Impl();
    }
  }
}

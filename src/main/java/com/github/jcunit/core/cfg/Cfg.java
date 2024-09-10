package com.github.jcunit.core.cfg;

import com.github.jcunit.factorspace.FactorSpace;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface Cfg {
  void startSymbol();

  FactorSpace toFactorSpace();

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
    public Builder add(String head, Element expansion, boolean exit) {
      return this;
    }

    public Cfg build() {
      return new Cfg.Impl();
    }
  }
}

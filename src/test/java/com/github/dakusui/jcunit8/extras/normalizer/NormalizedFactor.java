package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.factorspace.Factor;

interface NormalizedFactor extends Factor {
  FactorType type();

  enum FactorType {
    NUMBER(0),
    ENUM(1),
    BOOL(2),
    ;

    private final int type;

    FactorType(int type) {
      this.type = type;
    }
  }
}

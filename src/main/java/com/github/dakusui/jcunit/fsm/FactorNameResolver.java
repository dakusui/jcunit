package com.github.dakusui.jcunit.fsm;

public interface FactorNameResolver {
  int historyLength();

  String stateFactorName(int i);

  String actionFactorName(int i);

  int numParamFactors(int i);

  String paramFactorName(int i, int j);
}

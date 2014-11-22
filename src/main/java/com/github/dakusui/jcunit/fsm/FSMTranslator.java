package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public class FSMTranslator<SUT> {
  private final Tuple    tuple;
  private final FactorNameResolver resolver;

  public FSMTranslator(Tuple tuple, FactorNameResolver resolver) {
    Checks.checknotnull(tuple);
    Checks.checknotnull(resolver);
    this.tuple = tuple;
    this.resolver = resolver;
  }

  public State<SUT> state(int i) {
    Checks.checkcond(i >= 0);
    Checks.checkcond(i < this.resolver.size());
    return (State<SUT>) this.tuple.get(this.resolver.stateFactorName(i));
  }

  public Action<SUT> action(int i) {
    Checks.checkcond(i >= 0);
    Checks.checkcond(i < this.resolver.size());
    return (Action<SUT>) this.tuple.get(this.resolver.actionFactorName(i));
  }

  public Args args(int i) {
    Checks.checkcond(i >= 0);
    Checks.checkcond(i < this.resolver.size());
    Object[] values = new Object[this.resolver.numParamFactors(i)];
    for (int j = 0; j < values.length; j++) {
      values[j] = this.tuple.get(this.resolver.paramFactorName(i, j));
    }
    return new Args(values);
  }
}

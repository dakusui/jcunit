package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

class ScenarioTupleGenerator<SUT> extends TupleGeneratorBase {
  private final FSM<SUT> fsm;
  private FactorNameResolver resolver;

  ScenarioTupleGenerator(FSM<SUT> fsm) {
    this.fsm = fsm;
  }

  @Override
  public Tuple getTuple(int tupleId) {
    return null;
  }

  @Override
  protected long initializeTuples(Object[] params) {
    return 0;
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }

  static class FSMCM<SUT> extends ConstraintManagerBase {
    @Override public boolean check(Tuple tuple) throws UndefinedSymbol {
      return false;
    }
  }

  State<SUT> getState(Tuple tuple, int i) {
    Checks.checknotnull(tuple);
    Checks.checkcond(i >= 0);
    return (State<SUT>) tuple.get(this.resolver.stateFactorName(i));
  }

  Action<SUT> getAction(Tuple tuple, int i) {
    Checks.checknotnull(tuple);
    Checks.checkcond(i >= 0);
    return (Action<SUT>) tuple.get(this.resolver.actionFactorName(i));
  }

  Args getArgs(Tuple tuple, int i) {
    return null;
  }
}

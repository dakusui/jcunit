package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

public abstract class ScenarioTupleGenerator<SUT> extends TupleGeneratorBase {
  @Override
  public Tuple getTuple(int tupleId) {
    return null;
  }

  @Override
  protected long initializeTuples(Object[] params) {
    int historyLength = (Integer)params[0];
    int strength = (Integer)params[1];
    FSMFactors factors = new FSMFactors.Builder<SUT>()
        .setFSM(createFSM())
        .setLength(historyLength)
        .build();
    ConstraintManager cm = new FSMConstraintManager<SUT>();
    cm.setFactors(factors);
    TupleGenerator tupleGenerator = new Builder()
        .setConstraintManager(cm)
        .setFactors(factors)
        .setParameters(new Object[]{strength})
        .setTargetClass(this.getTargetClass())
        .setTupleGeneratorClass(IPO2TupleGenerator.class)
        .build();
    tupleGenerator.init(params);
    return this.size();
  }

  protected abstract FSM<SUT> createFSM();

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[]{
        ParamType.Int.withDefaultValue(2),
        ParamType.Int.withDefaultValue(2)
    };
  }
}

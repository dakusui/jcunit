package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.LinkedList;
import java.util.List;

public abstract class ScenarioTupleGenerator<SUT> extends TupleGeneratorBase {
  private List<Tuple> tuples;

  @Override
  public Tuple getTuple(int tupleId) {
    return tuples.get(tupleId);
  }

  @Override
  protected long initializeTuples(Object[] params) {
    int historyLength = (Integer) params[0];
    int strength = (Integer) params[1];
    FSM<SUT> fsm = createFSM();
    FSMFactors factors = new FSMFactors.Builder<SUT>()
        .setFSM(fsm)
        .setLength(historyLength)
        .build();
    ConstraintManager cm = new FSMConstraintManager<SUT>();
    cm.setFactors(factors);
    TupleGenerator tupleGenerator = new Builder()
        .setConstraintManager(cm)
        .setFactors(factors)
        .setParameters(new Object[] { strength })
        .setTargetClass(this.getTargetClass())
        .setTupleGeneratorClass(IPO2TupleGenerator.class)
        .build();
    tupleGenerator.init(params);
    String mainScenarioFactorName = this.mainScenarioFactorName();
    String setUpScenarioFactorName = this.setUpScenarioFactorName();
    Checks.checkplugin(mainScenarioFactorName != null,
        "mainScenarioFactorName() must not return null.");
    Checks.checkplugin(setUpScenarioFactorName != null,
        "setUpScenarioFactorName() must not return null.");
    this.tuples = new LinkedList<Tuple>();
    for (Tuple each : tupleGenerator) {
      Tuple.Builder b = new Tuple.Builder();
      ScenarioSequence main = new ScenarioSequence.Builder<SUT>()
          .setFSMFactors(factors)
          .setTuple(each)
          .build();
      b.put(mainScenarioFactorName, main);
      ScenarioSequence<SUT> setUp = composeSetUpScenario(main, fsm);
      if (setUp != null) {
        b.put(setUpScenarioFactorName, setUp);
      }
      this.tuples.add(b.build());
    }
    return this.tuples.size();
  }

  private ScenarioSequence<SUT> composeSetUpScenario(
      ScenarioSequence<SUT> main, FSM<SUT> fsm) {
    if (fsm.initialState().equals(main.state(0))) {
      return (ScenarioSequence<SUT>) ScenarioSequence.EMPTY;
    }
    return null; /* TODO */
  }

  protected abstract FSM<SUT> createFSM();

  protected abstract String mainScenarioFactorName();

  protected abstract String setUpScenarioFactorName();

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] {
        ParamType.Int.withDefaultValue(2),
        ParamType.Int.withDefaultValue(2)
    };
  }
}

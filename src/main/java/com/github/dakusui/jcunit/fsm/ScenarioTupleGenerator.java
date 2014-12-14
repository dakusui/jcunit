package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.TupleGeneration;
import com.github.dakusui.jcunit.core.factor.Factors;
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
    Factors baseFactors = this.getFactors();
    ConstraintManager baseCM = this.getConstraintManager();

    int historyLength = (Integer) params[0];
    int strength = (Integer) params[1];
    FSM<SUT> fsm = createFSM();
    FSMFactors factors = new FSMFactors.Builder<SUT>()
            .setFSM(fsm)
            .setLength(historyLength)
            .setBaseFactors(baseFactors)
            .build();
    ConstraintManager cm = new FSMConstraintManager<SUT>(baseCM);
    cm.setFactors(factors);
    TupleGenerator tupleGenerator = new Builder()
            .setConstraintManager(cm)
            .setFactors(factors)
            .setParameters(new Object[]{strength})
            .setTargetClass(this.getTargetClass())
            .setTupleGeneratorClass(IPO2TupleGenerator.class)
            .build();
    tupleGenerator.init(params);
    final List<ScenarioSequence<SUT>> mainScenarios = new LinkedList<ScenarioSequence<SUT>>();
    for (Tuple each : tupleGenerator) {
      ScenarioSequence<SUT> main = new ScenarioSequence.BuilderFromTuple<SUT>()
              .setFSMFactors(factors)
              .setTuple(each)
              .build();
      mainScenarios.add(main);
    }
    ////
    // Create a state router.
    List<State<SUT>> destinations = new LinkedList<State<SUT>>();
    StateRouter<SUT> router = new StateRouter<SUT>(fsm, destinations) {
      @Override
      protected List<Transition> possibleTransitionsFrom(State<SUT> state) {
        List<Transition> ret = new LinkedList<Transition>();
        for (ScenarioSequence<SUT> eachScenario : mainScenarios) {
          for (int i = 0; i < eachScenario.size(); i++) {
            Scenario<SUT> each = eachScenario.get(i);
            if (each.given.equals(state) && !each.then().state.equals(State.VOID)) {
              ret.add(new Transition<SUT>(eachScenario.action(i), eachScenario.args(i)));
            }
          }
        }
        return ret;
      }
    };
    ////
    // Build the final tuples
    String mainScenarioFactorName = this.mainScenarioFactorName();
    String setUpScenarioFactorName = this.setUpScenarioFactorName();
    Checks.checkplugin(mainScenarioFactorName != null,
            "mainScenarioFactorName() must not return null.");
    Checks.checkplugin(setUpScenarioFactorName != null,
            "setUpScenarioFactorName() must not return null.");
    this.tuples = new LinkedList<Tuple>();
    for (ScenarioSequence<SUT> each : mainScenarios) {
      Tuple.Builder b = new Tuple.Builder();
      b.put(mainScenarioFactorName, each);
      ScenarioSequence<SUT> setUp = router.routeTo(each.state(0));
      if (setUp != null) {
        b.put(setUpScenarioFactorName, setUp);
      }
      this.tuples.add(b.build());
    }
    return this.tuples.size();
  }

  protected abstract FSM<SUT> createFSM();

  protected String mainScenarioFactorName() {
    return "FSM:main";
  }

  protected String setUpScenarioFactorName() {
    return "FSM:setUp";
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[]{};
  }

  protected TupleGenerator createTupleGenerator(Factors baseFactors) {
    return null;
  }

  protected ConstraintManager createConstraintManager(Factors baseFactors) {
    return new ConstraintManager.Builder()
        .setFactors(baseFactors)
        .setConstraintManagerClass(getConstraintManagerClass())
        .setParameters(getConstraintManagerParams())
        .build();
  }

  protected Class<? extends TupleGenerator> getTupleGeneratorClass() {
    TupleGeneration ann = this.getClass().getAnnotation(TupleGeneration.class);
    return ann.generator().value();
  }

  protected Param[] getTupleGeneratorParams() {
    TupleGeneration ann = this.getClass().getAnnotation(TupleGeneration.class);
    return ann.generator().params();
  }

  protected Class<? extends ConstraintManager> getConstraintManagerClass() {
    TupleGeneration ann = this.getClass().getAnnotation(TupleGeneration.class);
    return ann.constraint().value();
  }

  protected Param[] getConstraintManagerParams() {
    TupleGeneration ann = this.getClass().getAnnotation(TupleGeneration.class);
    return ann.constraint().params();
  }

}

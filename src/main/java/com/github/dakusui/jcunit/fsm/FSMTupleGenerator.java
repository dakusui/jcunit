package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorMapper;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.LinkedList;
import java.util.List;

public class FSMTupleGenerator<SUT> extends TupleGeneratorBase {
  private final FSM<SUT>               fsm;
  private final TupleGenerator.Builder baseTupleGeneratorBuilder;
  private final List<FactorMapper<?>>  factorMappers;
  private       List<Tuple>            tuples;
  private       String                 fsmName;

  public FSMTupleGenerator(TupleGenerator.Builder baseTG, FSM<SUT> fsm, String fsmName, List<FactorMapper<?>> factorMappers) {
    this.fsm = Checks.checknotnull(fsm);
    this.baseTupleGeneratorBuilder = Checks.checknotnull(baseTG);
    this.fsmName = Checks.checknotnull(fsmName);
    this.factorMappers = factorMappers;
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = baseTupleGeneratorBuilder.getFactors();
    int historyLength = (Integer) params[0];
    FSMFactors fsmFactors = new FSMFactors.Builder<SUT>()
        .setFSM(fsm)
        .setLength(historyLength)
        .setBaseFactors(baseFactors)
        .build();

    ConstraintManager fsmCM = new FSMConstraintManager<SUT>(this.baseTupleGeneratorBuilder.getConstraintManager());
    fsmCM.setFactors(fsmFactors);
    final List<ScenarioSequence<SUT>> mainScenarios = new LinkedList<ScenarioSequence<SUT>>();
    for (Tuple each : new TupleGenerator.Builder(this.baseTupleGeneratorBuilder).setConstraintManager(fsmCM).setFactors(fsmFactors).build()) {
      ScenarioSequence<SUT> main = new ScenarioSequence.BuilderFromTuple<SUT>()
          .setFSMFactors(fsmFactors)
          .setTuple(each)
          .build();
      mainScenarios.add(main);
    }

    ////
    // Create a state router.
    List<State<SUT>> destinations = new LinkedList<State<SUT>>();
    for (ScenarioSequence<SUT> each : mainScenarios) {
      if (each.size() > 0) {
        destinations.add(each.get(0).given);
      }
    }
    StateRouter<SUT> router = new StateRouter<SUT>(fsm, destinations) {
      @Override
      protected List<Transition> possibleTransitionsFrom(State<SUT> state) {
        List<Transition> ret = new LinkedList<Transition>();
        for (ScenarioSequence<SUT> eachScenario : mainScenarios) {
          for (int i = 0; i < eachScenario.size(); i++) {
            Scenario<SUT> each = eachScenario.get(i);
            if (each.given.equals(state) && !each.then().state
                .equals(State.VOID)) {
              ret.add(new Transition<SUT>(eachScenario.action(i),
                  eachScenario.args(i)));
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
      this.tuples.add(translateFSMTupleToNormalTuple(b.build()));
    }
    return this.tuples.size();
  }

  private Tuple translateFSMTupleToNormalTuple(Tuple fsmTuple) {
    Tuple.Builder b = new Tuple.Builder();
    for (Factor each : this.baseTupleGeneratorBuilder.getFactors()) {
      b.put(each.name, fsmTuple.get(each.name));
    }
    for (FactorMapper<?> each : this.factorMappers) {
      b.put(each.factorName(), each.apply(fsmTuple));
    }
    return b.build();
  }

  protected String mainScenarioFactorName() {
    return FSMUtils.composeMainScenarioName(this.fsmName);
  }

  protected String setUpScenarioFactorName() {
    return FSMUtils.composeSetUpScenarioName(this.fsmName);
  }

  @Override
  public Tuple getTuple(int tupleId) {
    return tuples.get(tupleId);
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] {
        /* Length of FSM history */
        ParamType.Int.withDefaultValue(2)
    };
  }
}

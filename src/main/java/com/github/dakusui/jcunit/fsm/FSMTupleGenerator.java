package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.LinkedList;
import java.util.List;

public class FSMTupleGenerator<SUT> extends TupleGeneratorBase {
  private final FSM<SUT>       fsm;
  final private TupleGenerator baseTG;
  private       List<Tuple>    tuples;
  private       String         fsmName;

  public FSMTupleGenerator(TupleGenerator baseTG, FSM<SUT> fsm, String fsmName) {
    this.fsm = fsm;
    this.baseTG = baseTG;
    this.fsmName = fsmName;
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = this.getFactors();
    int historyLength = (Integer) params[0];
    FSMFactors fsmFactors = new FSMFactors.Builder<SUT>()
        .setFSM(fsm)
        .setLength(historyLength)
        .setBaseFactors(baseFactors)
        .build();

    ConstraintManager fsmCM = new FSMConstraintManager<SUT>(this.baseTG.getConstraintManager());
    fsmCM.setFactors(fsmFactors);
    final List<ScenarioSequence<SUT>> mainScenarios = new LinkedList<ScenarioSequence<SUT>>();
    for (Tuple each : new TupleGenerator.Builder(this.baseTG).setConstraintManager(fsmCM).setFactors(fsmFactors).build()) {
      ScenarioSequence<SUT> main = new ScenarioSequence.BuilderFromTuple<SUT>()
          .setFSMFactors(fsmFactors)
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
      this.tuples.add(b.build());
    }
    return this.tuples.size();
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

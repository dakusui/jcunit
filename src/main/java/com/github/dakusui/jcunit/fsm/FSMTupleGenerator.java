package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class FSMTupleGenerator<SUT> extends TupleGeneratorBase {
  private List<Tuple> tuples;

  @Override
  public Tuple getTuple(int tupleId) {
    return tuples.get(tupleId);
  }

  @Override
  public void init(Param[] params) {
    List<Object> processedParams = new LinkedList<Object>();
    if (params.length > 0) {
      processedParams.add(processParam(ParamType.Int, params[0]));
      if (params.length > 1) {
        processedParams
            .add(processParam(ParamType.TupleGeneratorClass, params[1]));
        if (params.length > 2) {
          processedParams.addAll(
              Arrays.asList(params).subList(2, params.length));
        }
      } else {
        processedParams.add(IPO2TupleGenerator.class);
      }
    } else {
      processedParams.add(2);
    }
    this.init(processedParams.toArray());
  }

  Object processParam(ParamType paramType, Param param) {
    return ParamType
        .processParams(new ParamType[] { paramType }, new Param[] { param })[0];
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = this.getFactors();
    ConstraintManager baseCM = this.getConstraintManager();

    int historyLength = (Integer) params[0];
    Class<? extends TupleGenerator> childTupleGeneratorClass = (Class<? extends TupleGenerator>) params[1];
    Param[] paramsForChild = new Param[params.length - 2];
    //noinspection SuspiciousSystemArraycopy
    System.arraycopy(params, 2, paramsForChild, 0, paramsForChild.length);
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
        .setParameters(paramsForChild)
        .setTargetClass(this.getTargetClass())
        .setTupleGeneratorClass(childTupleGeneratorClass)
        .build();
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

  protected abstract FSM<SUT> createFSM();

  protected String mainScenarioFactorName() {
    return "FSM:main";
  }

  protected String setUpScenarioFactorName() {
    return "FSM:setUp";
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] {
        ParamType.Int, /* Length of FSM history */
        ParamType.TupleGeneratorClass
            .withDefaultValue(IPO2TupleGenerator.class), /* The underlying tuple generator FQCN */
    };
  }
}

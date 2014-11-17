package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.reflect.Field;
import java.util.*;

public abstract class ScenarioProvider<SUT> extends LevelsProviderBase<ScenarioSequence<SUT>> {
  private List<ScenarioSequence> scenarioSequences;

  @Override
  protected void init(Field targetField, FactorField annotation,
      Object[] parameters) {
    TupleGenerator generator = createTupleGenerator(targetField, annotation,
        parameters, 3);
    List<ScenarioSequence<SUT>> partialScenarioSequences = new ArrayList<ScenarioSequence<SUT>>();
    for (long i = 0; i < generator.size(); i++) {
      Tuple tuple = generator.get(i);
      partialScenarioSequences.add(
          createPartialScenarioSequenceFromTuple(tuple));
    }
    this.scenarioSequences = organizeScenarioSequences(partialScenarioSequences);
  }

  private List<ScenarioSequence> organizeScenarioSequences(
      List<ScenarioSequence<SUT>> partialScenarioSequences) {
    return null;
  }

  private ScenarioSequence<SUT> createPartialScenarioSequenceFromTuple(
      Tuple tuple) {
    return null;
  }

  @Override
  public int size() {
    return this.scenarioSequences.size();
  }

  @Override
  public ScenarioSequence<SUT> get(int n) {
    return this.scenarioSequences.get(n);
  }

  private TupleGenerator createTupleGenerator(Field targetField,
      FactorField annotation,
      Object[] parameters, int historySize) {
    Class<? extends TupleGenerator> tupleGeneratorClass = IPO2TupleGenerator.class;
    ConstraintManager constraintManager = createConstraintManager();
    TupleGenerator ret = new TupleGenerator.Builder()
        .setTupleGeneratorClass(tupleGeneratorClass)
        .setFactors(loadFactors(createFSM(), historySize()))
        .setConstraintManager(constraintManager)
        .setParameters(parameters)
        .build();
    return ret;
  }

  private void findRoutes(List<ScenarioSequence<SUT>> routes, FSM<SUT> fsm, State<SUT> from, State<SUT> to) {
    for (State each : fsm.states()) {

    }
  }

  protected ConstraintManager createConstraintManager() {
    return new ConstraintManagerBase() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        return false;
      }
    };
  }

  protected abstract FSM createFSM();

  protected abstract int historySize();


  private Factors loadFactors(FSM fsm, int historySize) {
    Factors.Builder b = new Factors.Builder();
    for (int i = 0; i < historySize; i++) {
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(stateName(i));
        for (State each : fsm.states()) {
          bb.addLevel(each);
        }
        b.add(bb.build());
      }
      LinkedHashSet<Param> allParams = new LinkedHashSet<Param>();
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(actionName(i));
        for (Action each : fsm.actions()) {
          bb.addLevel(each);
          allParams.addAll(Arrays.asList(each.params()));
        }
        b.add(bb.build());
      }
      {
        for (Param each : allParams) {
          b.add(each);
        }
      }
    }
    return b.build();
  }

  private String stateName(int i) {
    return String.format("s%d", i);
  }

  private String actionName(int i) {
    return String.format("action%d", i);
  }

  private String argsName(int i) {
    return String.format("args%d", i);
  }
}

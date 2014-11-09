package com.github.dakusui.jcunit.experimentals.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

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
    Class<? extends TupleGenerator> tupleGeneratorClass = null;
    ConstraintManager constraintManager = null;
    return new TupleGenerator.Builder()
        .setConstraintManager(constraintManager)
        .setFactors(loadFactors(createFSM(), historySize()))
        .setParameters(parameters)
        .build();
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
      LinkedHashSet<Args> allArgs = new LinkedHashSet<Args>();
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(actionName(i));
        for (Action each : fsm.actions()) {
          bb.addLevel(each);
          allArgs.addAll(Arrays.asList(each.args()));
        }
        b.add(bb.build());
      }
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(argsName(i));
        for (Args each : allArgs) {
          bb.addLevel(each);
        }
        b.add(bb.build());
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

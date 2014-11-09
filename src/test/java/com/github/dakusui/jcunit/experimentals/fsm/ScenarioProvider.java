package com.github.dakusui.jcunit.experimentals.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;

public abstract class ScenarioProvider<SUT> extends LevelsProviderBase<ScenarioSequence<SUT>> {
  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {
    TupleGenerator generator = createTupleGenerator(targetField, annotation, parameters, 3);
    for (long i = 0; i < generator.size(); i++) {
      Tuple tuple = generator.get(i);
    }
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public ScenarioSequence<SUT> get(int n) {
    return null;
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

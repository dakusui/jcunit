package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorMapper;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.*;

public class FSMTupleGenerator extends TupleGeneratorBase {
  private final Map<String, FSM<?>>    fsms;
  private final TupleGenerator.Builder baseTupleGeneratorBuilder;
  private final List<FactorMapper<?>>  factorMappers;
  private       List<Tuple>            tuples;

  public FSMTupleGenerator(TupleGenerator.Builder baseTG, Map<String, FSM<?>> fsms, List<FactorMapper<?>> factorMappers) {
    this.fsms = Checks.checknotnull(fsms);
    this.baseTupleGeneratorBuilder = Checks.checknotnull(baseTG);
    this.factorMappers = factorMappers;
  }

  private static FSMFactors buildFSMFactors(Factors baseFactors, Map<String, FSM<?>> fsms) {
    FSMFactors.Builder b = new FSMFactors.Builder();
    for (FSM<?> each : fsms.values()) {
      b.addFSM(each);
    }
    return b.setBaseFactors(baseFactors).build();
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = baseTupleGeneratorBuilder.getFactors();
    FSMFactors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    ConstraintManager fsmCM = new FSMConstraintManager(this.baseTupleGeneratorBuilder.getConstraintManager());
    fsmCM.setFactors(fsmFactors);
    final List<Map<String, ScenarioSequence<?>>> mainScenarios = new LinkedList<Map<String, ScenarioSequence<?>>>();
    for (Tuple eachTuple : new TupleGenerator.Builder(this.baseTupleGeneratorBuilder).setConstraintManager(fsmCM).setFactors(fsmFactors).build()) {
      Map<String, ScenarioSequence<?>> cur = new LinkedHashMap<String, ScenarioSequence<?>>();
      for (Map.Entry<String, FSM<?>> entry : this.fsms.entrySet()) {
        FSM<?> eachFSM = entry.getValue();
        ScenarioSequence<?> main = new ScenarioSequence.BuilderFromTuple()
            .setFSMFactors(fsmFactors)
            .setTuple(eachTuple)
            .setFSMName(eachFSM.name())
            .build();
        cur.put(entry.getKey(), main);
      }
      mainScenarios.add(cur);
    }

    ////
    // Create a state router.
    Map<String, List<State<?>>> destinations = new LinkedHashMap<String, List<State<?>>>();
    for (Map<String, ScenarioSequence<?>> each : mainScenarios) {
      for (Map.Entry<String, ScenarioSequence<?>> ent : each.entrySet()) {
        List<State<?>> cur = destinations.get(ent.getKey());
        if (cur == null) {
          cur = new LinkedList<State<?>>();
          destinations.put(ent.getKey(), cur);
        }
        if (ent.getValue().size() > 0) {
          cur.add(ent.getValue().get(0).given);
        }
      }
    }
    final Map<String, Factor.Builder> mappedFactors = new LinkedHashMap<String, Factor.Builder>();
    for (final String fsmName : this.fsms.keySet()) {
      final FSM<?> fsm = this.fsms.get(fsmName);
      StateRouter router = new StateRouter(fsm, destinations.get(fsmName)) {
        @Override
        protected List<Transition> possibleTransitionsFrom(State state) {
          List<Transition> ret = new LinkedList<Transition>();
          for (ScenarioSequence<?> eachScenario : collectScenarioSequences(mainScenarios, fsmName)) {
            for (int i = 0; i < eachScenario.size(); i++) {
              Scenario each = eachScenario.get(i);
              if (each.given.equals(state) && !each.then().state
                  .equals(State.VOID)) {
                Transition t = new Transition(eachScenario.action(i),
                    eachScenario.args(i));
                if (!ret.contains(t))
                  ret.add(t);
              }
            }
          }
          return ret;
        }
      };
      ////
      // Build the final tuples
      String mainScenarioFactorName = FSMUtils.composeMainScenarioName(fsmName);
      String setUpScenarioFactorName = FSMUtils.composeSetUpScenarioName(fsmName);
      this.tuples = new LinkedList<Tuple>();
      for (ScenarioSequence<?> each : collectScenarioSequences(mainScenarios, fsmName)) {
        Tuple.Builder b = new Tuple.Builder();
        b.put(mainScenarioFactorName, each);
        ScenarioSequence<?> setUp = router.routeTo(each.state(0));
        if (setUp != null) {
          b.put(setUpScenarioFactorName, setUp);
        }
        this.tuples.add(translateFSMTupleToNormalTuple(b.build(), mappedFactors));
      }
    }
    super.setFactors(buildFactors(baseFactors, mappedFactors));
    ////
    // Constraint manager is used for negative tests generation, which is not supported yet.
    // This time I'm setting DEFAULT_CONSTRAINT_MANAGER.
    super.setConstraintManager(ConstraintManager.DEFAULT_CONSTRAINT_MANAGER);
    return this.tuples.size();
  }

  private static List<ScenarioSequence<?>> collectScenarioSequences(List<Map<String, ScenarioSequence<?>>> scenarioSeqs, String fsmName) {
    List<ScenarioSequence<?>> ret = new ArrayList<ScenarioSequence<?>>(scenarioSeqs.size());
    for (Map<String, ScenarioSequence<?>> each : scenarioSeqs) {
      ret.add(each.get(fsmName));
    }
    return ret;
  }
  private Factors buildFactors(Factors baseFactors, final Map<String, Factor.Builder> mappedFactors) {
    Factors.Builder fb = new Factors.Builder(baseFactors.asFactorList());
    List<Factor> factors = fb.getFactors();
    //noinspection unchecked
    List<Factor> matched = (List<Factor>) Utils.filter(factors, new Utils.Predicate<Factor>() {
      @Override
      public boolean apply(Factor in) {
        for (Factor.Builder each : mappedFactors.values()) {
          if (each.getName().equals(in.name))
            return true;
        }
        return false;
      }
    });
    if (!matched.isEmpty()) {
      for (Factor toBeRemoved : matched)
        factors.remove(toBeRemoved);
    }
    for (final Factor.Builder each : mappedFactors.values()) {
      fb.add(each.build());
    }
    return fb.build();
  }

  private Tuple translateFSMTupleToNormalTuple(Tuple fsmTuple, Map<String, Factor.Builder> mappedValues) {
    Tuple.Builder b = new Tuple.Builder();
    for (Factor each : this.baseTupleGeneratorBuilder.getFactors()) {
      b.put(each.name, fsmTuple.get(each.name));
    }
    for (FactorMapper<?> each : this.factorMappers) {
      String name = each.factorName();
      Object v = each.apply(fsmTuple);
      b.put(name, v);

      Factor.Builder bb;
      if (mappedValues.containsKey(name))
        bb = mappedValues.get(name);
      else {
        bb = new Factor.Builder();
        bb.setName(name);
        mappedValues.put(name, bb);
      }
      bb.addLevel(v);
    }
    return b.build();
  }

  @Override
  public Tuple getTuple(int tupleId) {
    return tuples.get(tupleId);
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] { };
  }
}

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

/**
 * Hello world
 *
 * @link http://www.google.com
 */
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
    for (Map.Entry<String, FSM<?>> each : fsms.entrySet()) {
      b.addFSM(each.getKey(), each.getValue());
    }
    return b.setBaseFactors(baseFactors).build();
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = baseTupleGeneratorBuilder.getFactors();
    FSMFactors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    ConstraintManager fsmCM = new FSMConstraintManager(this.baseTupleGeneratorBuilder.getConstraintManager());
    fsmCM.setFactors(fsmFactors);
    final List<Map<String, ScenarioSequence<?>>> mainStories = generateMainStories(fsmFactors, fsmCM);

    ////
    // Create a state router.
    // 1. Build a map from state name to a state which can be seen in the first place of a story.
    Map<String, List<State<?>>> setUpDestinations = collectSetupDestinations(mainStories);
    Map<String, StateRouter<?>> stateRouters = prepareStateRouters(mainStories, setUpDestinations);
    ////
    // Build the final tuples
    // all the possible levels will be accumulated to this variable 'mappedFactors' through 'buildTuples' process.
    final Map<String, Factor.Builder> mappedFactors = new LinkedHashMap<String, Factor.Builder>();
    this.tuples = buildTuples(this.fsms, mainStories, stateRouters, mappedFactors);
    super.setFactors(buildFactors(baseFactors, mappedFactors));
    ////
    // Constraint manager is used for negative tests generation, which is not supported yet.
    // This time I'm setting DEFAULT_CONSTRAINT_MANAGER.
    super.setConstraintManager(ConstraintManager.DEFAULT_CONSTRAINT_MANAGER);
    return this.tuples.size();
  }

  private Map<String, StateRouter<?>> prepareStateRouters(final List<Map<String, ScenarioSequence<?>>> mainStories, final Map<String, List<State<?>>> setUpDestinations) {
    Map<String, StateRouter<?>> routerMap = new HashMap<String, StateRouter<?>>();
    for (final String fsmName : this.fsms.keySet()) {
      final FSM<?> fsm = this.fsms.get(fsmName);
      //noinspection unchecked
      StateRouter<?> router = new StateRouter(fsm, setUpDestinations.get(fsmName)) {
        @Override
        protected List<Transition> possibleTransitionsFrom(State state) {
          List<Transition> ret = new LinkedList<Transition>();
          for (ScenarioSequence<?> eachScenario : collectStoriesForFSM(mainStories, fsmName)) {
            for (int i = 0; i < eachScenario.size(); i++) {
              Scenario each = eachScenario.get(i);
              if (each.given.equals(state) && !each.then().state
                  .equals(State.VOID)) {
                //noinspection unchecked
                Transition<?> t = new Transition(eachScenario.action(i),
                    eachScenario.args(i));
                if (!ret.contains(t))
                  ret.add(t);
              }
            }
          }
          return ret;
        }
      };
      routerMap.put(fsmName, router);
    }
    return routerMap;
  }

  private List<Tuple> buildTuples(Map<String, FSM<?>> fsms, List<Map<String, ScenarioSequence<?>>> mainStories, Map<String, StateRouter<?>> routerMap, Map<String, Factor.Builder> mappedFactors) {
    List<Tuple> tuples = new LinkedList<Tuple>();
    for (Map<String, ScenarioSequence<?>> each : mainStories) {
      Tuple.Builder b = new Tuple.Builder();
      for (String fsmName : fsms.keySet()) {
        StateRouter router = routerMap.get(fsmName);
        String mainScenarioFactorName = FSMUtils.composeMainScenarioName(fsmName);
        String setUpScenarioFactorName = FSMUtils.composeSetUpScenarioName(fsmName);
        ScenarioSequence<?> mainScenarioSequence = each.get(fsmName);
        b.put(mainScenarioFactorName, mainScenarioSequence);
        @SuppressWarnings("unchecked")
        ScenarioSequence<?> setUp = router.routeTo(mainScenarioSequence.state(0));
        if (setUp != null) {
          b.put(setUpScenarioFactorName, setUp);
        }
      }
      tuples.add(translateFSMTupleToNormalTuple(b.build(), mappedFactors));
    }
    return tuples;
  }

  private Map<String, List<State<?>>> collectSetupDestinations(List<Map<String, ScenarioSequence<?>>> mainStories) {
    Map<String, List<State<?>>> destinations = new LinkedHashMap<String, List<State<?>>>();
    for (Map<String, ScenarioSequence<?>> each : mainStories) {
      for (Map.Entry<String, ScenarioSequence<?>> ent : each.entrySet()) {
        List<State<?>> cur = destinations.get(ent.getKey());
        if (cur == null) {
          cur = new LinkedList<State<?>>();
          destinations.put(ent.getKey(), cur);
        }
        if (ent.getValue().size() > 0) {
          State<?> state = ent.getValue().get(0).given;
          if (!cur.contains(state)) {
            cur.add(ent.getValue().get(0).given);
          }
        }
      }
    }
    return destinations;
  }

  private List<Map<String, ScenarioSequence<?>>> generateMainStories(FSMFactors fsmFactors, ConstraintManager fsmCM) {
    final List<Map<String, ScenarioSequence<?>>> mainScenarios = new LinkedList<Map<String, ScenarioSequence<?>>>();
    for (Tuple eachTuple : generatePlainTuples(fsmFactors, fsmCM)) {
      Map<String, ScenarioSequence<?>> cur = new LinkedHashMap<String, ScenarioSequence<?>>();
      for (Map.Entry<String, FSM<?>> entry : this.fsms.entrySet()) {
        String fsmName = entry.getKey();
        ScenarioSequence<?> main = new ScenarioSequence.BuilderFromTuple()
            .setFSMFactors(fsmFactors)
            .setTuple(eachTuple)
            .setFSMName(fsmName)
            .build();
        cur.put(fsmName, main);
      }
      mainScenarios.add(cur);
    }
    return mainScenarios;
  }

  private TupleGenerator generatePlainTuples(FSMFactors fsmFactors, ConstraintManager fsmCM) {
    return new Builder(this.baseTupleGeneratorBuilder).setConstraintManager(fsmCM).setFactors(fsmFactors).build();
  }

  private static List<ScenarioSequence<?>> collectStoriesForFSM(List<Map<String, ScenarioSequence<?>>> storyMap, String fsmName) {
    List<ScenarioSequence<?>> ret = new ArrayList<ScenarioSequence<?>>(storyMap.size());
    for (Map<String, ScenarioSequence<?>> each : storyMap) {
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

  private Tuple translateFSMTupleToNormalTuple(Tuple fsmTuple, Map<String, Factor.Builder> mappedFactorBuilders) {
    Tuple.Builder b = new Tuple.Builder();
    for (Factor each : this.baseTupleGeneratorBuilder.getFactors()) {
      b.put(each.name, fsmTuple.get(each.name));
    }
    for (FactorMapper<?> each : this.factorMappers) {
      String name = each.factorName();
      Object v = each.apply(fsmTuple);
      b.put(name, v);

      Factor.Builder bb;
      if (mappedFactorBuilders.containsKey(name))
        bb = mappedFactorBuilders.get(name);
      else {
        bb = new Factor.Builder();
        bb.setName(name);
        mappedFactorBuilders.put(name, bb);
      }
      if (!bb.hasLevel(v))
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

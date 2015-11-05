package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.*;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

/**
 */
public class ToplevelCoveringArrayEngine extends CoveringArrayEngine.Base {
  private final Map<String, FSM>                 fsms;
  private final CoveringArrayEngine.Builder      baseCAEngineBuilder;
  private final List<Parameters.LocalConstraint> localCMs;
  private final RunnerContext                    runnerContext;

  public ToplevelCoveringArrayEngine(
      RunnerContext runnerContext,
      CoveringArrayEngine.Builder baseTGbuilder,
      Map<String, FSM> fsms,
      List<Parameters.LocalConstraint> localCMs) {
    this.runnerContext = checknotnull(runnerContext);
    this.fsms = checknotnull(fsms);
    this.baseCAEngineBuilder = checknotnull(baseTGbuilder);
    this.localCMs = Collections.unmodifiableList(localCMs);
  }

  @Override
  protected List<Tuple> generate() {
    Factors baseFactors = baseCAEngineBuilder.getFactors();
    FSMFactors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    Constraint fsmCM = new FSMConstraint(
        this.baseCAEngineBuilder.getConstraint(),
        this.localCMs
    );
    fsmCM.setFactors(fsmFactors);

    List<Tuple> tuples = Utils.dedup(generateTestCaseTuples(this.fsms, fsmFactors, fsmCM, this.runnerContext));
    ////
    // Rebuild factors from tuples.
    // Rebuilt factor should only contain
    // * all normal (non-fsm) factors.
    // * all 'Story' type factors with its all possible patterns without duplications.
    Factors.Builder factorsRebuilder = new Factors.Builder();
    for (String eachFSMName : fsms.keySet()) {
      Factor.Builder b = new Factor.Builder(eachFSMName);
      for (Tuple eachTuple : tuples) {
        Story story = (Story) eachTuple.get(eachFSMName);
        if (!b.hasLevel(story)) {
          b.addLevel(story);
        }
      }
      factorsRebuilder.add(b.build());
    }
    for (Factor eachFactor : fsmFactors) {
      if (!isFSMFactorName(eachFactor.name)) {
        factorsRebuilder.add(eachFactor);
      }
    }
    ////
    // In order to make behaviour of this object compatible with super class's,
    // set factors.
    super.setFactors(factorsRebuilder.build());
    return tuples;
  }

  private static FSMFactors buildFSMFactors(Factors baseFactors, Map<String, FSM> fsms) {
    FSMFactors.Builder b = new FSMFactors.Builder();
    for (Map.Entry<String, FSM> each : fsms.entrySet()) {
      b.addFSM(each.getKey(), each.getValue());
    }
    return b.setBaseFactors(baseFactors).build();
  }

  /*
   * Generate test case tuples.
   * Returned tuples already have Story attributes.
   */
  private List<Tuple> generateTestCaseTuples(Map<String, FSM> fsms, FSMFactors fsmFactors, Constraint fsmCM, RunnerContext runnerContext) {
    ////
    // Build test cases. At this point, test cases are generated as flatten FSM
    // tuples.
    Iterable<Tuple> flattenFSMTuples = generateFlattenFSMTestCaseTuples(fsmFactors, fsmCM, runnerContext).getCoveringArray();
    ////
    // First iteration: Build all main scenario sequences to list up
    //                  all edges. And prepare state routers.
    //
    // 1. Build a map from fsm name to main scenario sequences.
    Map<String, List<ScenarioSequence>> sequences = new LinkedHashMap<String, List<ScenarioSequence>>();
    for (Tuple eachTuple : flattenFSMTuples) {
      for (Map.Entry<String, FSM> entry : fsms.entrySet()) {
        String fsmName = entry.getKey();
        ScenarioSequence main = new ScenarioSequence.BuilderFromTuple()
            .setFSMFactors(fsmFactors)
            .setTuple(eachTuple)
            .setFSMName(fsmName)
            .build();
        //noinspection unchecked
        if (!sequences.containsKey(fsmName)) {
          sequences.put(fsmName, new LinkedList<ScenarioSequence>());
        }
        sequences.get(fsmName).add(main);
      }
    }
    // 2. Build a map from fsm name to state routers
    Map<String, StateRouter> stateRouters = new LinkedHashMap<String, StateRouter>();
    for (Map.Entry<String, List<ScenarioSequence>> each : sequences.entrySet()) {
      String fsmName = each.getKey();
      //noinspection unchecked
      StateRouter cur = new StateRouter(
          fsms.get(fsmName),
          new EdgeLister(each.getValue())
      );
      stateRouters.put(fsmName, cur);
    }
    ////
    // Second iteration: Build test case tuples by embedding Stories and normal
    //                   factors.
    final List<Tuple> ret = new LinkedList<Tuple>();
    for (Tuple eachTuple : flattenFSMTuples) {
      Tuple.Builder cur = new Tuple.Builder();
      for (Map.Entry<String, FSM> entry : this.fsms.entrySet()) {
        String fsmName = entry.getKey();
        ScenarioSequence main = new ScenarioSequence.BuilderFromTuple()
            .setFSMFactors(fsmFactors)
            .setTuple(eachTuple)
            .setFSMName(fsmName)
            .build();
        StateRouter router = stateRouters.get(fsmName);
        //noinspection unchecked
        Story<?, ?> story;
        if (main.state(0) != entry.getValue().initialState()) {
          //noinspection unchecked
          story = new Story(fsmName, router.routeTo(main.state(0)), main);
        } else {
          //noinspection unchecked
          story = new Story(fsmName, ScenarioSequence.EMPTY, main);
        }
        cur.put(fsmName, story);
      }
      for (String eachFactorName : eachTuple.keySet()) {
        if (!isFSMFactorName(eachFactorName)) {
          Object v = eachTuple.get(eachFactorName);
          cur.put(eachFactorName, v);
        }
      }
      ret.add(cur.build());
    }
    return ret;
  }

  private boolean isFSMFactorName(String eachFactorName) {
    return checknotnull(eachFactorName.contains(":"));
  }

  private CoveringArrayEngine generateFlattenFSMTestCaseTuples(FSMFactors fsmFactors, Constraint fsmCM, RunnerContext runnerContext) {
//    return this.baseCAEngineBuilder.build();
    return new Builder(
        runnerContext,
        fsmFactors,
        fsmCM,
        this.baseCAEngineBuilder.getEngineClass())
        .setConfigArgsForEngine(this.baseCAEngineBuilder.getConfigArgsForEngine())
        .setResolver(this.baseCAEngineBuilder.getResolver())
        .build();
  }
}

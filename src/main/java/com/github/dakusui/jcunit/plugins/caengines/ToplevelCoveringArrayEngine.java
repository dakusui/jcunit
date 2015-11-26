package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.*;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

/**
 */
public class ToplevelCoveringArrayEngine extends CoveringArrayEngine.Base {
  private final Map<String, FSM>                        fsms;
  private final CoveringArrayEngine.Builder             baseCAEngineBuilder;
  private final List<Parameters.LocalConstraintChecker> localCMs;
  private final RunnerContext                           runnerContext;

  public ToplevelCoveringArrayEngine(
      RunnerContext runnerContext,
      CoveringArrayEngine.Builder baseTGbuilder,
      Map<String, FSM> fsms,
      List<Parameters.LocalConstraintChecker> localCMs) {
    this.runnerContext = checknotnull(runnerContext);
    this.fsms = checknotnull(fsms);
    this.baseCAEngineBuilder = checknotnull(baseTGbuilder);
    this.localCMs = Collections.unmodifiableList(localCMs);
  }

  @Override
  protected List<Tuple> generate(Factors factors, ConstraintChecker constraintChecker) {
    Factors baseFactors = baseCAEngineBuilder.getFactors();
    Factors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    /* TODO : Build FSM CM appropriately */
    FSMConstraintChecker fsmCM = null;

    /*new FSMConstraintChecker(
        this.baseCAEngineBuilder.getConstraintChecker(),
        fsmFactors,
        this.localCMs
    );*/

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
    return tuples;
  }

  private static Factors buildFSMFactors(Factors baseFactors, Map<String, FSM> fsms) {
    Factors.Builder b = new Factors.Builder();
    for (Map.Entry<String, FSM> each : fsms.entrySet()) {
      FSMFactors fsmFactors = new FSMFactors.Builder(each.getKey(), each.getValue(), 2).build();
      for (Factor eachFactor : fsmFactors.asFactorList()) {
        b.add(eachFactor);
      }
    }
    for (Factor each : baseFactors) {
      b.add(each);
    }
    return b.build();
  }

  /*
   * Generate test case tuples.
   * Returned tuples already have Story attributes.
   */
  private List<Tuple> generateTestCaseTuples(Map<String, FSM> fsms, Factors fsmFactors, ConstraintChecker fsmCM, RunnerContext runnerContext) {
    ////
    // Build test cases. At this point, test cases are generated as flatten FSM
    // tuples.
    Iterable<Tuple> flattenFSMTuples = generateFlattenFSMTestCaseTuples(fsmFactors, fsmCM, runnerContext).generate(
        new FactorSpace(
            FactorSpace.convertFactorsIntoSimpleFactorDefs(fsmFactors),
            fsmCM));
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
      StateRouter cur = new StateRouter.Base(
          fsms.get(fsmName)
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
            .setTuple(eachTuple)
            .setFSMName(fsmName)
                .setHistoryLength(2)
            .build();
        StateRouter router = stateRouters.get(fsmName);
        //noinspection unchecked
        Story<?, ?> story;
        if (main.state(0) != entry.getValue().initialState()) {
          //noinspection unchecked
          story = new Story(fsmName, router.routeTo(main.state(0)), main);
        } else {
          //noinspection unchecked
          story = new Story(fsmName, ScenarioSequence.Empty.getInstance(), main);
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

  private CoveringArrayEngine generateFlattenFSMTestCaseTuples(Factors fsmFactors, ConstraintChecker fsmCM, RunnerContext runnerContext) {
    //noinspection unchecked
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

package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;

import java.util.*;

/**
 */
public class ToplevelCoveringArrayEngine extends CoveringArrayEngineBase {
  private final Map<String, FSM>                 fsms;
  private final CoveringArrayEngine.Builder      baseCAEngineBuilder;
  private final List<Parameters.LocalConstraint> localCMs;
  private       List<Tuple>                      tuples;

  public ToplevelCoveringArrayEngine(
      CoveringArrayEngine.Builder baseTG,
      Map<String, FSM> fsms,
      List<Parameters.LocalConstraint> localCMs) {
    this.fsms = Checks.checknotnull(fsms);
    this.baseCAEngineBuilder = Checks.checknotnull(baseTG);
    this.localCMs = Collections.unmodifiableList(localCMs);
  }

  @Override
  protected long initializeTuples() {
    Factors baseFactors = baseCAEngineBuilder.getFactors();
    FSMFactors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    Constraint fsmCM = new FSMConstraint(
        this.baseCAEngineBuilder.getConstraint(),
        this.localCMs
    );
    fsmCM.setFactors(fsmFactors);

    this.tuples = Utils.dedup(generateTestCaseTuples(this.fsms, fsmFactors, fsmCM));
    ////
    // Rebuild factors from tuples.
    // Rebuilt factor should only contain
    // * all normal (non-fsm) factors.
    // * all 'Story' type factors with its all possible patterns without duplications.
    Factors.Builder factorsRebuilder = new Factors.Builder();
    for (String eachFSMName : fsms.keySet()) {
      Factor.Builder b = new Factor.Builder(eachFSMName);
      for (Tuple eachTuple : this.tuples) {
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
    ////
    // Constraint manager is used for negative tests generation, which is not supported yet.
    // This time I'm setting DEFAULT_CONSTRAINT_MANAGER.
    super.setConstraint(Constraint.DEFAULT_CONSTRAINT_MANAGER);
    return this.tuples.size();
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
  private List<Tuple> generateTestCaseTuples(Map<String, FSM> fsms, FSMFactors fsmFactors, Constraint fsmCM) {
    ////
    // Build test cases. At this point, test cases are generated as flatten FSM
    // tuples.
    Iterable<Tuple> flattenFSMTuples = generateFlattenFSMTestCaseTuples(fsmFactors, fsmCM);
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
    return Checks.checknotnull(eachFactorName.contains(":"));
  }

  private CoveringArrayEngine generateFlattenFSMTestCaseTuples(FSMFactors fsmFactors, Constraint fsmCM) {
    return new Builder(this.baseCAEngineBuilder).setConstraint(fsmCM).setFactors(fsmFactors).build();
  }

  @Override
  public Tuple getTuple(int tupleId) {
    return tuples.get(tupleId);
  }
}

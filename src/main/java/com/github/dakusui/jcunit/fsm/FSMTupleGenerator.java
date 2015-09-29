package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorMapper;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

import java.util.*;

/**
 */
public class FSMTupleGenerator extends TupleGeneratorBase {
  private final Map<String, FSM>       fsms;
  private final TupleGenerator.Builder baseTupleGeneratorBuilder;
  private final List<FactorMapper>     factorMappers;
  private       List<Tuple>            tuples;

  public FSMTupleGenerator(TupleGenerator.Builder baseTG, Map<String, FSM> fsms, List<FactorMapper> factorMappers) {
    this.fsms = Checks.checknotnull(fsms);
    this.baseTupleGeneratorBuilder = Checks.checknotnull(baseTG);
    this.factorMappers = factorMappers;
  }

  private static FSMFactors buildFSMFactors(Factors baseFactors, Map<String, FSM> fsms) {
    FSMFactors.Builder b = new FSMFactors.Builder();
    for (Map.Entry<String, FSM> each : fsms.entrySet()) {
      b.addFSM(each.getKey(), each.getValue());
    }
    return b.setBaseFactors(baseFactors).build();
  }

  public static String composeMainScenarioName(String fsmName) {
    return String.format("FSM:main:%s", fsmName);
  }

  @Override
  protected long initializeTuples(Object[] params) {
    Factors baseFactors = baseTupleGeneratorBuilder.getFactors();
    FSMFactors fsmFactors = buildFSMFactors(baseFactors, this.fsms);

    ConstraintManager fsmCM = new FSMConstraintManager(this.baseTupleGeneratorBuilder.getConstraintManager());
    fsmCM.setFactors(fsmFactors);

    this.tuples = generateTuples(fsmFactors, fsmCM);
    ////
    // In order to make behaviour of this object compatible with super class's,
    // set factors.
    super.setFactors(buildFactors(baseFactors));
    ////
    // Constraint manager is used for negative tests generation, which is not supported yet.
    // This time I'm setting DEFAULT_CONSTRAINT_MANAGER.
    super.setConstraintManager(ConstraintManager.DEFAULT_CONSTRAINT_MANAGER);
    return this.tuples.size();
  }

  private List<Tuple> generateTuples(FSMFactors fsmFactors, ConstraintManager fsmCM) {
    final List<Tuple> ret = new LinkedList<Tuple>();
    for (Tuple eachTuple : generatePlainTuples(fsmFactors, fsmCM)) {
      Tuple.Builder cur = new Tuple.Builder();
      for (Map.Entry<String, FSM> entry : this.fsms.entrySet()) {
        StateRouter router = new StateRouter(
            this.fsms.get(entry.getKey()),
            new StateRouter.EdgeLister(null));
        String fsmName = entry.getKey();
        ScenarioSequence main = new ScenarioSequence.BuilderFromTuple()
            .setFSMFactors(fsmFactors)
            .setTuple(eachTuple)
            .setFSMName(fsmName)
            .build();
        //noinspection unchecked
        Story<?, ?> story = new Story(fsmName, router.routeTo(main.state(0)), main);
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

  private TupleGenerator generatePlainTuples(FSMFactors fsmFactors, ConstraintManager fsmCM) {
    return new Builder(this.baseTupleGeneratorBuilder).setConstraintManager(fsmCM).addFactors(fsmFactors).build();
  }

  /*
   * Return normal factors.
   */
  private Factors buildFactors(Factors baseFactors) {
    Factors.Builder b = new Factors.Builder();
    for (Factor each : baseFactors) {
      if (!isFSMFactorName(each.name)) {
        b.add(each);
      }
    }
    return b.build();
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
    return new ParamType[] {};
  }

  private List<Tuple> buildTuples(Map<String, FSM> fsms, List<Map<String, ScenarioSequence<Object>>> mainStories, Map<String, StateRouter<Object>> routerMap, Map<String, Factor.Builder> mappedFactors) {
    List<Tuple> tuples = new LinkedList<Tuple>();
    for (Map<String, ScenarioSequence<Object>> each : mainStories) {
      Tuple.Builder b = new Tuple.Builder();
      for (String fsmName : fsms.keySet()) {
        StateRouter router = routerMap.get(fsmName);
        String mainScenarioFactorName = composeMainScenarioName(fsmName);
        ScenarioSequence<Object> mainScenarioSequence = each.get(fsmName);
        @SuppressWarnings("unchecked")
        ScenarioSequence<Object> setUp = router.routeTo(mainScenarioSequence.state(0));
        b.put(mainScenarioFactorName, new Story<FSMSpec<Object>, Object>(fsmName, setUp, mainScenarioSequence));
      }
      tuples.add(translateFSMTupleToNormalTuple(b.build(), mappedFactors));
    }
    return tuples;
  }
}

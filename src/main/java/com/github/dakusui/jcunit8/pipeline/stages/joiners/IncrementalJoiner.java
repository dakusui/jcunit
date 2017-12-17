package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.LinkedList;
import java.util.List;

public class IncrementalJoiner extends Joiner.Base {
  private final Requirement requirement;

  IncrementalJoiner(Requirement requirement) {
    this.requirement = requirement;
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int strength = requirement.strength();
    List<String> lhsSeedAttributeNames = lhs.getAttributeNames().subList(0, strength);
    SchemafulTupleSet seeds = new StandardJoiner(requirement).apply(
        lhs.project(lhsSeedAttributeNames), rhs
    );
    List<Tuple> ts = buildInitialTupleSet(seeds, lhs, lhsSeedAttributeNames);


    final int t = requirement.strength();
    final int n = lhsSeedAttributeNames.size();
    TupleSet π;
    List<Factor> processedFactors = new LinkedList<>();
    for (int i = t + 1; i <= n; i++) {
      Factor Pi = Factor.create(lhs.getAttributeNames().get(i), lhs.getAttributeValuesOf(lhs.getAttributeNames().get(i)).toArray());
      π = prepare_π();
    }
    return null;
  }

  private static List<Tuple> buildInitialTupleSet(SchemafulTupleSet seeds, SchemafulTupleSet lhs, List<String> lhsAttributeNames) {
    return null;
  }

  private TupleSet prepare_π() {
    return null;
  }
}

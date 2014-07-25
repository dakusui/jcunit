package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.List;

public class IPO2SchemafulTupleGenerator extends SchemafulTupleGeneratorBase {
  List<Tuple> tests;

  @Override public Tuple getSchemafulTuple(int testId) {
    return this.tests.get((int) testId);
  }

  @Override protected long initializeSchemafulTuples(Object[] params) {
    IPO2 ipo2 = new IPO2(
        this.getFactors(),
        params.length == 0 ? 2 : ((Number)params[0]).intValue(),
        this.getConstraintManager(),
        new GreedyIPO2Optimizer());
    ////
    // Wire constraint manager.
    this.getConstraintManager().addObserver(ipo2);
    ////
    // Perform IPO algorithm.
    ipo2.ipo();
    this.tests = ipo2.getResult();
    return this.tests.size();
  }
}

package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.Arrays;
import java.util.List;

public class IPO2TupleGenerator extends TupleGeneratorBase {
  List<Tuple> tests;

  /**
   * {@inheritDoc}
   */
  @Override public Tuple getTuple(int tupleId) {
    return this.tests.get(tupleId);
  }

  /**
   * processedParameters[0] must be an int value greater than 1 and less than or
   * equal to the number of factors, if given.
   * If no parameter is given, it defaults to 2.
   * <p/>
   * If more than 1 parameter is given, this method will throw an {@code IllegalArgumentException}.
   */
  @Override protected long initializeTuples(
      Object[] processedParameters) {
    int strength = processedParameters.length == 0 ?
        2 :
        ((Number) processedParameters[0]).intValue();
    if (processedParameters.length > 1) {
      String msg = String.format(
          "At most 1 parameter is allowed for %s, but %d was given.: %s",
          this.getClass().getSimpleName(), processedParameters.length,
          Arrays.toString(processedParameters));
      throw new IllegalArgumentException(msg);
    }
    IPO2 ipo2 = new IPO2(
        this.getFactors(),
        strength,
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

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[]{ ParamType.Int.withDefaultValue(2) };
  }
}

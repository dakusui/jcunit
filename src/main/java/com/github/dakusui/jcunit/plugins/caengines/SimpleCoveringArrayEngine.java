package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.LinkedList;
import java.util.List;

/**
 * A tuple generator which generates a test suite each of whose test cases
 * has only one factor is set to non-default level.
 *
 * The default value is the first level of each factor.
 */
public class SimpleCoveringArrayEngine extends CoveringArrayEngineBase {
  private List<Tuple> tests;

  public SimpleCoveringArrayEngine() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple getTuple(int tupleId) {
    return this.tests.get(tupleId);
  }

  @Override
  protected long initializeTuples() {
    this.tests = new LinkedList<Tuple>();
    Factors factors = this.getFactors();
    for (String eachFactorName : factors.getFactorNames()) {
      for (Object eachLevel : factors.get(eachFactorName)) {
        Tuple tuple = newTuple(factors);
        tuple.put(eachFactorName, eachLevel);
        try {
          if (this.getConstraint().check(tuple)) {
            this.tests.add(tuple);
          }
        } catch (UndefinedSymbol undefinedSymbol) {
          // This path shouldn't be executed because this tuple generator assigns
          // values to all the factors (symbols).
          assert false;
        }
      }
    }
    return this.tests.size();
  }


  /**
   * Returns a tuple whose fields are filled with default values of the factors.
   */
  private Tuple newTuple(Factors factors) {
    Tuple.Builder b = new Tuple.Builder();
    for (String eachFactorName : factors.getFactorNames()) {
      b.put(eachFactorName, factors.get(eachFactorName).levels.get(0));
    }
    return b.build();
  }
}

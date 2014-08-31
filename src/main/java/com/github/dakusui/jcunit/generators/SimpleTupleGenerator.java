package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.LinkedList;
import java.util.List;

public class SimpleTupleGenerator extends TupleGeneratorBase {
  private List<Tuple> tests;
  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple getTuple(int tupleId) {
    return this.tests.get(tupleId);
  }

  @Override
  protected long initializeTuples(Object[] params) {
    this.tests = new LinkedList<Tuple>();
    Factors factors = this.getFactors();
    for (String eachFactorName : factors.getFactorNames()) {
      for (Object eachLevel : factors.get(eachFactorName)) {
        Tuple tuple = newTuple(factors);
        tuple.put(eachFactorName, eachLevel);
        try {
          if (this.getConstraintManager().check(tuple)) {
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

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }
}

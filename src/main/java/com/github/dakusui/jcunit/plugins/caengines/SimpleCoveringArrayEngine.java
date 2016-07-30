package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintBundle;

import java.util.LinkedList;
import java.util.List;

/**
 * A tuple generator which generates a test suite each of whose test cases
 * has only one factor is set to non-default level.
 *
 * The default value is the first level of each factor.
 */
public class SimpleCoveringArrayEngine extends CoveringArrayEngine.Base {
  public SimpleCoveringArrayEngine() {
  }

  @Override
  protected List<Tuple> generate(Factors factors, ConstraintBundle constraintBundle) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (String eachFactorName : factors.getFactorNames()) {
      for (Object eachLevel : factors.get(eachFactorName)) {
        Tuple tuple = newTuple(factors);
        tuple.put(eachFactorName, eachLevel);
        try {
          if (constraintBundle.newConstraintChecker().check(tuple)) {
            ret.add(tuple);
          }
        } catch (UndefinedSymbol undefinedSymbol) {
          // This path shouldn't be executed because this tuple generator assigns
          // values to all the factors (symbols).
          assert false;
        }
      }
    }
    return ret;
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

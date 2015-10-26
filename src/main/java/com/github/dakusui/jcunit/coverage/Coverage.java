package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.util.Set;

public class Coverage {
  final         int     strength;
  private final Factors factorSpace;
  Set<Tuple> items;


  public Coverage(Factors factors, int strength) {
    this.factorSpace = Checks.checknotnull(factors);
    Checks.checkcond(strength <= factors.size());
    Checks.checkcond(strength > 0);
    this.strength = strength;
  }

  public void process(Tuple testCase) {
    for (Tuple each : TupleUtils.subtuplesOf(testCase, this.strength)) {
      items.remove(each);
    }
  }

}

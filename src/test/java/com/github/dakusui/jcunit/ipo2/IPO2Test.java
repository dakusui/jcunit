package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.jcunit.generators.ipo2.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public abstract class IPO2Test {
  protected List<ValueTuple<String, Object>> generate(
      Factors domains, int strength,
      ConstraintManager<String, Object> constraintManager,
      IPO2Optimizer optimizer) {
    IPO2 ipo = new IPO2(domains.toLinkedHashMap(), strength, constraintManager,
        optimizer);
    ipo.ipo();
    return ipo.getResult();
  }

  protected void verify(List<ValueTuple<String, Object>> testcases,
      int strength, Factors factors) {
    System.out.println(testcases);
    System.out.println(testcases.size());
    System.out.println("----");
    List<ValueTuple<String, Object>> tuplesToBeGenerated = factors
        .generateAllPossibleTuples(strength);
    List<ValueTuple<String, Object>> notFound = new LinkedList<ValueTuple<String, Object>>();
    for (ValueTuple<String, Object> t : tuplesToBeGenerated) {
      if (!find(t, testcases)) {
        notFound.add(t);
      }
    }
    assertTrue(String
            .format("%d tuples were not found. %s", notFound.size(), notFound),
        notFound.size() == 0
    );
  }

  protected boolean find(ValueTuple<String, Object> t,
      List<ValueTuple<String, Object>> tuples) {
    for (ValueTuple<String, Object> cur : tuples) {
      if (matches(t, cur)) {
        return true;
      }
    }
    return false;
  }

  private boolean matches(ValueTuple<String, Object> q,
      ValueTuple<String, Object> tuple) {
    for (String k : q.keySet()) {
      if (!tuple.containsKey(k)) {
        return false;
      }
      if (!IPO2Utils.eq(q.get(k), tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  protected static Factor factor(String name, Object... factors) {
    return new Factor(name, Arrays.asList(factors));
  }

}

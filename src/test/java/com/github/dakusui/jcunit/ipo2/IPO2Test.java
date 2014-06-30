package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hiroshi on 6/30/14.
 */
public class IPO2Test {
  static class Factor {
    String name;
    Object[] levels;
    Factor(String name, Object[] levels) {
      this.name = name;
      this.levels = levels;
    }
  }

  @Test
  public void test() {
    Factor[] factors = new Factor[]{
        factor("F1", "L11", "L12"),
        factor("F2", "L21", "L22", "L23"),
        factor("F3", "L31", "L32", "L33", "L34")
    };
    int strength = 2;
    ConstraintManager constraintManager = new NullConstraintManager();
    IPO2Optimizer optimizer = new GreedyIPO2Optimizer();

    List<ValueTuple<String, Object>> testcases = generate(factors(factors), strength, constraintManager, optimizer);
    verify(testcases, strength);
  }


  protected LinkedHashMap<String, Object[]> factors(Factor[] factors) {
    LinkedHashMap<String, Object[]> ret = new LinkedHashMap<String, Object[]>();
    for (Factor f : factors) {
      ret.put(f.name, f.levels);
    }
    return ret;
  }

  protected List<ValueTuple<String, Object>> generate(LinkedHashMap<String, Object[]> domains, int strength, ConstraintManager constraintManager, IPO2Optimizer optimizer) {
    IPO2 ipo = new IPO2(domains, strength, constraintManager, optimizer);
    ipo.ipo();
    return ipo.getResult();
  }

  protected void verify(List<ValueTuple<String, Object>> testcases, int strength) {
    System.out.println(testcases);
  }

  private static Factor factor(String name, Object... factors) {
    return new Factor(name, factors);
  }
}

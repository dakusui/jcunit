package com.github.dakusui.jcunit.framework.tests.ipo2;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Before;
import org.junit.Test;

public class BenchMark extends IPO2Test {
  public static class FactorsDef {
    int numLevels;
    int numFactors;

    public FactorsDef(int numLevels, int numFactors) {
      this.numLevels = numLevels;
      this.numFactors = numFactors;
    }
  }

  static FactorsDef factorsDef(int l, int f) {
    return new FactorsDef(l, f);
  }

  static Factors buildFactors(FactorsDef... factorsDefs) {
    Factors.Builder fb = new Factors.Builder();
    char ch = 'A';
    for (FactorsDef fd : factorsDefs) {
      for (int i = 0; i < fd.numFactors; i++) {
        Factor.Builder b = new Factor.Builder();
        b.setName(new Character(ch).toString());
        for (int j = 0; j < fd.numLevels; j++) {
          b.addLevel(new Character(ch).toString() + j);
        }
        ch++;
        fb.add(b.build());
      }
    }
    return fb.build();
  }

  protected int strength;

  @Before
  public void before() {
    this.strength = 2;
  }

  @Test
  public void benchmark3$4() {
    Factors factors = buildFactors(factorsDef(3, 4));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark3$13() {
    Factors factors = buildFactors(factorsDef(3, 13));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark4$15_3$17_2$20() {
    Factors factors = buildFactors(factorsDef(4, 15), factorsDef(3, 17), factorsDef(2, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark4$1_3$30_2$35() {
    Factors factors = buildFactors(factorsDef(4, 1), factorsDef(3, 30), factorsDef(2, 35));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark2$100() {
    Factors factors = buildFactors(factorsDef(2, 100));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

  @Test
  public void benchmark10$20() {
    Factors factors = buildFactors(factorsDef(10, 20));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generate(factors,
        strength, constraintManager, optimizer);
    verify(strength, factors, constraintManager, ipo.getResult(), ipo.getRemainders()
    );
  }

}

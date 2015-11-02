package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.List;

public class IPO2CoveringArrayEngine extends CoveringArrayEngineBase {
  private final int strength;
  List<Tuple> tests;

  public IPO2CoveringArrayEngine(
      @Param(source = Param.Source.INSTANCE, defaultValue = "2") int strength) {
    this.strength = strength;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple getTuple(int tupleId) {
    return this.tests.get(tupleId);
  }

  /**
   * processedParameters[0] must be an int value greater than 1 and less than or
   * equal to the number of factors, if given.
   * If no parameter is given, it defaults to 2.
   * <p/>
   * If more than 1 parameter is given, this method will throw an {@code IllegalArgumentException}.
   */
  @Override
  protected long initializeTuples() {
    Factors factors = this.getFactors();
    Checks.checktest(factors.size() >= 2,
        "There must be 2 or more factors, but only %s (%s) given.",
        factors.size(),
        StringUtils.join(",", new StringUtils.Formatter<Factor>() {
              @Override
              public String format(Factor elem) {
                return elem.name;
              }
            },
            factors.asFactorList().toArray(new Factor[factors.size()])
        ));
    Checks.checktest(factors.size() >= strength,
        "The strength must be greater than 1 and less than %s, but %s was given.",
        factors.size(),
        strength);
    Checks.checktest(strength >= 2,
        "The strength must be greater than 1 and less than %s, but %s was given.",
        factors.size(),
        strength);
    IPO2 ipo2 = new IPO2(
        this.getFactors(),
        strength,
        this.getConstraint(),
        new GreedyIPO2Optimizer());
    ////
    // Wire constraint checker.
    this.getConstraint().addObserver(ipo2);
    ////
    // Perform IPO algorithm.
    ipo2.ipo();
    this.tests = ipo2.getResult();
    return this.tests.size();
  }
}

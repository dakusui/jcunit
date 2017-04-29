package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.GreedyIpo2Optimizer;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintBundle;

import java.util.List;

public class Ipo2CoveringArrayEngine extends CoveringArrayEngine.Base {
  private final int strength;

  public Ipo2CoveringArrayEngine(
      @Param(source = Param.Source.CONFIG, defaultValue = "2") int strength) {
    this.strength = strength;
  }

  /**
   * processedParameters[0] must be an int value greater than 1 and less than or
   * equal to the number of factors, if given.
   * If no parameter is given, it defaults to 2.
   * <p/>
   * If more than 1 parameter is given, this method will throw an {@code IllegalArgumentException}.
   * @param factors from which a covering array is generated.
   * @param constraintBundle with which a covering array is generated.
   */
  @Override
  protected List<Tuple> generate(Factors factors, ConstraintBundle constraintBundle) {
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
    Ipo2 ipo2 = new Ipo2(
        factors,
        strength,
        constraintBundle.newConstraintChecker(),
        new GreedyIpo2Optimizer());
    ////
    // Perform IPO algorithm.
    return ipo2.ipo().getGeneratedTuples();
  }
}

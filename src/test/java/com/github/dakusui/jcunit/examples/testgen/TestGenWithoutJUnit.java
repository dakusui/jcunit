package com.github.dakusui.jcunit.examples.testgen;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.utils.CoveringArrayEngines;

import java.io.PrintStream;

/**
 * An example to let CAEngine generate a test suite as Tuple objects.
 * Based on a work by "ether" ( http://rainyday.blog.so-net.ne.jp/ )
 * <p/>
 * This relies on JCUnit's internal classes, which can be changed by JCUnit's
 * author, although big change wouldn't happen so often.
 *
 * @see "http://rainyday.blog.so-net.ne.jp/2015-07-05"
 */
public class TestGenWithoutJUnit {
  public static void main(String... args) {
    new TestGenWithoutJUnit().run(System.out);
    new TestGenWithoutJUnit().runMoreFluently(System.out);
  }

  public void run(PrintStream ps) {
    // TODO: Update example accordingly (#35)
    Factor os = new Factor.Builder("OS")
        .addLevel("Windows")
        .addLevel("Linux")
        .build();
    Factor browser = new Factor.Builder("Browser")
        .addLevel("Chrome")
        .addLevel("Firefox")
        .build();
    Factor bits = new Factor.Builder("Bits")
        .addLevel("32")
        .addLevel("64")
        .build();
    Factors factors = new Factors.Builder().add(os).add(browser).add(bits).build();
    CoveringArrayEngine engine = CoveringArrayEngines.createSimpleBuilder(factors)
        .build();
    CoveringArray coveringArray = engine.generate(new FactorSpace(
        FactorSpace.convertFactorsIntoSimpleFactorDefs(factors),
        ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER));

    for (Tuple each : coveringArray) {
      ps.println(each);
    }
  }

  public void runMoreFluently(PrintStream ps) {
    // TODO: Update example accordingly (#35)
    Factors factors = new Factors.Builder()
        .add("OS", "Windows", "Linux")
        .add("Browser", "Chrome", "Firefox")
        .add("Bits", "32", "64").build();
    CoveringArrayEngine engine = CoveringArrayEngines.createSimpleBuilder(
        factors,
        IPO2CoveringArrayEngine.class, new String[][] { { "2" } }
    ).build();

    CoveringArray coveringArray = engine.generate(new FactorSpace(
        FactorSpace.convertFactorsIntoSimpleFactorDefs(factors),
        ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER)
    );
    for (Tuple each : coveringArray) {
      ps.println(each);
    }
  }

}

package com.github.dakusui.jcunit.examples.testgen;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.io.PrintStream;

/**
 * An example to let TupleGenerator generate a test suite as Tuple objects.
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
    new TestGenWithoutJUnit().moreFluentStyleRun(System.out);
  }

  public void run(PrintStream ps) {
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
    TupleGenerator tg = new TupleGenerator.Builder()
        .setFactors(factors)
    /* -- To set custom parameter(s) for the tuple generator, you can do below.
        .setParameters(new Param.ArrayBuilder()
          .add("2")
        .build())
     */
    /* -- You can set custom constraint manager by using setConstraintManager method.
        .setConstraintManager(ConstraintManager.DEFAULT_CONSTRAINT_MANAGER)
     */
        .build();
    for (Tuple each : tg) {
      ps.println(each);
    }
  }

  public void moreFluentStyleRun(PrintStream ps) {
    TupleGenerator tg = new TupleGenerator.Builder().setFactors(
        new Factors.Builder()
            .add("OS", "Windows", "Linux")
            .add("Browser", "Chrome", "Firefox")
            .add("Bits", "32", "64").build()
    ).build();
    for (Tuple each : tg) {
      ps.println(each);
    }
  }

}

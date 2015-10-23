package com.github.dakusui.jcunit.plugins.generators;

import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A tuple generator which generates a test suite using random.
 * A user can specify a random seed to be used in the generation.
 *
 * @see RandomTupleGenerator#parameterTypes()
 */
public class RandomTupleGenerator extends TupleGeneratorBase {
  private List<Tuple> tests;

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple getTuple(int tupleId) {
    return tests.get(tupleId);
  }

  @Override
  protected long initializeTuples(Object[] params) {
    int size = (Integer) params[0];
    this.tests = new ArrayList<Tuple>(size);
    Random random = new Random((Long) params[1]);
    Factors factors = this.getFactors();
    int retries = 50;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < retries; j++) {
        Tuple tuple = newTuple(factors, random);
        try {
          if (this.getConstraintManager().check(tuple)) {
            this.tests.add(tuple);
            break;
          }
        } catch (UndefinedSymbol undefinedSymbol) {
          // This path shouldn't be executed because this tuple generator assigns
          // values to all the factors (symbols).
          assert false;
        }
      }
    }
    return tests.size();
  }

  private Tuple newTuple(Factors factors, Random random) {
    Tuple.Builder b = new Tuple.Builder();
    for (String eachFactorName : factors.getFactorNames()) {
      Factor f = factors.get(eachFactorName);
      b.put(eachFactorName, f.levels.get(random.nextInt(f.levels.size())));
    }
    return b.build();
  }

  /**
   * The first parameter specifies the number of test cases. This must be
   * non-negative integer. This parameter is mandatory.
   *
   * The second one specifies a seed for random number generation. By specifying
   * the seed, you can get the same test suite always.
   * Long value or a fixed string "SYSTEM_PROPERTY" can be given. If the string
   * "SYSTEM_PROPERTY" is given, JCUnit will get it from a system property
   * {@code jcunit.generator.randomseed}. And if the system property isn't set,
   * JCUnit uses a number based on current time as its seed. This parameter is
   * mandatory.
   *
   * @return definitions of parameters for this TupleGenerator
   * @see com.github.dakusui.jcunit.core.SystemProperties.KEY#RANDOMSEED
   * @see SystemProperties#randomSeed()
   */
  @Override
  public Arg.Type[] parameterTypes() {
    return new Arg.Type[] {
        new Arg.Type() {
          @Override
          public Object parse(String[] values) {
            Integer ret = (Integer) Arg.Type.Int.parse(values);
            Checks.checktest(ret >= 0, "Must be non-negative, but '%s' was given.", ret);
            return ret;
          }
        },
        new Arg.Type.NonArrayType() {
          @Override
          protected Object parse(String str) {
            long randomSeed = 0;
            if ("SYSTEM_PROPERTY".equals(str)) {
              randomSeed = SystemProperties.randomSeed();
            } else {
              try {
                randomSeed = java.lang.Long.parseLong(str);
              } catch (NumberFormatException e) {
                Checks.checktest(false, "The value '%s' can't be used as a random seed. It must be a long.", str);
              }
            }
            return randomSeed;
          }
        }
    };
  }
}

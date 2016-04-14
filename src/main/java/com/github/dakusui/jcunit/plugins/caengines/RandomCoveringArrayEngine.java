package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.SystemProperties;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A tuple generator which generates a test suite using random.
 * A user can specify a random seed to be used in the generation.
 */
public class RandomCoveringArrayEngine extends CoveringArrayEngine.Base {
  private final long        seed;
  private final int         size;

  /**
   * <p/>
   * The first parameter specifies the number of test cases to be generated.
   * This must be non-negative integer and is a mandatory parameter.
   * <p/>
   * The second one specifies a seed for random number generation. By specifying
   * the seed, you can get the same test suite always.
   *
   * @see SystemProperties#randomSeed()
   */
  public RandomCoveringArrayEngine(
      @Param(source = Param.Source.CONFIG) int size,
      @Param(
          source = Param.Source.SYSTEM_PROPERTY,
          propertyKey = SystemProperties.Key.RANDOMSEED,
          defaultValue = "1"
      ) long seed
  ) {
    Checks.checktest(size >= 0, "Must be non-negative, but '%s' was given.", size);
    this.size = size;
    this.seed = seed;
  }

  @Override
  protected List<Tuple> generate(Factors factors, ConstraintChecker constraintChecker) {
    List<Tuple> ret = new ArrayList<Tuple>(size);
    Random random = new Random(this.seed);
    int retries = 50;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < retries; j++) {
        Tuple tuple = newTuple(factors, random);
        try {
          if (constraintChecker.check(tuple)) {
            ret.add(tuple);
            break;
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

  private Tuple newTuple(Factors factors, Random random) {
    Tuple.Builder b = new Tuple.Builder();
    for (String eachFactorName : factors.getFactorNames()) {
      Factor f = factors.get(eachFactorName);
      b.put(eachFactorName, f.levels.get(random.nextInt(f.levels.size())));
    }
    return b.build();
  }
}

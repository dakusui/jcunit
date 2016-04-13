package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleImpl;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.StandardCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerBase;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public class TestSuite extends AbstractList<TestCase> {

  private final List<TestCase> testCases;

  public TestSuite(List<TestCase> testCases) {
    this.testCases = Utils.newUnmodifiableList(testCases);
  }

  @Override
  public TestCase get(int index) {
    return this.testCases.get(index);
  }

  @Override
  public int size() {
    return this.testCases.size();
  }

  /**
   * A builder class of a test suite.
   */
  public static class Builder {
    private final List<Factor>        factors;
    private       CoveringArrayEngine coveringArrayEngine;
    private List<Utils.Predicate<Tuple>> constraints = new LinkedList<Utils.Predicate<Tuple>>();
    private boolean negativeTestsEnabled;

    public Builder addConstraint(Utils.Predicate<Tuple> constraint) {
      this.constraints.add(checknotnull(constraint));
      return this;
    }

    public Builder addFactor(String name, List<?> levels) {
      checknotnull(name);
      checknotnull(levels);
      Factor.Builder b = new Factor.Builder(name);
      for (Object each : levels) {
        b.addLevel(each);
      }
      this.factors.add(b.build());
      return this;
    }

    public Builder addBooleanFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.booleanLevels()));
      return this;
    }

    public Builder addByteFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.byteLevels()));
      return this;
    }

    public Builder addCharFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.charLevels()));
      return this;
    }

    public Builder addShortFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.shortLevels()));
      return this;
    }

    public Builder addIntFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.intLevels()));
      return this;
    }

    public Builder addLongFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.longLevels()));
      return this;
    }

    public Builder addFloatFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.doubleLevels()));
      return this;
    }

    public Builder addDoubleFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.doubleLevels()));
      return this;
    }

    public Builder addStringFactor(String name) {
      this.addFactor(name, toList(FactorField.DefaultLevels.stringLevels()));
      return this;
    }

    public Builder addEnumLevels(String name, Class<? extends Enum> enumClass) {
      this.addFactor(name, Arrays.asList(enumClass.getEnumConstants()));
      return this;
    }

    private List toList(final Object primitiveArray) {
      return new AbstractList() {
        @Override
        public Object get(int index) {
          return Array.get(primitiveArray, index);
        }

        @Override
        public int size() {
          return Array.getLength(primitiveArray);
        }
      };
    }

    public Builder enableNegativeTests() {
      this.negativeTestsEnabled = true;
      return this;
    }

    public Builder disableNegativeTests() {
      this.negativeTestsEnabled = false;
      return this;
    }

    public TestSuite build() {
      FactorSpace.Builder builder = new FactorSpace.Builder();
      builder.addFactorDefs(Utils.transform(this.factors, new Utils.Form<Factor, FactorDef>() {
        @Override
        public FactorDef apply(final Factor in) {
          return new FactorDef.Simple(in.name, new LevelsProvider() {
            @Override
            public int size() {
              return in.levels.size();
            }

            @Override
            public Object get(int n) {
              return in.levels.get(n);
            }
          });
        }
      }));
      ConstraintChecker checker = new SmartConstraintCheckerBase(new Factors(factors)) {
        @Override
        protected List<Constraint> getConstraints() {
          return Utils.transform(
              constraints,
              new Utils.Form<Utils.Predicate<Tuple>, Constraint>() {
                @Override
                public Constraint apply(Utils.Predicate<Tuple> in) {
                  return new GuardedConstraint(String.format("constraint-%03d", constraints.indexOf(in)), in);
                }
              }
          );
        }
      };
      builder.setTopLevelConstraintChecker(checker);
      List<TestCase> ret = new LinkedList<TestCase>();
      final int[] id = new int[1];
      ret.addAll(Utils.transform(
          this.coveringArrayEngine.generate(builder.build()),
          new Utils.Form<Tuple, TestCase>() {
            @Override
            public TestCase apply(Tuple in) {
              return new TestCase(id[0]++, TestCase.Type.REGULAR, in);
            }
          }
      ));
      if (negativeTestsEnabled) {
        ret.addAll(Utils.transform(
            checker.getViolations(),
            new Utils.Form<Tuple, TestCase>() {
              @Override
              public TestCase apply(Tuple in) {
                return new TestCase(id[0]++, TestCase.Type.VIOLATION, in);
              }
            }
        ));
      }
      return new TestSuite(ret);
    }

    public Builder(CoveringArrayEngine engine) {
      this.coveringArrayEngine = checknotnull(engine);
      this.factors = new LinkedList<Factor>();
    }

    public Builder(int strength) {
      this(new StandardCoveringArrayEngine(strength));
    }

    public Builder() {
      this(2);
    }


    static class GuardedConstraint implements Constraint {
      private final Utils.Predicate<Tuple> predicate;
      private final String                 tag;

      GuardedConstraint(String tag, Utils.Predicate<Tuple> base) {
        this.predicate = checknotnull(base);
        this.tag = checknotnull(tag);
      }

      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        try {
          return this.predicate.apply(new GuardedTuple(tuple));
        } catch (Undef e) {
          throw e.toUndefinedSymbol();
        }
      }

      @Override
      public String tag() {
        return this.tag;
      }
    }

    static class GuardedTuple extends TupleImpl {
      GuardedTuple(Tuple tuple) {
        this.putAll(checknotnull(tuple));
      }

      @Override
      public Object get(Object key) {
        Checks.checkcond(key instanceof String);
        if (!containsKey(key))
          // already checked.
          //noinspection ConstantConditions
          throw new Undef((String) key);
        return super.get(key);
      }
    }

    private static class Undef extends JCUnitException {
      private final String factorName;

      /**
       * Creates an object of this class.
       *
       * @param factorName An error message for this object.
       */
      private Undef(String factorName) {
        super(null, null);
        this.factorName = checknotnull(factorName);
      }

      UndefinedSymbol toUndefinedSymbol() {
        return new UndefinedSymbol(factorName);
      }
    }
  }
}

package com.github.dakusui.jcunit.api;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
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

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public class TestSuiteBuilder {
  private final List<Factor>        factors;
  private       CoveringArrayEngine coveringArrayEngine;
  private List<Utils.Predicate<Tuple>> constraints = new LinkedList<Utils.Predicate<Tuple>>();
  private boolean negativeTestsEnabled;

  public TestSuiteBuilder addConstraint(Utils.Predicate<Tuple> constraint) {
    this.constraints.add(checknotnull(constraint));
    return this;
  }

  public TestSuiteBuilder addFactor(String name, Object... levels) {
    this.addFactor(name, Utils.asList(levels));
    return this;
  }

  public TestSuiteBuilder addFactor(String name, List<?> levels) {
    checknotnull(name);
    checknotnull(levels);
    Factor.Builder b = new Factor.Builder(name);
    for (Object each : levels) {
      b.addLevel(each);
    }
    this.factors.add(b.build());
    return this;
  }

  public TestSuiteBuilder enableNegativeTests() {
    this.negativeTestsEnabled = true;
    return this;
  }

  public TestSuiteBuilder disableNegativeTests() {
    this.negativeTestsEnabled = false;
    return this;
  }

  public List<Tuple> build() {
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
    ConstraintChecker checker =         new SmartConstraintCheckerBase(new Factors(factors)) {
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
    List<Tuple> ret = new LinkedList<Tuple>();
    ret.addAll(this.coveringArrayEngine.generate(builder.build()));
    if (negativeTestsEnabled) {
      ret.addAll(checker.getViolations());
    }
    return ret;
  }

  public TestSuiteBuilder(CoveringArrayEngine engine) {
    this.coveringArrayEngine = checknotnull(engine);
    this.factors = new LinkedList<Factor>();
  }

  public TestSuiteBuilder(int strength) {
    this(new StandardCoveringArrayEngine(strength));
  }

  public TestSuiteBuilder() {
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

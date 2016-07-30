package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.DefaultInstance;
import com.github.dakusui.jcunit.runners.standard.annotations.Uses;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.Arrays;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Checks.checksymbols;

public class SmartConstraintCheckerImpl extends SmartConstraintChecker {
  private final List<Constraint> constraints;

  public SmartConstraintCheckerImpl(
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.TEST_CLASS) final Class<?> testClass,
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.FACTORS) final Factors factors
  ) {
    super(factors);
    this.constraints = Utils.transform(Utils.filter(new TestClass(testClass).getAnnotatedMethods(Condition.class), new Utils.Predicate<FrameworkMethod>() {
      @Override
      public boolean apply(FrameworkMethod in) {
        return in.getAnnotation(Condition.class).constraint();
      }
    }), new Utils.Form<FrameworkMethod, Constraint>() {
      @Override
      public Constraint apply(final FrameworkMethod in) {
        Uses ann =  in.getAnnotation(Uses.class);
        final Uses uses = ann != null
            ? ann
            : DefaultInstance.USES;
        return new Constraint() {
          public List<String> factorNamesInUse = Arrays.asList(uses.value());

          @Override
          public boolean check(Tuple tuple) throws UndefinedSymbol {
            if (uses != null) {
              checksymbols(tuple, uses.value());
            }
            try {
              return (Boolean) in.invokeExplosively(TestCaseUtils.toTestObject(testClass, tuple));
            } catch (Throwable throwable) {
              throw Checks.wrap(throwable);
            }
          }

          @Override
          public String tag() {
            return in.getName();
          }

          @Override
          public List<String> getFactorNamesInUse() {
            if (factorNamesInUse.contains("*")) {
              return factors.getFactorNames();
            }
            return this.factorNamesInUse;
          }
        };
      }
    });
  }

  @Override
  protected List<Constraint> getConstraints() {
    return this.constraints;
  }
}

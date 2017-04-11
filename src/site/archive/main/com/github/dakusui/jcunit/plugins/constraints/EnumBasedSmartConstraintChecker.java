package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.Arrays;
import java.util.List;

/**
 */
public class EnumBasedSmartConstraintChecker extends SmartConstraintChecker {
  protected final String   constraintClassName;

  private final Class<? extends Constraint> constraintClass;

  public EnumBasedSmartConstraintChecker(
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.TEST_CLASS) Class<?> testClass,
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.FACTORS) Factors factors,
      @Param(source = Param.Source.CONFIG) String constraintClassName
  ) {
    super(testClass, factors);
    this.constraintClassName = Checks.checknotnull(constraintClassName);
    this.constraintClass = validateConstraintClass(findClass(this.testClass, this.constraintClassName));
  }

  private Class<?> findClass(Class<?> testClass, String constraintClassName) {
    String ret = constraintClassName;
    if (constraintClassName.startsWith(".")) {
      ret = testClass.getCanonicalName() + constraintClassName.substring(1);
    }
    try {
      return Class.forName(ret);
    } catch (ClassNotFoundException e) {
      throw Checks.wrap(e);
    }
  }

  private Class<? extends Constraint> validateConstraintClass(Class<?> constraintClass) {
    Checks.checknotnull(constraintClass);
    Checks.checktest(Enum.class.isAssignableFrom(constraintClass), "Given argument '%s' is not an enum.", constraintClass.getCanonicalName());
    Checks.checktest(Constraint.class.isAssignableFrom(constraintClass), "Given argument '%s' is not a constraint.", constraintClass.getCanonicalName());
    //noinspection unchecked
    return (Class<? extends Constraint>) constraintClass;
  }

  @Override
  public List<Constraint> getConstraints() {
    return constraints(this.constraintClass);
  }

  @Override
  public ConstraintChecker getFreshObject() {
    return new EnumBasedSmartConstraintChecker(this.testClass, this.factors, this.constraintClassName);
  }

  static private List<Constraint> constraints(Class<? extends Constraint> constraintClass) {
    ////
    // Java8 compiler complains of this line unless this cast is done.
    //noinspection RedundantCast
    return (List<Constraint>) Arrays.asList(constraintClass.getEnumConstants());
  }

}

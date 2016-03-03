package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.*;

/**
 */
final public class SmartConstraintChecker implements ConstraintChecker {

  private final Set<Constraint>             constraintsToBeCovered;
  private final List<Tuple>                 chosenViolations;
  private final Class<? extends Constraint> constraintClass;
  private final Set<Tuple>                  factorLevelsToBeCovered;
  private Tuple regularTestCase = null;

  public SmartConstraintChecker(
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.TEST_CLASS) Class<?> testClass,
      @Param(source = Param.Source.CONTEXT, contextKey = RunnerContext.Key.FACTORS) Factors factors,
      @Param(source = Param.Source.CONFIG) String constraintClassName
  ) {
    this.constraintClass = validateConstraintClass(findClass(
        Checks.checknotnull(testClass),
        Checks.checknotnull(constraintClassName)));
    this.constraintsToBeCovered = new HashSet<Constraint>(this.constraints());
    this.chosenViolations = new LinkedList<Tuple>();
    if (factors == null) {
      ////
      // factorSpace can be null on validation phase.
      this.factorLevelsToBeCovered = null;
    } else {
      this.factorLevelsToBeCovered = new LinkedHashSet<Tuple>(
          factors.generateAllPossibleTuples(1)
      );
    }
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

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (checkTupleAndUpdateViolations(tuple)) {
      if (regularTestCase == null) {
        regularTestCase = tuple;
      }
      if (!factorLevelsToBeCovered.isEmpty()) {
        this.factorLevelsToBeCovered.removeAll(TupleUtils.subtuplesOf(tuple, 1));
      }
      return true;
    }
    return false;
  }

  @Override
  public List<Tuple> getViolations() {
    List<Tuple> ret = new LinkedList<Tuple>(this.chosenViolations);
    for (Tuple factorLevel : this.factorLevelsToBeCovered) {
      ret.add(new Tuple.Builder()
          .putAll(this.regularTestCase)
          .putAll(factorLevel)
          .build());
    }
    return ret;
  }

  @Override
  public List<String> getTags() {
    return Utils.dedup(Utils.transform(
        this.constraints(),
        new Utils.Form<Constraint, String>() {
          @Override
          public String apply(Constraint in) {
            return in.tag();
          }
        }
    ));
  }

  @Override
  public boolean violates(final Tuple tuple, final String constraintTag) {
    Checks.checknotnull(constraintTag);
    Checks.checknotnull(tuple);
    return Utils.filter(
        Utils.filter(
            constraints(),
            new Utils.Predicate<Constraint>() {
              @Override
              public boolean apply(Constraint in) {
                return constraintTag.equals(in.tag());
              }
            }
        ),
        new Utils.Predicate<Constraint>() {
          @Override
          public boolean apply(Constraint in) {
            try {
              return !in.check(tuple);
            } catch (UndefinedSymbol undefinedSymbol) {
              // This shouldn't happen because JCUnit calls violates method only with
              // 'complete' tuple.
              throw Checks.wrap(undefinedSymbol);
            }
          }
        }).size() > 0;
  }


  private boolean checkTupleAndUpdateViolations(Tuple tuple) throws UndefinedSymbol {
    Set<Constraint> violatedConstraints = new HashSet<Constraint>();
    for (Constraint each : this.constraints()) {
      if (!each.check(tuple)) {
        violatedConstraints.add(each);
      }
    }
    if (violatedConstraints.size() == 1) {
      Constraint constraint = violatedConstraints.iterator().next();
      if (constraintsToBeCovered.contains(constraint)) {
        this.constraintsToBeCovered.remove(constraint);
        this.factorLevelsToBeCovered.removeAll(TupleUtils.subtuplesOf(tuple, 1));
        this.chosenViolations.add(tuple);
      }
    }
    return violatedConstraints.isEmpty();
  }

  private List<Constraint> constraints() {
    ////
    // Java8 compiler complains of this line unless this cast is done.
    //noinspection RedundantCast
    return (List<Constraint>)Arrays.asList(this.constraintClass.getEnumConstants());
  }

  private Class<? extends Constraint> validateConstraintClass(Class<?> constraintClass) {
    Checks.checknotnull(constraintClass);
    Checks.checktest(Enum.class.isAssignableFrom(constraintClass), "Given argument '%s' is not an enum.", constraintClass.getCanonicalName());
    Checks.checktest(Constraint.class.isAssignableFrom(constraintClass), "Given argument '%s' is not a constraint.", constraintClass.getCanonicalName());
    //noinspection unchecked
    return (Class<? extends Constraint>) constraintClass;
  }
}

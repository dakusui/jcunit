package com.github.dakusui.jcunit.tests.modules.ipo2;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.GreedyIpo2Optimizer;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.utils.StringUtils.join;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sun.security.krb5.internal.Krb5.DEBUG;

@Ignore
public class Ipo2CoverageTest {
  @Test
  public void givenSize2FactorSpaceWithNoConstraint$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] {};
    List<Tuple> generated = new Ipo2(
        factors = createFactors(2),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertNoMissingTuples(factors, strength, constraints, generated);

    assertEquals("[{A=A1, B=B1}, {A=A1, B=B2}, {A=A2, B=B1}, {A=A2, B=B2}]", generated.toString());
    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize3FactorSpaceWithNoConstraint$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] {};
    List<Tuple> generated = new Ipo2(
        factors = createFactors(3),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertNoMissingTuples(factors, strength, constraints, generated);

    assertEquals("[{A=A1, B=B1, C=C1}, {A=A1, B=B2, C=C2}, {A=A2, B=B1, C=C2}, {A=A2, B=B2, C=C1}]", generated.toString());
    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize2FactorSpaceWithConstraintForA$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] { constraint("A", "A1") };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(2),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);

    assertEquals(2, generated.size());
  }

  @Test
  public void givenSize3FactorSpaceWithConstraintForA$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] { constraint("A", "A1") };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(3),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);

    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize4FactorSpaceWithConstraintForAandB$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] { and(constraint("A", "A1"), constraint("B", "B1")) };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(4),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertNoMissingTuples(factors, strength, constraints, generated);

    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize4FactorSpaceWithConstraintForORedAandB$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] { or(constraint("A", "A1"), constraint("B", "B1")) };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(4),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);

    assertEquals(7, generated.size());
  }

  @Test
  public void givenSize6FactorSpaceWithConstraintForCandORedAandB$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] {
        or(constraint("A", "A1"), constraint("B", "B1")),
        constraint("C", "C1") };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(6),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);

    assertEquals(8, generated.size());
  }

  @Test
  public void givenSize3FactorSpaceWithConstraintForAandC$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] {
        cond(
            constraint("A", "A2"),
            constraint("C", "C11", "C12", "C13", "C14", "C15"),
            cond(
                constraint("A", "A5"),
                constraint("C", "C11", "C12", "C13", "C14", "C15"),
                not(constraint("C", "C25"))
            )),
    };
    Factors factors;
    int strength = 2;
    List<Tuple> generated = new Ipo2(
        factors = createFactors(3, 4, 1, 24),
        strength,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    printFactors(factors);
    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);
    assertEquals(110, generated.size());
  }

  @Test
  public void givenSize4FactorSpaceWithConstraintForAandC$whenGenerateTestSuite$thenWorksFine() {
    Factors factors;
    int strength;
    Constraint[] constraints = new Constraint[] {
        cond(
            constraint("A", "A2"),
            constraint("D", "D11", "D12", "D13", "D14", "D15"),
            cond(
                constraint("A", "A5"),
                constraint("D", "D11", "D12", "D13", "D14", "D15"),
                not(constraint("D", "D25"))
            )),
    };
    List<Tuple> generated = new Ipo2(
        factors = createFactors(4, 4, 1, 3, 24),
        strength = 2,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);
    assertEquals(151 /*or more*/, generated.size());
  }

  @Test
  public void givenSize5FactorSpaceWithConstraintForAandC$whenGenerateTestSuite$thenWorksFine() {
    int strength = 2;
    Constraint[] constraints = new Constraint[] {
        cond(
            constraint("A", "A2"),
            constraint("E", "E11", "E12", "E13", "E14", "E15"),
            cond(
                constraint("A", "A5"),
                constraint("E", "E11", "E12", "E13", "E14", "E15"),
                not(constraint("E", "E25"))
            )),
    };
    Factors factors;
    List<Tuple> generated = new Ipo2(
        factors = createFactors(5, 4, 1, 3, 3, 24),
        strength,
        constraintChecker(constraints),
        new GreedyIpo2Optimizer()
    ).ipo().getGeneratedTuples();

    assertNoMissingTuples(factors, strength, constraints, generated);
    assertNoViolations(constraints, generated);
    assertEquals(156, generated.size());
  }

  private void assertNoViolations(Constraint[] constraints, List<Tuple> generated) {
    for (Constraint eachConstraint : constraints) {
      for (Tuple eachTestCase : generated) {
        try {
          assertTrue(format("%s violates %s", eachTestCase, eachConstraint), eachConstraint.check(eachTestCase));
        } catch (UndefinedSymbol undefinedSymbol) {
          throw Checks.wrap(undefinedSymbol);
        }
      }
    }
  }

  private void assertNoMissingTuples(Factors factors, int strength, Constraint[] constraints, List<Tuple> generated) {
    List<Tuple> tuplesThatShouldHaveBeenCovered = tuplesNotViolatingAnyConstraints(
        asList(constraints),
        tuplesNotCovered(
            factors.asFactorList(),
            strength,
            figureOutCoveredTuples(generated, strength)));
    assertEquals(
        format("Generated tuples are %s (%s).%n Following %s tuples should have been covered but not. ",
            generated.size(),
            generated,
            tuplesThatShouldHaveBeenCovered.size()
        ),
        Collections.emptyList(),
        tuplesThatShouldHaveBeenCovered);
  }

  private Set<Tuple> figureOutCoveredTuples(List<Tuple> testCases, int strength) {
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple eachTestCase : testCases) {
      ret.addAll(TupleUtils.subtuplesOf(eachTestCase, strength));
    }
    return ret;
  }

  private List<Tuple> tuplesNotCovered(List<Factor> factors, int strength, final Set<Tuple> coveredTuples) {
    return Utils.filter(new Factors(factors).generateAllPossibleTuples(strength), new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        return !coveredTuples.contains(in);
      }
    });
  }

  private List<Tuple> tuplesNotViolatingAnyConstraints(final List<Constraint> constraints, List<Tuple> tuples) {
    return Utils.filter(tuples, new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(final Tuple tin) {
        return Utils.filter(constraints, new Utils.Predicate<Constraint>() {
          @Override
          public boolean apply(Constraint cin) {
            try {
              return !cin.check(tin);
            } catch (UndefinedSymbol undefinedSymbol) {
              throw Checks.wrap(undefinedSymbol);
            }
          }
        }).isEmpty();
      }
    });
  }

  private void printFactors(Factors factors) {
    if (DEBUG) {
      for (Factor each : factors) {
        System.out.println(each.name + ":" + each.levels);
      }
    }
  }

  private Constraint not(final Constraint constraint) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        return !constraint.check(tuple);
      }

      @Override
      public String tag() {
        return format("tag-not(%s)", constraint);
      }

      @Override
      public List<String> getFactorNamesInUse() {
        return constraint.getFactorNamesInUse();
      }
    };
  }

  private Constraint cond(final Constraint cond, final Constraint when, final Constraint otherwise) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        if (cond.check(tuple))
          return when.check(tuple);
        return otherwise.check(tuple);
      }

      @Override
      public String tag() {
        return format("tag-cond(%s)", join(",", getFactorNamesInUse()));
      }

      @Override
      public List<String> getFactorNamesInUse() {
        Set<String> ret = new LinkedHashSet<String>();
        for (Constraint each : new Constraint[] { cond, when, otherwise }) {
          ret.addAll(each.getFactorNamesInUse());
        }
        return new ArrayList<String>(ret);
      }

      @Override
      public String toString() {
        return format("'cond(%s)", StringUtils.join(",", new Object[] { cond, when, otherwise }));
      }
    };
  }


  private Constraint or(final Constraint... constraints) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        for (Constraint each : constraints) {
          if (each.check(tuple))
            return true;
        }
        return false;
      }

      @Override
      public String tag() {
        return format("tag-or(%s)", join(",", getFactorNamesInUse()));
      }

      @Override
      public List<String> getFactorNamesInUse() {
        Set<String> ret = new LinkedHashSet<String>();
        for (Constraint each : constraints) {
          ret.addAll(each.getFactorNamesInUse());
        }
        return new ArrayList<String>(ret);
      }

      @Override
      public String toString() {
        return format("'or(%s)", StringUtils.join(",", (Object[]) constraints));
      }
    };
  }

  private Constraint and(final Constraint... constraints) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        for (Constraint each : constraints) {
          if (!each.check(tuple))
            return false;
        }
        return true;
      }

      @Override
      public String tag() {
        return format("tag-and(%s)", join(",", getFactorNamesInUse()));
      }

      @Override
      public List<String> getFactorNamesInUse() {
        Set<String> ret = new LinkedHashSet<String>();
        for (Constraint each : constraints) {
          ret.addAll(each.getFactorNamesInUse());
        }
        return new ArrayList<String>(ret);
      }

      @Override
      public String toString() {
        return format("'and(%s)", StringUtils.join(",", (Object[]) constraints));
      }
    };
  }

  private Constraint constraint(final String factorName, final Object... allowedValues) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        //checkcond(tuple.containsKey(factorName), "missing %s in %s", factorName, tuple);
        return Utils.asList(allowedValues).contains(tuple.get(factorName));
      }

      @Override
      public String tag() {
        return "tag-" + factorName;
      }

      @Override
      public List<String> getFactorNamesInUse() {
        return Collections.singletonList(factorName);
      }

      @Override
      public String toString() {
        return format("'%s' is one of '%s'", factorName, Arrays.toString(allowedValues));
      }
    };
  }

  private Factors createFactors(int numFactors, int... sizes) {
    char factorNameChar = 'A';
    Factors.Builder builder = new Factors.Builder();
    for (int i = 0; i < numFactors; i++) {
      String factorName = Character.toString(factorNameChar);
      if (i >= sizes.length) {
        builder.add(new Factor.Builder(factorName).addLevel(factorName + "1").addLevel(factorName + "2").build());
      } else {
        Factor.Builder b = new Factor.Builder(factorName);
        for (int j = 0; j <= sizes[i]; j++) {
          b.addLevel(factorName + (j + 1));
        }
        builder.add(b.build());
      }
      factorNameChar++;
    }
    return builder.build();
  }

  private ConstraintChecker constraintChecker(Constraint... constraints) {
    return new SimpleConstraintChecker(Utils.asList(constraints));
  }

  static class SimpleConstraintChecker implements ConstraintChecker {
    private final List<Constraint> constraints;

    SimpleConstraintChecker(List<Constraint> constraints) {
      this.constraints = checknotnull(constraints);
    }

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      for (Constraint each : getConstraints()) {
        if (each.check(tuple)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public List<Tuple> getViolations(Tuple regularTestCase) {
      ////
      // This implementation is not capable of generating violations automatically.
      return Collections.emptyList();
    }

    @Override
    public List<String> getTags() {
      return Utils.dedup(Utils.transform(this.getConstraints(),
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
      checknotnull(constraintTag);
      checknotnull(tuple);
      return Utils.filter(
          Utils.filter(
              this.getConstraints(),
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

    @Override
    public List<Constraint> getConstraints() {
      return this.constraints;
    }

    @Override
    public ConstraintChecker getFreshObject() {
      return this;
    }
  }
}

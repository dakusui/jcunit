package com.github.dakusui.jcunit.tests.plugins.caengines.ngipo;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo3.IpoGc;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import org.junit.Test;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.utils.StringUtils.join;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IpoGcTest {
  @Test
  public void givenSize2FactorSpaceWithNoConstraint$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] {};
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(2)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{A=A1, B=B1}, {A=A1, B=B2}, {A=A2, B=B1}, {A=A2, B=B2}]", generated.toString());
    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize3FactorSpaceWithNoConstraint$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] {};
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(3)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{A=A1, B=B1, C=C1}, {A=A1, B=B2, C=C2}, {A=A2, B=B1, C=C2}, {A=A2, B=B2, C=C1}]", generated.toString());
    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize2FactorSpaceWithConstraintForA$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] { constraint("A", "A1") };
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(2)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{A=A1, B=B1}, {A=A1, B=B2}]", generated.toString());
    assertEquals(2, generated.size());
  }

  @Test
  public void givenSize3FactorSpaceWithConstraintForA$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] { constraint("A", "A1") };
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(3)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{B=B1, C=C1, A=A1}, {B=B1, C=C2, A=A1}, {B=B2, C=C1, A=A1}, {B=B2, C=C2, A=A1}]", generated.toString());
    assertEquals(4, generated.size());
  }

  @Test
  public void givenSize4FactorSpaceWithConstraintForAandB$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] { constraint("A", "A1"), constraint("B", "B1") };
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(4)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{C=C1, D=D1, A=A1, B=B1}, {C=C1, D=D2, A=A1, B=B1}, {C=C2, D=D1, A=A1, B=B1}, {C=C2, D=D2, A=A1, B=B1}, {B=B1, A=A1, C=C1, D=D2}]", generated.toString());
    assertEquals(5, generated.size());
  }

  @Test
  public void givenSize4FactorSpaceWithConstraintForORedAandB$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] { or(constraint("A", "A1"), constraint("B", "B1")) };
    List<Tuple> generated = new IpoGc(2, constraintChecker(constraints), createFactors(4)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals("[{C=C1, D=D1, A=A1, B=B1}, {C=C1, D=D2, A=A1, B=B2}, {C=C2, D=D1, A=A1, B=B2}, {C=C2, D=D2, A=A2, B=B1}, {A=A1, C=C1, D=D1, B=B1}]", generated.toString());
    assertEquals(5, generated.size());
  }

  @Test
  public void givenSize6FactorSpaceWithConstraintForCandORedAandB$whenGenerateTestSuite$thenWorksFine() {
    Constraint[] constraints = new Constraint[] {
        or(constraint("A", "A1"), constraint("B", "B1")),
        constraint("C", "C1") };
    List<Tuple> generated = new IpoGc(
        2,
        constraintChecker(constraints),
        createFactors(6)).ipo().getGeneratedTuples();

    assertNoViolations(constraints, generated);
    assertEquals(
        "[" +
            "{D=D1, E=E1, F=F1, A=A1, B=B1, C=C1}, {D=D1, E=E2, F=F2, A=A1, B=B2, C=C1}, " +
            "{D=D2, E=E1, F=F2, A=A2, B=B1, C=C1}, {D=D2, E=E2, F=F1, A=A1, B=B2, C=C1}, " +
            "{A=A1, D=D1, E=E2, F=F2, B=B1, C=C1}, {A=A1, F=F1, D=D2, E=E1, B=B1, C=C1}]",
        generated.toString());
    assertEquals(6, generated.size());
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
        return format("'or(%s)", StringUtils.join(",", new Object[] { constraints }));
      }
    };
  }

  private Constraint constraint(final String factorName, final Object... allowedValues) {
    return new Constraint() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        checkcond(tuple.containsKey(factorName));
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

  private Factors createFactors(int size) {
    char factorNameChar = 'A';
    Factors.Builder builder = new Factors.Builder();
    for (int i = 0; i < size; i++) {
      String factorName = Character.toString(factorNameChar);
      builder.add(new Factor.Builder(factorName).addLevel(factorName + "1").addLevel(factorName + "2").build());
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
    public List<Tuple> getViolations() {
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

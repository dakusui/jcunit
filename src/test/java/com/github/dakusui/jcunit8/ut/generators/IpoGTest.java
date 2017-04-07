package com.github.dakusui.jcunit8.ut.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo.DontCare;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class IpoGTest {
  public static class InvolvedConstraints {
    private static final Constraint constraint_a   = new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        return false;
      }

      @Override
      public List<String> involvedKeys() {
        return singletonList("a");
      }
    };
    private static final Constraint constraint_ab  = new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        return false;
      }

      @Override
      public List<String> involvedKeys() {
        return asList("a", "b");
      }
    };
    private static final Constraint constraint_abc = new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        return false;
      }

      @Override
      public List<String> involvedKeys() {
        return asList("a", "b", "c");
      }
    };

    @Test
    public void givenFullyInvolvedConstraints$whenGetFullyInvolvedConstraints$thenReturned() {
      assertEquals(
          asList(constraint_a, constraint_ab),
          IpoG.getFullyInvolvedConstraints(
              asList("a", "b"),
              asList(
                  constraint_a,
                  constraint_ab,
                  constraint_abc
              )
          ));
    }

    @Test
    public void givenPartiallyInvolvedConstraints$whenGetFullyInvolvedConstraints$thenReturned() {
      assertEquals(
          singletonList(constraint_abc),
          IpoG.getPartiallyInvolvedConstraints(
              asList("a", "b"),
              singletonList(
                  constraint_abc
              )
          ));
    }
  }

  public static class AssignmentsAllowedByPartiallyInvolvedConstraints {
    static class Fixture {
      static final Fixture simple                         = new Fixture(
          new Tuple.Builder().put("a", 1).put("b", 1).build(),
          new LinkedList<Factor>() {{
            add(Factor.create("a", asList(1, 2, 3).toArray()));
            add(Factor.create("b", asList(1, 2, 3).toArray()));
            add(Factor.create("c", asList(1, 2, 3).toArray()));
          }},
          new LinkedList<Constraint>() {{
            add(new Constraint() {
              @Override
              public boolean test(Tuple tuple) {
                return ((int) tuple.get("a")) + ((int) tuple.get("b")) + ((int) tuple.get("c")) <= 4;
              }

              @Override
              public List<String> involvedKeys() {
                return asList("a", "b", "c");
              }
            });
          }}
      );
      static final Fixture twoFreeFactors                 = new Fixture(
          new Tuple.Builder().put("a", 1).build(),
          new LinkedList<Factor>() {{
            add(Factor.create("a", asList(1, 2, 3).toArray()));
            add(Factor.create("b", asList(1, 2, 3).toArray()));
            add(Factor.create("c", asList(1, 2, 3).toArray()));
          }},
          new LinkedList<Constraint>() {{
            add(new Constraint() {
              @Override
              public boolean test(Tuple tuple) {
                return ((int) tuple.get("a")) + ((int) tuple.get("b")) + ((int) tuple.get("c")) <= 4;
              }

              @Override
              public List<String> involvedKeys() {
                return asList("a", "b", "c");
              }
            });
          }}
      );
      static final Fixture violatesFullyCoveredConstraint = new Fixture(
          new Tuple.Builder().put("a", 1).put("b", 1).build(),
          new LinkedList<Factor>() {{
            add(Factor.create("a", asList(1, 2, 3).toArray()));
            add(Factor.create("b", asList(1, 2, 3).toArray()));
            add(Factor.create("c", asList(1, 2, 3).toArray()));
          }},
          new LinkedList<Constraint>() {{
            add(new Constraint() {
              @Override
              public boolean test(Tuple tuple) {
                return ((int) tuple.get("a")) != ((int) tuple.get("b"));
              }

              @Override
              public List<String> involvedKeys() {
                return asList("a", "b");
              }
            });
          }}
      );

      final         Tuple            tuple;
      private final List<Factor>     factors;
      private final List<Constraint> constraints;

      Fixture(Tuple tuple, List<Factor> factors, List<Constraint> constraints) {
        this.tuple = tuple;
        this.factors = factors;
        this.constraints = constraints;
      }
    }

    @Test
    public void givenSimpleExample$whenAssignmentsAllowedByAllPartiallyInvolvedConstraints() {
      Fixture fixture = Fixture.simple;
      List<Tuple> assignments = IpoG.streamAssignmentsAllowedByConstraints(
          fixture.tuple,
          fixture.factors,
          fixture.constraints
      ).collect(toList());

      assertEquals(
          assignments,
          asList(
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 1).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 2).build()
          )
      );
    }

    @Test
    public void givenTwoFreeFactors$whenAssignmentsAllowedByAllPartiallyInvolvedConstraints() {
      Fixture fixture = Fixture.twoFreeFactors;
      List<Tuple> assignments = IpoG.streamAssignmentsAllowedByConstraints(
          fixture.tuple,
          fixture.factors,
          fixture.constraints
      ).collect(toList());

      assertEquals(
          assignments,
          asList(
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 1).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 2).build(),
              new Tuple.Builder().put("a", 1).put("b", 2).put("c", 1).build()
          )
      );
    }

    @Test
    public void givenTupleViolatingFullyCoveredConstraint$whenAssignmentsAllowedByAllPartiallyInvolvedConstraints() {
      Fixture fixture = Fixture.violatesFullyCoveredConstraint;
      List<Tuple> assignments = IpoG.streamAssignmentsAllowedByConstraints(
          fixture.tuple,
          fixture.factors,
          fixture.constraints
      ).collect(toList());

      assertEquals(
          assignments,
          emptyList()
      );
    }
  }

  public static class SatisfiesAllOf {
    @Test
    public void givenEmptyConstraintList$whenSatisfiesAllOf$thenTrue() {
      assertTrue(IpoG.satisfiesAllOf(emptyList()).test(new Tuple.Builder().build()));
    }

    @Test
    public void givenConstraintsAllReturningTrue$whenSatisfiesAllOf$thenTrue() {
      assertTrue(IpoG.satisfiesAllOf(asList(
          new Constraint() {
            @Override
            public boolean test(Tuple tuple) {
              return true;
            }

            @Override
            public List<String> involvedKeys() {
              return emptyList();
            }
          },
          new Constraint() {
            @Override
            public boolean test(Tuple tuple) {
              return true;
            }

            @Override
            public List<String> involvedKeys() {
              return emptyList();
            }
          }
      )).test(new Tuple.Builder().build()));
    }

    @Test
    public void givenConstraintsOneReturningFalse$whenSatisfiesAllOf$thenFalse() {
      assertFalse(IpoG.satisfiesAllOf(asList(
          new Constraint() {
            @Override
            public boolean test(Tuple tuple) {
              return true;
            }

            @Override
            public List<String> involvedKeys() {
              return emptyList();
            }
          },
          new Constraint() {
            @Override
            public boolean test(Tuple tuple) {
              return false;
            }

            @Override
            public List<String> involvedKeys() {
              return emptyList();
            }
          }
      )).test(new Tuple.Builder().build()));
    }
  }

  public static class ReplaceDontCareValues {
    List<Factor>           factors     = asList(
        Factor.create("a", new Object[] { 1, 2, 3 }),
        Factor.create("b", new Object[] { 1, 2, 3 }),
        Factor.create("c", new Object[] { 1, 2, 3 })
    );
    List<Constraint>       constraints = singletonList(
        new Constraint() {
          @Override
          public boolean test(Tuple tuple) {
            int a = (int) tuple.get("a");
            int b = (int) tuple.get("b");
            int c = (int) tuple.get("c");
            return a + b + c < 6;
          }

          @Override
          public List<String> involvedKeys() {
            return asList("a", "b", "c");
          }
        }
    );
    Function<Tuple, Tuple> func        = IpoG.replaceDontCareValuesWithActualLevels(
        factors,
        constraints
    );

    @Test
    public void given$whenReplaceDontCareValuesWithActualLevels$then() {
      assertEquals(
          new Tuple.Builder()
              .put("a", 1)
              .put("b", 2)
              .put("c", 2)
              .build(),
          func.apply(new Tuple.Builder()
              .put("a", 1)
              .put("b", 2)
              .put("c", 2)
              .build()
          ));
    }

    @Test
    public void givenCisDontCare$whenReplaceDontCareValuesWithActualLevels$then() {
      assertEquals(
          new Tuple.Builder()
              .put("a", 1)
              .put("b", 2)
              .put("c", 1)
              .build(),
          func.apply(new Tuple.Builder()
              .put("a", 1)
              .put("b", 2)
              .put("c", DontCare)
              .build()
          ));
    }

    @Test
    public void givenBandCareDontCare$whenReplaceDontCareValuesWithActualLevels$then() {
      assertEquals(
          new Tuple.Builder()
              .put("a", 1)
              .put("b", 1)
              .put("c", 1)
              .build(),
          func.apply(new Tuple.Builder()
              .put("a", 1)
              .put("b", DontCare)
              .put("c", DontCare)
              .build()
          ));
    }

    @Test
    public void givenABandCareDontCare$whenReplaceDontCareValuesWithActualLevels$thenWorksFine() {
      assertEquals(
          new Tuple.Builder()
              .put("a", 1)
              .put("b", 1)
              .put("c", 1)
              .build(),
          func.apply(new Tuple.Builder()
              .put("a", DontCare)
              .put("b", DontCare)
              .put("c", DontCare)
              .build()
          ));
    }

    @Test
    public void givenCisDontCareDontCare$whenAssignmentsForDontCaresUnderConstraints$thenWorksFine() {
      Tuple tuple = new Tuple.Builder()
          .put("a", 1)
          .put("b", 1)
          .put("c", DontCare)
          .build();
      assertEquals(
          asList(
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 1).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 2).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 3).build()
          ),
          IpoG.streamAssignmentsForDontCaresUnderConstraints(
              tuple,
              factors,
              constraints
          ).collect(toList())
      );
    }

    @Test
    public void givenBandCareDontCareDontCare$whenAssignmentsForDontCaresUnderConstraints$thenWorksFine() {
      Tuple tuple = new Tuple.Builder()
          .put("a", 1)
          .put("b", DontCare)
          .put("c", DontCare)
          .build();
      assertEquals(
          asList(
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 1).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 2).build(),
              new Tuple.Builder().put("a", 1).put("b", 1).put("c", 3).build(),
              new Tuple.Builder().put("a", 1).put("b", 2).put("c", 1).build(),
              new Tuple.Builder().put("a", 1).put("b", 2).put("c", 2).build(),
              new Tuple.Builder().put("a", 1).put("b", 3).put("c", 1).build()
          ),
          IpoG.streamAssignmentsForDontCaresUnderConstraints(
              tuple,
              /*
              factors.stream()
                  .filter(factor -> tuple.get(factor.getName()) == DontCare)
                  .collect(toList()),
              constraints
              */
              factors,
              constraints
          ).collect(toList())
      );
    }

    @Test
    public void givenQuadraticEquationExample$whenReplaceDontCares$thenWorksFine() {
      this.factors = asList(
          Factor.create("a", new Object[] { -1, 0, 1, 2, 4 }),
          Factor.create("b", new Object[] { -1, 0, 1, 2, 4, 8 }),
          Factor.create("c", new Object[] { -1, 0, 1, 2, 4 })
      );
      this.constraints = singletonList(
          new Constraint() {
            @Override
            public boolean test(Tuple tuple) {
              int a = (int) tuple.get("a");
              int b = (int) tuple.get("b");
              int c = (int) tuple.get("c");
              return b * b - 4 * c * a >= 0;
            }

            @Override
            public List<String> involvedKeys() {
              return asList("a", "b", "c");
            }
          }
      );
      Tuple tuple = new Tuple.Builder()
          .put("a", 4)
          .put("b", DontCare)
          .put("c", 4)
          .build();
      List<Tuple> result = IpoG.streamAssignmentsForDontCaresUnderConstraints(
          tuple,
          factors,
          constraints
      ).collect(toList());
      assertEquals(
          singletonList(new Tuple.Builder().put("a", 4).put("c", 4).put("b", 8).build()),
          result
      );
    }
  }

  public static class AllPossibleTuples {
    List<Factor> factors = asList(
        Factor.create("a", new Object[] { 1, 2, 3 }),
        Factor.create("b", new Object[] { 1, 2, 3 }),
        Factor.create("c", new Object[] { 1, 2, 3 })
    );

    @Test
    public void given3FactorsWithStrength1$whenAllPossibleTuples$thenAllCovered() {
      assertEquals(
          9,
          IpoG.streamAllPossibleTuples(factors, 1).collect(toList()).size()
      );
      ////
      //Make sure no duplication
      assertEquals(
          9,
          Utils.unique(IpoG.streamAllPossibleTuples(factors, 1).collect(toList())).size()
      );
      IpoG.streamAllPossibleTuples(factors, 1).forEach(tuple -> assertEquals(1, tuple.size()));
    }

    @Test
    public void given3FactorsWithStrength2$whenAllPossibleTuples$thenAllCovered() {
      assertEquals(
          27,
          IpoG.streamAllPossibleTuples(factors, 2).collect(toList()).size()
      );
      ////
      //Make sure no duplication
      assertEquals(
          27,
          Utils.unique(IpoG.streamAllPossibleTuples(factors, 3).collect(toList())).size()
      );
      IpoG.streamAllPossibleTuples(factors, 2).forEach(tuple -> assertEquals(2, tuple.size()));
    }

    @Test
    public void given3FactorsAndStrength3$whenAllPossibleTuples$thenAllCovered() {
      assertEquals(
          27,
          IpoG.streamAllPossibleTuples(factors, 3).collect(toList()).size()
      );
      ////
      //Make sure no duplication
      assertEquals(
          27,
          Utils.unique(IpoG.streamAllPossibleTuples(factors, 3).collect(toList())).size()
      );
      IpoG.streamAllPossibleTuples(factors, 3).forEach(tuple -> assertEquals(3, tuple.size()));
    }
  }

  public static class TupleTest {
    List<Tuple> ts = new LinkedList<Tuple>() {{
      add(new Tuple.Builder().put("a", 1).put("b", 1).put("c", DontCare).build());
      add(new Tuple.Builder().put("a", 1).put("b", 2).build());
    }};

    @Test
    public void givenMatchingDontCareFactor$whenIncompleteTestsToCoverGivenTuple$thenChosen() {
      Tuple σ = new Tuple.Builder().put("b", 1).put("c", 1).build();
      assertEquals(
          singletonList(ts.get(0)),
          IpoG.streamIncompleteTestsToCoverGivenTuple(ts, σ).collect(toList())
      );
    }

    @Test
    public void givenMatchingAbsentFactor$whenIncompleteTestsToCoverGivenTuple$thenChosen() {
      Tuple σ = new Tuple.Builder().put("b", 2).put("c", 1).build();
      assertEquals(
          singletonList(ts.get(1)),
          IpoG.streamIncompleteTestsToCoverGivenTuple(ts, σ).collect(toList())
      );
    }

    @Test
    public void givenNoMatching$whenIncompleteTestsToCoverGivenTuple$thenNotChosen() {
      Tuple σ = new Tuple.Builder().put("b", 3).build();
      assertEquals(
          emptyList(),
          IpoG.streamIncompleteTestsToCoverGivenTuple(ts, σ).collect(toList())
      );
    }
  }

  private static Tuple project(Tuple in, String... factorNames) {
    Tuple.Builder builder = new Tuple.Builder();
    for (String each : factorNames) {
      builder.put(each, in.get(each));
    }
    return builder.build();
  }
}

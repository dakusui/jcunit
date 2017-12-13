package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;
import static java.util.Arrays.asList;

public class CAgenerationUnderConstraints {
  @Test
  public void c1() {
    List<Tuple> generated = generateWithIpoGplus(
        new FactorSpaceSpec("P")
            .addFactors(4, 10)
            .addConstraint(1, 2)
            .addConstraint(3, 2)
            .build(),
        3
    );
    System.out.println("lines(c1)=" + generated.size());
  }

  @Test
  public void c2() {
    List<Tuple> generated = generateWithIpoGplus(
        new FactorSpaceSpec("P")
            .addFactors(4, 10)
            .addConstraint(1, 2)
            .addConstraint(3, 4)
            .build(),
        3
    );
    System.out.println("lines(c1)=" + generated.size());
  }

  @Test
  public void c1_pipelined() {
    Requirement requirement = new Requirement.Builder().withStrength(3).build();
    TestSuite testSuite = Pipeline.Standard.create().execute(
        new Config.Builder(requirement).withJoiner(new CustomJoiner(requirement)).build(),
        new ParameterSpace.Builder()
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p0"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p1"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p2"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p3"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p4"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p5"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p6"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p7"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p8"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p9"))
            .addConstraint(constraint("p1", "p2"))
            .addConstraint(constraint("p3", "p2"))
            .build()
    );
    System.out.println("lines(c1_pipelined)=" + testSuite.size());
  }


  @Test
  public void c10_pipelined() {
    long before = System.currentTimeMillis();
    Requirement requirement = new Requirement.Builder().withStrength(3).build();
    TestSuite testSuite = Pipeline.Standard.create().execute(
        new Config.Builder(requirement).withJoiner(new CustomJoiner(requirement)).build(),
        new ParameterSpace.Builder()
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p0"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p1"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p2"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p3"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p4"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p5"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p6"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p7"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p8"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create("p9"))
            .addConstraint(constraint("p1", "p2"))
            .addConstraint(constraint("p3", "p4"))
            .addConstraint(constraint("p5", "p6"))
            .addConstraint(constraint("p7", "p8"))
            .addConstraint(constraint("p9", "p2"))
            .build()
    );
    System.out.printf("lines(c10_pipelined)=%s;%d[msec]%n", testSuite.size(), System.currentTimeMillis() - before);
  }

  @Test
  public void c10$2_pipelined() {
    int strength = 2;
    SchemafulTupleSet lhs = buildSchemafulTupleSet("p0", strength);
    SchemafulTupleSet rhs = buildSchemafulTupleSet("p1", strength);
    long before = System.currentTimeMillis();
    System.out.printf("lhs size=%s%n", lhs.size());
    System.out.printf("rhs size=%s%n", rhs.size());
    System.out.println("joining");
    SchemafulTupleSet result = new CustomJoiner(new Requirement.Builder().withStrength(strength).build()).doJoin(lhs, rhs);
    long after = System.currentTimeMillis();
    System.out.printf("size=%s;time=%s[msec]%n", result.size(), after - before);
  }

  @Test
  public void c10$3_pipelined() {
    int strength = 2;
    SchemafulTupleSet lhs = buildSchemafulTupleSet("p0", strength);
    SchemafulTupleSet rhs = buildSchemafulTupleSet("p1", strength);
    SchemafulTupleSet rhs2 = buildSchemafulTupleSet("p2", strength);
    long before = System.currentTimeMillis();
    System.out.printf("lhs size=%s%n", lhs.size());
    System.out.printf("rhs size=%s%n", rhs.size());
    System.out.println("joining");
    SchemafulTupleSet joined1 = new CustomJoiner(new Requirement.Builder().withStrength(strength).build()).doJoin(lhs, rhs);
    long after = System.currentTimeMillis();
    System.out.printf(">>>size=%s;time=%s[msec]%n", joined1.size(), after - before);

    long before2 = System.currentTimeMillis();
    System.out.printf("lhs size=%s%n", joined1.size());
    System.out.printf("rhs size=%s%n", rhs2.size());
    System.out.println("joining");
    SchemafulTupleSet result = new CustomJoiner(new Requirement.Builder().withStrength(strength).build()).doJoin(joined1, rhs2);
    long after2 = System.currentTimeMillis();
    System.out.printf(">>>size=%s;time=%s[msec]%n", result.size(), after2 - before2);
  }

  private SchemafulTupleSet buildSchemafulTupleSet(String prefix, int strength) {
    Requirement requirement = new Requirement.Builder().withStrength(strength).build();
    return toSchemafulTupleSet(Pipeline.Standard.create().execute(
        new Config.Builder(
            requirement
        ).withJoiner(new CustomJoiner(requirement)).build(),
        new ParameterSpace.Builder()
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "0"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "1"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "2"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "3"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "4"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "5"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "6"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "7"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "8"))
            .addParameter(ParameterUtils.simple(0, 1, 2, 3).create(prefix + "9"))
            .addConstraint(constraint(prefix + "1", prefix + "2"))
            .addConstraint(constraint(prefix + "3", prefix + "4"))
            .addConstraint(constraint(prefix + "5", prefix + "6"))
            .addConstraint(constraint(prefix + "7", prefix + "8"))
            .addConstraint(constraint(prefix + "9", prefix + "2"))
            .build()
    ));
  }

  private Constraint constraint(String a, String b) {
    return new Constraint() {
      @Override
      public String getName() {
        return String.format("%s<=%s", a, b);
      }

      @Override
      public boolean test(Tuple tuple) {
        return (int) tuple.get(a) <= (int) tuple.get(b);
      }

      @Override
      public List<String> involvedKeys() {
        return asList(a, b);
      }
    };
  }

  private SchemafulTupleSet toSchemafulTupleSet(TestSuite testSuite) {
    return new SchemafulTupleSet.Builder(
        testSuite.getParameterSpace().getParameterNames()
    ).addAll(
        testSuite.stream().map(TestCase::get).collect(Collectors.toList())
    ).build();
  }

  public static class CustomJoiner extends Joiner.Standard {
    public CustomJoiner(Requirement requirement) {
      super(requirement);
    }

    @Override
    protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      long before = System.currentTimeMillis();
      try {
        return super.doJoin(lhs, rhs);
      } finally {
        System.out.println(
            String.format("lhs=%s;%s", lhs.getAttributeNames(), lhs.size())
        );
        System.out.println(
            String.format("rhs=%s;%s", rhs.getAttributeNames(), rhs.size())
        );
        System.out.println(
            System.currentTimeMillis() - before
        );
      }
    }
  }
}

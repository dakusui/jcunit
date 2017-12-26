package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner2;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class StandardJoinSandbox {
  @Rule
  public TestName testName = new TestName();

  @Test
  public void std2_lhs10_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 10, 1);
  }

  @Test
  public void std2_lhs20_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 20, 1);
  }

  @Test
  public void std2_lhs40_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 40, 1);
  }

  @Test
  public void std2_lhs50_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 50, 1);
  }

  @Test
  public void std2_lhs10_rhs3_1() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    runJoin(joinerFactory, 0, 10, 3);
  }

  @Test
  public void std2_lhs20_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 20, 3);
  }

  @Test
  public void std2_lhs30_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 30, 3);
  }

  @Test
  public void std2_lhs40_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 40, 3);
  }

  @Test
  public void std2_lhs50_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 50, 3);
  }

  @Test
  public void std2_lhs10_rhs4_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 10, 4);
  }

  @Test
  public void std2_lhs10_rhs5_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 10, 5);
  }

  @Test
  public void std2_lhs10_rhs3() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 10, 3);
  }

  @Test
  public void std1_lhs10_rhs3() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner::new;
    for (int i = 0; i < 4; i++)
      runJoin(joinerFactory, 0, 10, 3);
  }

  private void runJoin(Function<Requirement, Joiner> joinerFactory, int offset, int lhsWidth, int rhsWidth) {
    SchemafulTupleSet lhs = JoinDataSet.load(3, lhsWidth);
    IntFunction<String> factorNameFormatter = i -> String.format("r%03d", i);
    SchemafulTupleSet rhs = JoinDataSet
        .load(3, 10, factorNameFormatter)
        .project(IntStream.range(offset, offset + rhsWidth).mapToObj(factorNameFormatter).collect(toList()));
    JoinSession session = new JoinSession.Builder(3)
        .with(joinerFactory)
        .lhs(lhs)
        .rhs(rhs)
        .build();
    session.execute();
    session.verify();
    System.out.printf("%s: size=%d; width=%d; time=%s[msec]; %s (input=lhs=%s; rhs=%s)%n",
        testName.getMethodName(), session.result.size(), session.result.width(),
        session.time(), session.result.getAttributeNames().size(),
        lhs.size(),
        rhs.size()
    );
    //session.result.forEach(System.out::println);
  }

  @After
  public void after() {
    System.setProperty("debug", "no");
  }
}

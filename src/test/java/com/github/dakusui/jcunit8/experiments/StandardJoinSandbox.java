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
      runJoin(i, joinerFactory, 0, 10, 1, testName.getMethodName());
  }

  @Test
  public void std2_lhs20_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 20, 1, testName.getMethodName());
  }

  @Test
  public void std2_lhs40_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 40, 1, testName.getMethodName());
  }

  @Test
  public void std2_lhs50_rhs1_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 50, 1, testName.getMethodName());
  }

  @Test
  public void std2_lhs10_rhs3_1() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    runJoin(3, joinerFactory, 0, 10, 3, testName.getMethodName());
  }

  @Test
  public void std2_lhs20_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 20, 3, testName.getMethodName());
  }

  @Test
  public void std2_lhs30_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 30, 3, testName.getMethodName());
  }

  @Test
  public void std2_lhs40_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 40, 3, testName.getMethodName());
  }

  @Test
  public void std2_lhs50_rhs3_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 50, 3, testName.getMethodName());
  }

  @Test
  public void std2_lhs10_rhs4_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 10, 4, testName.getMethodName());
  }

  @Test
  public void std2_lhs10_rhs5_4() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 10, 5, testName.getMethodName());
  }

  @Test
  public void std2_lhs10_rhs3() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner2::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 10, 3, testName.getMethodName());
  }

  @Test
  public void std1_lhs10_rhs3() {
    Function<Requirement, Joiner> joinerFactory = StandardJoiner::new;
    for (int i = 0; i < 4; i++)
      runJoin(i, joinerFactory, 0, 10, 3, testName.getMethodName());
  }

  static void runJoin(int doi, Function<Requirement, Joiner> joinerFactory, int offset, int lhsWidth, int rhsWidth, String testName) {
    SchemafulTupleSet lhs = JoinDataSet.load(doi, lhsWidth);
    SchemafulTupleSet rhs = loadRhs(offset, rhsWidth, 3);
    JoinSession session = doJoin(lhs, rhs, doi, joinerFactory);
    System.out.printf("%s: size=%d; width=%d; time=%s[msec]; %s (input=lhs=%s; rhs=%s)%n",
        testName, session.result.size(), session.result.width(),
        session.time(), session.result.getAttributeNames().size(),
        lhs.size(),
        rhs.size()
    );
    // session.result.forEach(System.out::println);
  }

  static JoinSession doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs, int doi, Function<Requirement, Joiner> joinerFactory) {
    JoinSession session = new JoinSession.Builder(doi)
        .with(joinerFactory)
        .lhs(lhs)
        .rhs(rhs)
        .build();
    session.execute();
    session.verify();
    return session;
  }

  static SchemafulTupleSet loadRhs(int offset, int rhsWidth, int doi) {
    IntFunction<String> factorNameFormatter = i -> String.format("r%03d", i);
    return JoinDataSet
        .load(doi, 100, factorNameFormatter)
        .project(IntStream.range(offset, offset + rhsWidth).mapToObj(factorNameFormatter).collect(toList()));
  }

  static SchemafulTupleSet loadLhs(int offset, int rhsWidth, int doi) {
    IntFunction<String> factorNameFormatter = i -> String.format("l%03d", i);
    return JoinDataSet
        .load(doi, 100, factorNameFormatter)
        .project(IntStream.range(offset, offset + rhsWidth).mapToObj(factorNameFormatter).collect(toList()));
  }

  @After
  public void after() {
    System.setProperty("debug", "no");
  }

}

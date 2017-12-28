package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.github.dakusui.jcunit8.experiments.StandardJoinSandbox.loadLhs;
import static java.util.stream.Collectors.toList;

public class ScratchJoinSandbox {
  @Rule
  final public TestName testName = new TestName();
  private long before;

  @Before
  public void before() {
    this.before = System.currentTimeMillis();
  }

  @Test
  public void doi2_lhs10_rhs10$1() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            lhs(),
            rhs(0)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$2() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                lhs(),
                rhs(0)
            ).result,
            rhs(10)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$3() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    lhs(),
                    rhs(0)
                ).result,
                rhs(10)
            ).result,
            rhs(20)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$4() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        lhs(),
                        rhs(0)
                    ).result,
                    rhs(10)
                ).result,
                rhs(20)
            ).result,
            rhs(30)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$5() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            lhs(),
                            rhs(0)
                        ).result,
                        rhs(10)
                    ).result,
                    rhs(20)
                ).result,
                rhs(30)
            ).result,
            rhs(40)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$6() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            doJoin(
                                lhs(),
                                rhs(0)
                            ).result,
                            rhs(10)
                        ).result,
                        rhs(20)
                    ).result,
                    rhs(30)
                ).result,
                rhs(40)
            ).result,
            rhs(50)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$7() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            doJoin(
                                doJoin(
                                    lhs(),
                                    rhs(0)
                                ).result,
                                rhs(10)
                            ).result,
                            rhs(20)
                        ).result,
                        rhs(30)
                    ).result,
                    rhs(40)
                ).result,
                rhs(50)
            ).result,
            rhs(60)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$8() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            doJoin(
                                doJoin(
                                    doJoin(
                                        lhs(),
                                        rhs(0)
                                    ).result,
                                    rhs(10)
                                ).result,
                                rhs(20)
                            ).result,
                            rhs(30)
                        ).result,
                        rhs(40)
                    ).result,
                    rhs(50)
                ).result,
                rhs(60)
            ).result,
            rhs(70)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$9() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            doJoin(
                                doJoin(
                                    doJoin(
                                        doJoin(
                                            lhs(),
                                            rhs(0)
                                        ).result,
                                        rhs(10)
                                    ).result,
                                    rhs(20)
                                ).result,
                                rhs(30)
                            ).result,
                            rhs(40)
                        ).result,
                        rhs(50)
                    ).result,
                    rhs(60)
                ).result,
                rhs(70)
            ).result,
            rhs(80)
        ),
        System.currentTimeMillis() - before
    );
  }

  @Test
  public void doi2_lhs10_rhs10$10() {
    System.out.printf(
        "%s: %s: totalTime=%s%n",
        testName.getMethodName(),
        doJoin(
            doJoin(
                doJoin(
                    doJoin(
                        doJoin(
                            doJoin(
                                doJoin(
                                    doJoin(
                                        doJoin(
                                            doJoin(
                                                lhs(),
                                                rhs(0)
                                            ).result,
                                            rhs(10)
                                        ).result,
                                        rhs(20)
                                    ).result,
                                    rhs(30)
                                ).result,
                                rhs(40)
                            ).result,
                            rhs(50)
                        ).result,
                        rhs(60)
                    ).result,
                    rhs(70)
                ).result,
                rhs(80)
            ).result,
            rhs(90)
        ),
        System.currentTimeMillis() - before
    );
  }

  private JoinSession doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    return StandardJoinSandbox.doJoin(lhs, rhs, 2, StandardJoiner::new);
  }

  private SchemafulTupleSet lhs() {
    return loadLhs(0, 10, 2);
  }

  private SchemafulTupleSet rhs(int offset) {
    return _loadRhs(offset, 2);
  }

  private static SchemafulTupleSet _loadRhs(int offset, @SuppressWarnings("SameParameterValue") int doi) {
    IntFunction<String> factorNameFormatter = i -> String.format("r%03d", i + offset);
    return JoinDataSet
        .load(doi, 100, factorNameFormatter)
        .project(IntStream.range(0, 10).mapToObj(factorNameFormatter).collect(toList()));
  }
}

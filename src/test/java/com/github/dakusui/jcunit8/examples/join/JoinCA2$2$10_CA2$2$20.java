package com.github.dakusui.jcunit8.examples.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.join;

public class JoinCA2$2$10_CA2$2$20 {
  private List<Tuple> lhs    = generateWithIpoGplus(new CompatFactorSpaceSpecForExperiments("LHS").addFactors(2, 10).build(), 2);
  private List<Tuple> rhs    = generateWithIpoGplus(new CompatFactorSpaceSpecForExperiments("RHS").addFactors(2, 20).build(), 2);
  private List<Tuple> joined = join(lhs, rhs, Joiner.Standard::new, 2);
  private List<Tuple> joined2 = join(rhs, lhs, Joiner.Standard::new, 2);

  @Test
  public void printLhs() {
    printCA(lhs);
  }

  @Test
  public void printRhs() {
    printCA(rhs);
  }

  @Test
  public void printJoined() {
    printCA(joined);
  }

  @Test
  public void printJoined2() {
    printCA(joined2);
  }

  private static <T> void printCA(List<T> list) {
    AtomicInteger i = new AtomicInteger(0);
    list.forEach(each -> System.out.printf("%s:%s%n",i.incrementAndGet(), each));
  }
}

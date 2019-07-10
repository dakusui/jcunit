package com.github.dakusui.jcunit8.experiments.sandboxes;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpec;
import com.github.dakusui.jcunit8.extras.normalizer.FactorSpaceSpec;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.StopWatch;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;

public class JoinSandbox3 {
  class CoveringArrayGenerator implements Callable<List<Tuple>> {
    final private FactorSpace factorSpace;
    final private int         strength;

    CoveringArrayGenerator(FactorSpace factorSpace, int strength) {
      this.factorSpace = factorSpace;
      this.strength = strength;
    }

    @Override
    public List<Tuple> call() {
      return CoveringArrayGenerationUtils.generateWithIpoGplus(factorSpace, strength);
    }
  }

  @Test
  public void test() throws ExecutionException, InterruptedException {
    FactorSpaceSpec spec1 = new CompatFactorSpaceSpec("A").addFactors(2, 40);
    FactorSpaceSpec spec2 = new CompatFactorSpaceSpec("B").addFactors(2, 30);
    FactorSpaceSpec spec3 = new CompatFactorSpaceSpec("C").addFactors(2, 30);

    ExecutorService threadPool = ForkJoinPool.commonPool();
    try {
      ////
      // warm-up and validation.
      assertCoveringArray(
          generateCoveringArrayByCascading(spec1, spec2, spec3, threadPool),
          CoveringArrayGenerationUtils.mergeFactorSpaces(
              CoveringArrayGenerationUtils.mergeFactorSpaces(spec1.build(), spec2.build()),
              spec3.build()
          ),
          2
      );
      System.out.println("size,time");
      for (int i = 0; i < 10; i++) {
        StopWatch stopWatch = new StopWatch();
        int size = generateCoveringArrayByCascading(spec1, spec2, spec3, threadPool).size();
        System.out.printf("%d,%d%n", size, stopWatch.get());
      }
    } finally {
      threadPool.shutdownNow();
    }
  }

  private List<Tuple> generateCoveringArrayByCascading(FactorSpaceSpec spec1, FactorSpaceSpec spec2, FactorSpaceSpec spec3, ExecutorService threadPool) throws InterruptedException, ExecutionException {
    Future<List<Tuple>> ca1 = threadPool.submit(new CoveringArrayGenerator(spec1.build(), 2));
    Future<List<Tuple>> ca2 = threadPool.submit(new CoveringArrayGenerator(spec2.build(), 2));
    Future<List<Tuple>> ca3 = threadPool.submit(new CoveringArrayGenerator(spec3.build(), 2));

    return CoveringArrayGenerationUtils.join(
        CoveringArrayGenerationUtils.join(ca1.get(), ca2.get(), Joiner.Standard::new, 2),
        ca3.get(),
        Joiner.Standard::new, 2
    );
  }
}

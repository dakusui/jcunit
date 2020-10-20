package com.github.dakusui.jcunit8.experiments.sandboxes;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils;
import com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils.StopWatch;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils.assertCoveringArray;

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
    FactorSpaceSpec spec1 = new FactorSpaceSpec("A").addFactors(2, 40);
    FactorSpaceSpec spec2 = new FactorSpaceSpec("B").addFactors(2, 30);
    FactorSpaceSpec spec3 = new FactorSpaceSpec("C").addFactors(2, 30);

    ExecutorService threadPool = ForkJoinPool.commonPool();
    try {
      ////
      // warm-up and validation.
      assertCoveringArray(
          generateCoveringArrayByCascading(spec1, spec2, spec3, threadPool),
          CoveringArrayGenerationUtils.mergeFactorSpaces(
              CoveringArrayGenerationUtils.mergeFactorSpaces(spec1.toFactorSpace(), spec2.toFactorSpace()),
              spec3.toFactorSpace()
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
    Future<List<Tuple>> ca1 = threadPool.submit(new CoveringArrayGenerator(spec1.toFactorSpace(), 2));
    Future<List<Tuple>> ca2 = threadPool.submit(new CoveringArrayGenerator(spec2.toFactorSpace(), 2));
    Future<List<Tuple>> ca3 = threadPool.submit(new CoveringArrayGenerator(spec3.toFactorSpace(), 2));

    return CoveringArrayGenerationUtils.join(
        CoveringArrayGenerationUtils.join(ca1.get(), ca2.get(), Joiner.Standard::new, 2),
        ca3.get(),
        Joiner.Standard::new, 2
    );
  }
}

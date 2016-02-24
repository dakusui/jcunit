package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;

import java.io.PrintStream;

public interface Report<C extends Metrics> {
  void submit(C coverage);

  class Printer<C extends Metrics> implements Report<C> {
    private final PrintStream ps;

    Printer(PrintStream ps) {
      this.ps = Checks.checknotnull(ps);
    }

    @Override
    public void submit(C coverage) {
      for (Metrics.Metric<?> each : coverage.metrics()) {
        this.ps.println(each);
      }
    }
  }
}

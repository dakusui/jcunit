package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.coverage.Metrics.Metric;

import java.io.PrintStream;

public interface Report {
  void submit(Metrics<?> coverage);

  class Printer implements Report {
    private final PrintStream ps;

    public Printer(PrintStream ps) {
      this.ps = Checks.checknotnull(ps);
    }

    @Override
    public void submit(Metrics<?> coverage) {
      for (Metric<?> each : coverage.metrics()) {
        this.ps.println(each.name());
        for (Metric.Value eachValue : Metric.Utils.valuesOf(each)) {
          if (double.class == eachValue.type) {
            this.ps.println(String.format("  %20s - %2.2f", eachValue.name, eachValue.value));
          } else {
            this.ps.println(String.format("  %20s - %s", eachValue.name, eachValue.value));
          }
        }
      }
    }
  }
}

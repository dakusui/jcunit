package com.github.dakusui.peerj.testbases;

import java.util.function.Function;

import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.greaterThanOrEqualTo;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class StopWatch<T, R> implements Function<T, R> {
  final         Function<T, R>      function;
  private final Function<T, String> inputSummarizer;
  private final Function<R, String> outputSummarizer;
  long      timeSpent = -1;
  R         result    = null;
  T         input     = null;
  Throwable exception = null;

  public StopWatch(Function<T, R> function, Function<T, String> inputSummarizer, Function<R, String> outputSummarizer) {
    this.function = function;
    this.inputSummarizer = inputSummarizer;
    this.outputSummarizer = outputSummarizer;
  }

  @Override
  public R apply(T in) {
    this.input = requireNonNull(in);
    long before = System.currentTimeMillis();
    try {
      return this.result = requireNonNull(this.function.apply(in));
    } catch (Error | RuntimeException e) {
      this.exception = e;
      throw e;
    } finally {
      if (this.result != null)
        timeSpent = System.currentTimeMillis() - before;
    }
  }

  public String report() {
    if (this.exception == null) {
      require(this.timeSpent, greaterThanOrEqualTo((long) 0));
      return format(
          "function:%s;input:%s;output:%s;%s[msec]",
          this.function,
          this.inputSummarizer.apply(this.input),
          this.outputSummarizer.apply(this.result),
          this.timeSpent);
    } else
      return format("function:%s;input:%s;FAILED:%s",
          this.function,
          this.inputSummarizer.apply(this.input),
          this.exception.getMessage());
  }
}

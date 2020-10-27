package com.github.dakusui.peerj;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.utils.CasaUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.NOP;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.greaterThanOrEqualTo;
import static com.github.dakusui.peerj.CasaExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.CasaExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static com.github.dakusui.peerj.utils.CasaUtils.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public abstract class CasaExperimentParameterized extends CasaExperimentBase {

  public static class StopWatch<T, R> implements Function<T, R> {
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

  private final Spec spec;

  public CasaExperimentParameterized(Spec spec) {
    this.spec = spec;
  }

  public static List<Spec> parameters(Predicate<CasaUtils> cond, List<Integer> strengths) {
    CasaUtils[] values = values();
    return Arrays.stream(values)
        .filter(cond)
        .flatMap(each -> strengths.stream()
            .map(t -> new Spec.Builder()
                .strength(t)
                .algorithm(IPOG)
                .constraintHandlingMethod(FORBIDDEN_TUPLES)
                .def(each)
                .build()))
        .collect(toList());
  }

  @Before
  public void before() {
    //System.setErr(NOP);
  }

  @Test
  public void acts() {
    StopWatch<CasaExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductActsExperiment", (CasaExperimentParameterized self) -> self.conductActsExperiment(self.spec.def)),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile("acts", "none"), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void joinWithSimplePartitioner() {
    StopWatch<CasaExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductJoinExperiment", (CasaExperimentParameterized self) -> self.conductJoinExperiment(self.spec.def, simplePartitioner())),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile("join", "simple"), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void joinWithStandardPartitioner() {
    StopWatch<CasaExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductJoinExperiment", (CasaExperimentParameterized self) -> self.conductJoinExperiment(self.spec.def, standardPartitioner(spec.strength))),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile("join", "standard"), Stream.of(stopWatch.report()));
    }
  }

  public File resultFile(String generationMode, String joinMode) {
    File baseDir = baseDirFor(this.spec.def, this.spec.strength, generationMode, joinMode).getParentFile();
    //noinspection ResultOfMethodCallIgnored
    baseDir.mkdirs();
    return new File(baseDir, "result.txt");
  }

  public void writeTo(File file, Stream<String> stream) {
    try {
      try (OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
        stream.peek(System.out::println).forEach(line -> write(writer, String.format("%s%n", line)));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void write(OutputStreamWriter writer, String line) {
    try {
      writer.write(line);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected ConstraintHandlingMethod constraintHandlingMethod() {
    return spec.constraintHandlingMethod;
  }

  @Override
  protected Algorithm algorithm() {
    return spec.algorithm;
  }

  @Override
  protected int strength() {
    return spec.strength;
  }
}

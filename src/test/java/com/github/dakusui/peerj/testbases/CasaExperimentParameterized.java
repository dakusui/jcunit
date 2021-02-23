package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.pcond.functions.Printables;
import com.github.dakusui.peerj.utils.PeerJUtils2;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.peerj.utils.CasaDataSet;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.testbases.ExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.testbases.ExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static com.github.dakusui.peerj.utils.CasaDataSet.values;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public abstract class CasaExperimentParameterized extends CasaExperimentBase {

  private final Spec spec;

  public CasaExperimentParameterized(Spec spec) {
    this.spec = spec;
  }

  public static List<Spec> parameters(Predicate<CasaDataSet> cond, List<Integer> strengths) {
    CasaDataSet[] values = values();
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
        Printables.function("conductActsExperiment", (CasaExperimentParameterized self) -> self.conductActsExperimentForCasa(self.spec.def)),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile("acts", "none"), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  @Test
  public void joinWithSimplePartitioner() {
    StopWatch<CasaExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printables.function("conductJoinExperiment", (CasaExperimentParameterized self) -> self.conductJoinExperimentForCasa(self.spec.def, PeerJUtils2.simplePartitioner())),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile("join", "simple"), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  @Ignore
  @Test
  public void joinWithStandardPartitioner() {
    StopWatch<CasaExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printables.function("conductJoinExperiment", (CasaExperimentParameterized self) -> self.conductJoinExperimentForCasa(self.spec.def, PeerJUtils2.standardPartitioner(spec.strength))),
        (CasaExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile("join", "standard"), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  public File resultFile(String generationMode, String partitionerName) {
    return PeerJUtils2.resultFile("casa-" + this.spec.def, this.spec.strength, generationMode, partitionerName);
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

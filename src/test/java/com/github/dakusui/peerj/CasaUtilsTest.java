package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.acts.Acts;
import com.github.dakusui.peerj.utils.CasaUtils;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Test;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class CasaUtilsTest {
  {
    System.setErr(new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
      }
    }));
  }

  public static final int    STRENGTH           = 3;
  public static final String ALGORITHM          = "ipog";
  public static final String CONSTRAINT_HANDLER = "forbiddentuples";

  @Test
  public void testInsurance() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "prefix",
        -1,
        asList(
            "2",
            "14",
            "2 13 17 31 3 6 6 2 2 2 2 11 2 5").iterator(),
        singletonList("0").iterator());
    System.out.println(casaModel);
  }


  @Test
  public void testBanking2() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "prefix",
        -1,
        asList(
            "2",
            "15",
            "4 2 2 2 2 2 2 2 2 2 2 2 2 2 2").iterator(),
        asList(
            "3",
            "2",
            "- 2 - 20",
            "2",
            "- 20 - 1",
            "2",
            "- 20 - 3").iterator());
    System.out.println(casaModel);
  }


  @Test
  public void readBanking2FromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        -1
    );
    System.out.println(casaModel);
  }

  @Test
  public void generateCoveringArrayForBanking2FromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        STRENGTH
    );
    Acts.generateWithActs(
        new File("target/acts/cassa"),
        casaModel.factorSpace,
        casaModel.strength,
        ALGORITHM,
        CONSTRAINT_HANDLER)
        .forEach(System.err::println);
  }

  @Test
  public void generateCoveringArrayForInsuranceFromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Insurance",
        "prefix",
        STRENGTH
    );
    Acts.generateWithActs(
        new File("target/acts/cassa"),
        casaModel.factorSpace,
        casaModel.strength,
        ALGORITHM,
        CONSTRAINT_HANDLER)
        .forEach(System.out::println);
  }

  @Test
  public void generateCoveringArrayByActs() {
    for (CasaUtils each : CasaUtils.values()) {
      long before = System.currentTimeMillis();
      List<Tuple> result = conductActsExperiment(each);
      System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
    }
  }

  private List<Tuple> conductActsExperiment(CasaUtils def) {
    Requirement requirement = new Requirement.Builder()
        .withStrength(STRENGTH)
        .build();
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength()
    );
    return Acts.generateWithActs(
        new File("target/acts/casa-" + def + "-acts-" + currentThread().getId()),
        casaModel.factorSpace,
        casaModel.strength,
        ALGORITHM,
        CONSTRAINT_HANDLER)
        .stream()
        .peek(System.err::println)
        .collect(toList());
  }

  @Test
  public void generateCoveringArrayFileByJoining() {
    for (CasaUtils each : CasaUtils.values()) {
      long before = System.currentTimeMillis();
      try {
        List<Tuple> result = conductJoinExperiment(each);
        System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
      } catch (NotCombinatorialJoinApplicable | ProcessStreamer.Failure e) {
        System.out.println("name:" + each + ",FAILED");
      }
    }
  }

  private List<Tuple> conductJoinExperiment(CasaUtils def) {
    Requirement requirement = new Requirement.Builder()
        .withStrength(STRENGTH)
        .build();
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength()
    );
    return simplePartitioner()
        .apply(casaModel.factorSpace)
        .parallelStream()
        .peek(factorSpace -> System.err.println("->" + factorSpace))
        .peek(factorSpace -> {
          if (factorSpace.getFactorNames().isEmpty())
            throw new NotCombinatorialJoinApplicable(def.toString());
        })
        .map(factorSpace -> Acts.generateWithActs(
            new File("target/acts/casa-" + def + "-join-" + currentThread().getId()),
            factorSpace,
            casaModel.strength,
            ALGORITHM,
            CONSTRAINT_HANDLER))
        .map(arr -> arr.stream().map(t -> rename(t, currentThread().getId())).collect(toList()))
        .map(SchemafulTupleSet::fromTuples)
        .reduce(new Joiner.WeakenProduct(requirement))
        .orElseThrow(NoSuchElementException::new)
        .stream()
        .peek(System.err::println)
        .collect(toList());
  }

  private static Tuple rename(Tuple tuple, long i) {
    Tuple.Builder b = Tuple.builder();
    tuple.keySet().forEach(k -> b.put(String.format("P%02d_%s", i, k), tuple.get(k)));
    return b.build();
  }

  private static Partitioner simplePartitioner() {
    return new Partitioner() {
      @Override
      public List<FactorSpace> apply(FactorSpace factorSpace) {
        factorSpace.getFactorNames();
        List<String> keysInConstraints = factorSpace.getConstraints()
            .stream()
            .flatMap(c -> c.involvedKeys().stream())
            .distinct()
            .collect(toList());
        List<String> keysNotInConstraints = factorSpace.getFactorNames()
            .stream()
            .filter(k -> !keysInConstraints.contains(k))
            .collect(toList());

        return asList(
            projectFactorSpace(factorSpace, keysInConstraints, factorSpace.getConstraints()),
            projectFactorSpace(factorSpace, keysNotInConstraints, emptyList())
        );
      }

      private FactorSpace projectFactorSpace(FactorSpace factorSpace, List<String> keysInConstraints, List<Constraint> constraints) {
        return FactorSpace.create(
            keysInConstraints
                .stream()
                .filter(k -> factorSpace.getFactorNames().contains(k))
                .map(factorSpace::getFactor)
                .collect(toList()),
            constraints);
      }
    };
  }

  private static class NotCombinatorialJoinApplicable extends RuntimeException {
    NotCombinatorialJoinApplicable(String message) {
      super(message);
    }
  }
}

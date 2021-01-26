package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.utils.ProcessStreamerUtils;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.dakusui.crest.utils.printable.Functions.size;
import static com.github.dakusui.pcond.Preconditions.*;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public enum PeerJUtils2 {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(PeerJUtils2.class);

  public static final long SESSION_ID = System.currentTimeMillis();

  public static File baseDirFor(String datasetName, int strength, String generationMode, String partitionerName) {
    return new File(
        "target/acts/" +
            SESSION_ID + "/" +
            datasetName + "/" +
            generationMode + "-" +
            partitionerName + "/" +
            strength);
  }

  public static Tuple renameFactors(Tuple tuple, long partitionId) {
    Tuple.Builder b = Tuple.builder();
    tuple.keySet().forEach(k -> b.put(format("P%02d_%s", partitionId, k), tuple.get(k)));
    return b.build();
  }

  public static Partitioner evenPartitioner() {
    return new Partitioner() {
      @Override
      public List<FactorSpace> apply(FactorSpace factorSpace) {
        requireArgument(factorSpace.getFactors().size() % 2, equalTo(0));
        List<FactorSpace> ret = new ArrayList<>(2);
        List<Factor> leftFactors = new ArrayList<>(factorSpace.getFactors().size());

        List<Factor> rightFactors = new ArrayList<>(factorSpace.getFactors().size() / 2 + 1);
        for (int i = 0; i < factorSpace.getFactors().size() / 2; i += 1) {
          leftFactors.add(factorSpace.getFactor(factorSpace.getFactors().get(i).getName()));
          rightFactors.add(factorSpace.getFactor(factorSpace.getFactors().get(i + factorSpace.getFactors().size() / 2).getName()));
        }
        List<Constraint> allConstraints = new ArrayList<>(factorSpace.getConstraints());
        Set<String> leftFactorNames = leftFactors.stream().map(Factor::getName).collect(toSet());
        Set<String> rightFactorNames = rightFactors.stream().map(Factor::getName).collect(toSet());
        List<Constraint> leftConstraints = factorSpace.getConstraints().stream()
            .filter(c -> leftFactorNames.containsAll(c.involvedKeys()))
            .collect(toList());
        List<Constraint> rightConstraints = factorSpace.getConstraints().stream()
            .filter(c -> rightFactorNames.containsAll(c.involvedKeys()))
            .collect(toList());
        System.out.println(leftConstraints.size());
        System.out.println(rightConstraints.size());
        System.out.println(allConstraints.size());
        allConstraints.removeAll(leftConstraints);
        System.out.println(allConstraints.size());
        allConstraints.removeAll(rightConstraints);
        System.out.println(allConstraints.size());
        requireState(allConstraints, isEmpty());
        ret.add(FactorSpace.create(leftFactors, leftConstraints, factorSpace.baseStrength(), factorSpace.relationStrength()));
        ret.add(FactorSpace.create(rightFactors, rightConstraints, factorSpace.baseStrength(), factorSpace.relationStrength()));
        return ret;
      }

      @Override
      public String name() {
        return "even";
      }
    };
  }

  public static Partitioner simplePartitioner() {
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

      @Override
      public String name() {
        return "simple";
      }

      private FactorSpace projectFactorSpace(FactorSpace factorSpace, List<String> keysInConstraints, List<Constraint> constraints) {
        return FactorSpace.create(
            keysInConstraints
                .stream()
                .filter(k -> factorSpace.getFactorNames().contains(k))
                .map(factorSpace::getFactor)
                .collect(toList()),
            constraints,
            factorSpace.baseStrength(),
            factorSpace.relationStrength());
      }
    };
  }

  public static Partitioner standardPartitioner(int strength) {
    return new Partitioner.Standard(requirement(strength)) {
      @Override
      public List<FactorSpace> apply(FactorSpace factorSpace) {
        return require(super.apply(factorSpace), transform(size()).check(greaterThan(1)));
      }

      @Override
      public String name() {
        return "standard";
      }
    };
  }

  public static Requirement requirement(int strength) {
    return new Requirement.Builder()
        .withStrength(strength)
        .build();
  }

  public static void write(OutputStreamWriter writer, String line) {
    try {
      writer.write(line);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static File resultFile(String datasetName, int strength, String generationMode, String partitionerName) {
    File baseDir = baseDirFor(datasetName, strength, generationMode, partitionerName).getParentFile();
    //noinspection ResultOfMethodCallIgnored
    baseDir.mkdirs();
    return new File(new File(baseDir, Integer.toString(strength)), "result.txt");
  }

}

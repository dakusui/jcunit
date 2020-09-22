package com.github.dakusui.jcunit8.experiments.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.experiments.peerj.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.experiments.peerj.CompatFactorSpaceSpecForExperiments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public enum JoinExperimentUtils {
  ;
  private static final Logger LOGGER   = LoggerFactory.getLogger(JoinExperimentUtils.class);
  public static final  File   BASE_DIR = new File("src/test/resources/pregenerated-cas");

  public static List<Tuple> loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
      FactorSpaceSpecForExperiments factorSpaceSpec,
      int strength,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory) {
    File baseDir = BASE_DIR;
    if (new File(baseDir, Objects.toString(strength)).mkdirs())
      LOGGER.debug(String.format("Directory '%s/%s' was created.", baseDir, strength));
    return loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
        factorSpaceSpec,
        strength,
        baseDir,
        factory);
  }

  private static List<Tuple> loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
      FactorSpaceSpecForExperiments factorSpaceSpec,
      int strength,
      File baseDir,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory) {
    return loadPregeneratedCoveringArrayFor(factorSpaceSpec, strength, baseDir)
        .orElseGet(() -> {
          CompatFactorSpaceSpecForExperiments abstractModel = convertToAbstractModel(factorSpaceSpec);
          LOGGER.debug(String.format("Generating a covering array for %s(strength=%s) ...", factorSpaceSpec, strength));
          long before = System.currentTimeMillis();
          List<Tuple> ret = factory.apply(abstractModel.build(), strength);
          long after = System.currentTimeMillis();
          LOGGER.debug("Generated.");
          LOGGER.debug("Saving...");
          saveTo(dataFileFor(abstractModel, strength, baseDir), ret);
          saveTo(timeFileFor(abstractModel, strength, baseDir), after - before);
          LOGGER.debug("Saved.");
          return ret.stream()
              .map(t -> convert(t, factorSpaceSpec.prefix()))
              .collect(toList());
        });
  }

  public static long timeSpentForGeneratingCoveringArray(
      FactorSpaceSpecForExperiments factorSpaceSpec,
      int strength,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory
  ) {
    File baseDir = BASE_DIR;
    // Ensure the array is pre-generated already.
    loadPregeneratedOrGenerateAndSaveCoveringArrayFor(factorSpaceSpec, strength, baseDir, factory);
    CompatFactorSpaceSpecForExperiments abstractModel = convertToAbstractModel(factorSpaceSpec);
    try {
      return loadFrom(timeFileFor(abstractModel, strength, baseDir));
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private static CompatFactorSpaceSpecForExperiments convertToAbstractModel(FactorSpaceSpecForExperiments in) {
    CompatFactorSpaceSpecForExperiments ret = new CompatFactorSpaceSpecForExperiments("PREFIX");
    in.factorSpecs().forEach(entry -> ret.addFactors(entry.getKey(), entry.getValue()));
    if (!in.constraints().isEmpty()) {
      in.constraints().forEach(ret::addConstraint);
      ret.constraintSetName(in.constraintSetName().orElseThrow(RuntimeException::new));
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  private static Optional<List<Tuple>> loadPregeneratedCoveringArrayFor(FactorSpaceSpecForExperiments factorSpaceSpec, int strength, File baseDir) {
    try {
      LOGGER.debug("Loading pre-generated covering array for " + factorSpaceSpec.createSignature() + ":strength=" + strength);
      try {
        if (!timeFileFor(factorSpaceSpec, strength, baseDir).exists())
          throw new FileNotFoundException("Time-file doesn't exist");
        return Optional.of(
            ((List<Tuple>) loadFrom(dataFileFor(factorSpaceSpec, strength, baseDir)))
                .stream()
                .map(each -> convert(each, factorSpaceSpec.prefix()))
                .collect(toList()));
      } finally {
        LOGGER.debug("Loaded");
      }
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.debug(
          format("Pregenerated file for '%s' was not available because: %s.",
              factorSpaceSpec.toString(),
              e.getMessage()));
      return Optional.empty();
    }
  }

  private static File dataFileFor(FactorSpaceSpecForExperiments factorSpaceSpec, int strength, File baseDir) {
    return fileFor(strength, baseDir, signatureOf(factorSpaceSpec));
  }

  private static File timeFileFor(FactorSpaceSpecForExperiments factorSpaceSpec, int strength, File baseDir) {
    return fileFor(strength, baseDir, signatureOf(factorSpaceSpec) + ".time");
  }

  private static File fileFor(int strength, File baseDir, String filename) {
    return new File(new File(baseDir, Objects.toString(strength)), filename);
  }

  private static String signatureOf(FactorSpaceSpecForExperiments factorSpaceSpec) {
    return factorSpaceSpec.createSignature();
  }

  private static void saveTo(File file, Object data) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
      oos.writeObject(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T loadFrom(File file) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      return (T) ois.readObject();
    }
  }

  private static Tuple convert(Tuple in, String newPrefix) {
    Tuple.Builder b = Tuple.builder();
    in.forEach((k, v) -> b.put(
        requireArgument(k, key -> key.startsWith("p")).replace("p", newPrefix),
        v
    ));
    return b.build();
  }

  private static <T> T requireArgument(T value, Predicate<T> cond) {
    if (!cond.test(value))
      throw new IllegalArgumentException(format("'%s' must satisfy %s", value, cond));
    return value;
  }
}

package com.github.dakusui.jcunit8.experiments.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public enum JoinExperimentUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(JoinExperimentUtils.class);

  public static List<Tuple> loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
      FactorSpaceSpecForExperiments factorSpaceSpec,
      int strength,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory) {
    File baseDir = new File("src/test/resources/pregenerated-cas");
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
          List<Tuple> ret = factory.apply(abstractModel.build(), strength);
          LOGGER.debug("Generated.");
          LOGGER.debug("Saving...");
          saveTo(fileFor(abstractModel, strength, baseDir), ret);
          LOGGER.debug("Saved.");
          return ret.stream()
              .map(t -> convert(t, factorSpaceSpec.prefix()))
              .collect(toList());
        });
  }

  private static CompatFactorSpaceSpecForExperiments convertToAbstractModel(FactorSpaceSpecForExperiments in) {
    CompatFactorSpaceSpecForExperiments ret = new CompatFactorSpaceSpecForExperiments("PREFIX");
    in.factorSpecs().forEach(entry -> ret.addFactors(entry.getKey(), entry.getValue()));
    return ret;
  }

  private static Optional<List<Tuple>> loadPregeneratedCoveringArrayFor(FactorSpaceSpecForExperiments factorSpaceSpec, int strength, File baseDir) {
    try {
      LOGGER.debug("Loading pre-generated covering array for " + factorSpaceSpec.createSignature() + ":strength=" + strength);
      try {
        return Optional.of(
            loadFrom(fileFor(factorSpaceSpec, strength, baseDir)).stream()
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

  private static File fileFor(FactorSpaceSpecForExperiments factorSpaceSpec, int strength, File baseDir) {
    return new File(new File(baseDir, Objects.toString(strength)), signatureOf(factorSpaceSpec));
  }

  private static String signatureOf(FactorSpaceSpecForExperiments factorSpaceSpec) {
    return factorSpaceSpec.createSignature();
  }

  private static void saveTo(File file, List<Tuple> tuples) {
    try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
      oos.writeObject(tuples);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private static List<Tuple> loadFrom(File file) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      return (List<Tuple>) ois.readObject();
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

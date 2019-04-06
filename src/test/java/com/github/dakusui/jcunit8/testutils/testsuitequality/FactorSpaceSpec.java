package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FactorSpaceSpec {
  private final SortedMap<Integer, Integer> factorSpecs = new TreeMap<>((o1, o2) -> o2 - o1);
  private final String                      prefix;

  public FactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  public FactorSpaceSpec addFactor(int numLevels, int numFactors) {
    this.factorSpecs.put(numLevels, numFactors);
    return this;
  }

  public FactorSpace build() {
    AtomicInteger index = new AtomicInteger(0);
    return FactorSpace.create(
        new LinkedList<Factor>() {{
          factorSpecs.keySet().stream()
              .flatMap((Integer numLevels) -> IntStream.range(0, factorSpecs.get(numLevels))
                  .mapToObj(i -> Factor.create(
                      format("%s-%02d", prefix, index.getAndIncrement()),
                      IntStream.range(0, numLevels)
                          .boxed().collect(toList())
                          .toArray())))
              .forEach(this::add);
        }},
        Collections.emptyList()
    );
  }

  @Override
  public String toString() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(" ", format("%s[", this.prefix), "]"));
  }

  private String signature() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(","));
  }

  public static List<Tuple> loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
      FactorSpaceSpec factorSpaceSpec,
      int strength,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory) {
    File baseDir = new File("src/test/resources/pregenerated-cas");
    if (new File(baseDir, Objects.toString(strength)).mkdirs())
      System.out.println(String.format("Directory '%s/%s' was created.", baseDir, strength));
    return loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
        factorSpaceSpec,
        strength,
        baseDir,
        factory);
  }

  private static List<Tuple> loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
      FactorSpaceSpec factorSpaceSpec,
      int strength,
      File baseDir,
      BiFunction<FactorSpace, Integer, List<Tuple>> factory) {
    return loadPregeneratedCoveringArrayFor(factorSpaceSpec, strength, baseDir)
        .orElseGet(() -> {
          FactorSpaceSpec abstractModel = convertToAbstractModel(factorSpaceSpec);
          System.out.println(String.format("Generating a covering array for %s(strength=%s)", factorSpaceSpec, strength));
          List<Tuple> ret = factory.apply(abstractModel.build(), strength);
          saveTo(fileFor(abstractModel, strength, baseDir), ret);
          return ret.stream()
              .map(t -> convert(t, factorSpaceSpec.prefix))
              .collect(toList());
        });
  }

  private static FactorSpaceSpec convertToAbstractModel(FactorSpaceSpec in) {
    FactorSpaceSpec ret = new FactorSpaceSpec("PREFIX");
    in.factorSpecs.forEach(ret::addFactor);
    return ret;
  }

  private static Optional<List<Tuple>> loadPregeneratedCoveringArrayFor(FactorSpaceSpec factorSpaceSpec, int strength, File baseDir) {
    try {
      System.out.println("Loading pre-generated covering array for " + factorSpaceSpec.signature() + ":strength=" + strength);
      try {
        return Optional.of(
            loadFrom(fileFor(factorSpaceSpec, strength, baseDir)).stream()
                .map(each -> convert(each, factorSpaceSpec.prefix))
                .collect(toList()));
      } finally {
        System.out.println("Loaded");
      }
    } catch (IOException | ClassNotFoundException e) {
      System.out.println(
          format("Pregenerated file for '%s' was not available because: %s.",
              factorSpaceSpec.toString(),
              e.getMessage()));
      return Optional.empty();
    }
  }

  private static File fileFor(FactorSpaceSpec factorSpaceSpec, int strength, File baseDir) {
    return new File(new File(baseDir, Objects.toString(strength)), signatureOf(factorSpaceSpec));
  }

  private static String signatureOf(FactorSpaceSpec factorSpaceSpec) {
    return factorSpaceSpec.signature();
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
        requireArgument(k, key -> key.startsWith("PREFIX")).replace("PREFIX", newPrefix),
        v
    ));
    return b.build();
  }

  private static <T> T requireArgument(T value, Predicate<T> cond) {
    if (!cond.test(value))
      throw new IllegalArgumentException(format("'%s' must satisfy %s", value, cond));
    return value;
  }

  public static void main(String... args) {
    System.out.println(new FactorSpaceSpec("F").addFactor(2, 3).addFactor(4, 2).toString());
  }

  public int numFactors() {
    return this.factorSpecs.size();
  }
}

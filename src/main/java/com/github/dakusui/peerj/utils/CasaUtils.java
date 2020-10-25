package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.model.NormalizedConstraint;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.github.dakusui.crest.utils.printable.Functions.size;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.greaterThan;
import static com.github.dakusui.pcond.functions.Predicates.transform;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public enum CasaUtils {
  BANKING1("IBM", "Banking1"),
  BANKING2("IBM", "Banking2"),
  COMP_PROTOCOL("IBM", "CommProtocol"),
  CONCURRENCY("IBM", "Concurrency"),
  HEALTHCARE1("IBM", "Healthcare1"),
  HEALTHCARE2("IBM", "Healthcare2"),
  HEALTHCARE3("IBM", "Healthcare3"),
  HEALTHCARE4("IBM", "Healthcare4"),
  INSURANCE("IBM", "Insurance"),
  NETWORK_MGMT("IBM", "NetworkMgmt"),
  PROCESSOR_COMM1("IBM", "ProcessorComm1"),
  PROCESSOR_COMM2("IBM", "ProcessorComm2"),
  SERVICES("IBM", "Services"),
  STORAGE1("IBM", "Storage1"),
  STORAGE2("IBM", "Storage2"),
  STORAGE3("IBM", "Storage3"),
  STORAGE4("IBM", "Storage4"),
  STORAGE5("IBM", "Storage5"),
  STORAGE6("IBM", "SystemMgmt"),
  TELECOM("IBM", "Telecom"),
  BENCHMARK_APACHE("Real", "benchmark_apache"),
  BENCHMARK_BUGZILLA("Real", "benchmark_bugzilla"),
  BENCHMARK_GCC("Real", "benchmark_gcc"),
  BENCHMARK_SPINS("Real", "benchmark_spins"),
  BENCHMARK_SPINV("Real", "benchmark_spinv"),
  TCAS("Real", "tcas"),
  BENCHMARK_1("Synthetic", "benchmark_1"),
  BENCHMARK_2("Synthetic", "benchmark_2"),
  BENCHMARK_3("Synthetic", "benchmark_3"),
  BENCHMARK_4("Synthetic", "benchmark_4"),
  BENCHMARK_5("Synthetic", "benchmark_5"),
  BENCHMARK_6("Synthetic", "benchmark_6"),
  BENCHMARK_7("Synthetic", "benchmark_7"),
  BENCHMARK_8("Synthetic", "benchmark_8"),
  BENCHMARK_9("Synthetic", "benchmark_9"),
  BENCHMARK_10("Synthetic", "benchmark_10"),
  BENCHMARK_11("Synthetic", "benchmark_11"),
  BENCHMARK_12("Synthetic", "benchmark_12"),
  BENCHMARK_13("Synthetic", "benchmark_13"),
  BENCHMARK_14("Synthetic", "benchmark_14"),
  BENCHMARK_15("Synthetic", "benchmark_15"),
  BENCHMARK_16("Synthetic", "benchmark_16"),
  BENCHMARK_17("Synthetic", "benchmark_17"),
  BENCHMARK_18("Synthetic", "benchmark_18"),
  BENCHMARK_19("Synthetic", "benchmark_19"),
  BENCHMARK_20("Synthetic", "benchmark_20"),
  BENCHMARK_21("Synthetic", "benchmark_21"),
  BENCHMARK_22("Synthetic", "benchmark_22"),
  BENCHMARK_23("Synthetic", "benchmark_23"),
  BENCHMARK_24("Synthetic", "benchmark_24"),
  BENCHMARK_25("Synthetic", "benchmark_25"),
  BENCHMARK_26("Synthetic", "benchmark_26"),
  BENCHMARK_27("Synthetic", "benchmark_27"),
  BENCHMARK_28("Synthetic", "benchmark_28"),
  BENCHMARK_29("Synthetic", "benchmark_29"),
  BENCHMARK_30("Synthetic", "benchmark_30"),
  ;

  public static final long   SESSION_ID = System.currentTimeMillis();
  private final       String categoryName;
  private final       String modelName;

  CasaUtils(String categoryName, String modelName) {
    this.categoryName = categoryName;
    this.modelName = modelName;
  }

  public static File baseDirFor(CasaUtils def, int strength, String generationMode, String joinMode) {
    return new File(
        "target/acts/" + SESSION_ID + "/casa-" + def + "/" + generationMode + "-" +
            joinMode + "/" +
            strength + "/" +
            currentThread().getId());
  }

  public static Tuple renameFactors(Tuple tuple, long i) {
    Tuple.Builder b = Tuple.builder();
    tuple.keySet().forEach(k -> b.put(format("P%02d_%s", i, k), tuple.get(k)));
    return b.build();
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
            constraints);
      }
    };
  }

  public static Partitioner standardPartitioner(int strength) {
    return new Partitioner.Standard(requirement(strength)) {
      @Override
      public List<FactorSpace> apply(FactorSpace factorSpace) {
        return require(super.apply(factorSpace), transform(size()).check(greaterThan(1)));
      }
    };
  }

  public static Requirement requirement(int strength) {
    return new Requirement.Builder()
        .withStrength(strength)
        .build();
  }

  public static class CasaModel {
    public final int         strength;
    public final FactorSpace factorSpace;

    CasaModel(int strength, FactorSpace factorSpace) {
      this.strength = strength;
      this.factorSpace = factorSpace;
    }

    @Override
    public String toString() {
      return String.format("t=%s;%s", strength, factorSpace);
    }
  }

  /**
   * Creates a {@link CasaModel} object from a preset data set.
   *
   * @param def              An instance of {@link CasaUtils}.
   * @param factorNamePrefix {@code "L"}, {@code "param"}, etc.
   * @param strength         E.g., 2, 3, ...
   * @return A created {@link CasaModel} object.
   */
  public static CasaModel readCasaModel(CasaUtils def, String factorNamePrefix, int strength) {
    return readCasaModel(def.categoryName, def.modelName, factorNamePrefix, strength);
  }

  /**
   * Creates a {@link CasaModel} object from a preset data set.
   *
   * @param categoryName     E.g., {@code IBM}, {@code Real}, etc.
   * @param modelName        E.g., {@code Banking1}, {@code CommProtocol}, etc.
   * @param factorNamePrefix {@code "L"}, {@code "param"}, etc.
   * @param strength         E.g., 2, 3, ...
   * @return A created {@link CasaModel} object.
   */
  public static CasaModel readCasaModel(String categoryName, String modelName, String factorNamePrefix, int strength) {
    return readCasaModel(
        factorNamePrefix,
        strength,
        fileReaderFor(modelFileFor(categoryName, modelName)),
        fileReaderFor(constraintFileFor(categoryName, modelName))
    );
  }

  public static CasaModel readCasaModel(String factorNamePrefix, int strength, Iterator<String> modelData, Iterator<String> constraintModel) {
    int defaultStrength = readDefaultStrength(modelData);
    if (strength < 1)
      strength = defaultStrength;
    return new CasaModel(strength, readFactorSpace(factorNamePrefix, modelData, constraintModel));
  }

  private static int readDefaultStrength(Iterator<String> modelData) {
    return readInt(modelData);
  }

  private static FactorSpace readFactorSpace(String factorNamePredix, Iterator<String> modelData, Iterator<String> constraintModel) {
    List<Factor> factors = readFactors(factorNamePredix, modelData);
    return FactorSpace.create(
        factors,
        readConstraints(constraintModel, factors));
  }


  private static List<Factor> readFactors(String factorNamePrefix, Iterator<String> modelData) {
    int numFactors = readNumFactors(modelData);
    List<Integer> data = parseIntList(readString(modelData));
    Checks.checkcond(numFactors == data.size());
    AtomicInteger i = new AtomicInteger(0);
    return data.stream()
        .map((Integer numLevels) -> createFactor(format("%s-%s", factorNamePrefix, i.getAndIncrement()), numLevels))
        .collect(toList());
  }

  private static Factor createFactor(String name, int numLevels) {
    return Factor.create(name, IntStream.range(0, numLevels).boxed().toArray());
  }

  private static int readNumFactors(Iterator<String> modelData) {
    return readInt(modelData);
  }

  private static List<Constraint> readConstraints(Iterator<String> modelData, List<Factor> factors) {
    int numConstraints = readNumConstraints(modelData);
    List<NormalizedConstraint> constraints = new ArrayList<>(numConstraints);
    while (modelData.hasNext()) {
      constraints.add(readConstraint(modelData, factors));
    }
    Checks.checkcond(numConstraints == constraints.size());
    return constraints.stream().map(c -> (Constraint) c).collect(toList());
  }

  private static int readNumTerms(Iterator<String> modelData) {
    return readInt(modelData);
  }

  private static int readNumConstraints(Iterator<String> modelData) {
    return readInt(modelData);
  }

  private static NormalizedConstraint readConstraint(Iterator<String> modelData, List<Factor> factors) {
    int numTerms = readNumTerms(modelData);
    List<NormalizedConstraint> terms = new ArrayList<>(numTerms);
    Iterator<String> i = Arrays.asList(readString(modelData).split(" +")).iterator();
    while (i.hasNext()) {
      terms.add(readTerm(i, factors));
    }
    Checks.checkcond(numTerms == terms.size());
    return ConstraintUtils.or(terms.toArray(new NormalizedConstraint[0]));
  }

  private static NormalizedConstraint readTerm(Iterator<String> terms, List<Factor> factors) {
    String sign = readString(terms);
    int value = readInt(terms);
    Map.Entry<String, Object> entry = entryFor(value, factors);
    if (sign.equals("+"))
      return ConstraintUtils.eq(entry.getKey(), Objects.toString(entry.getValue()));
    else if (sign.equals("-"))
      return ConstraintUtils.neq(entry.getKey(), Objects.toString(entry.getValue()));
    else
      throw new RuntimeException("Unknown sign: '" + sign + "' was found");
  }

  private static Map.Entry<String, Object> entryFor(int value, List<Factor> factors) {
    AtomicInteger i = new AtomicInteger(0);
    return factors.stream()
        .filter(f -> i.get() <= value && value < i.addAndGet(f.getLevels().size()))
        .findFirst()
        .map(f -> entry(f.getName(), f.getLevels().get(value - (i.get() - f.getLevels().size()))))
        .orElseThrow(NoSuchElementException::new);
  }

  private static int readInt(Iterator<String> data) {
    return parseInt(readString(data).trim());
  }

  private static String readString(Iterator<String> data) {
    Checks.checkcond(data.hasNext());
    return data.next();
  }

  private static List<Integer> parseIntList(String line) {
    return Arrays.stream(line.split(" +")).map(Integer::parseInt).collect(toList());
  }

  private static int parseInt(String line) {
    return Integer.parseInt(line);
  }

  private static <K, V> Map.Entry<K, V> entry(K key, V value) {
    return new Map.Entry<K, V>() {
      @Override
      public K getKey() {
        return key;
      }

      @Override
      public V getValue() {
        return value;
      }

      @Override
      public V setValue(V value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  private static Iterator<String> fileReaderFor(File file) {
    try {
      return new LinkedList<String>() {
        {
          BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
          String line;
          while ((line = reader.readLine()) != null)
            this.add(line);
        }
      }.iterator();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static File constraintFileFor(String categoryName, String modelName) {
    return new File(categoryDirectoryFor(categoryName), String.format("%s.constraints", modelName));
  }

  private static File categoryDirectoryFor(String categoryName) {
    return new File(String.format("%s/models/%s/2way", JoinExperimentUtils.TEST_RESOURCES_DIR, categoryName));
  }

  private static File modelFileFor(String categoryName, String modelName) {
    return new File(categoryDirectoryFor(categoryName), String.format("%s.model", modelName));
  }

  public static class NotCombinatorialJoinApplicable extends RuntimeException {
    public NotCombinatorialJoinApplicable(String message) {
      super(message);
    }
  }
}

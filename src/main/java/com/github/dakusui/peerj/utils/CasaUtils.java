package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.PeerJUtils2;
import com.github.dakusui.peerj.ext.base.ConstraintUtils;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public enum CasaUtils {
  ;

  public static File baseDirFor(CasaDataSet def, int strength, String generationMode, String partitionerName) {
    return PeerJUtils2.baseDirFor("casa-" + def, strength, generationMode, partitionerName);
  }

  /**
   * Creates a {@link CasaDataSet.CasaModel} object from a preset data set.
   *
   * @param def              An instance of {@link CasaDataSet}.
   * @param factorNamePrefix {@code "L"}, {@code "param"}, etc.
   * @param strength         E.g., 2, 3, ...
   * @return A created {@link CasaDataSet.CasaModel} object.
   */
  public static CasaDataSet.CasaModel readCasaModel(CasaDataSet def, String factorNamePrefix, int strength) {
    return readCasaModel(def.categoryName, def.modelName, factorNamePrefix, strength);
  }

  /**
   * Creates a {@link CasaDataSet.CasaModel} object from a preset data set.
   *
   * @param categoryName     E.g., {@code IBM}, {@code Real}, etc.
   * @param modelName        E.g., {@code Banking1}, {@code CommProtocol}, etc.
   * @param factorNamePrefix {@code "L"}, {@code "param"}, etc.
   * @param strength         E.g., 2, 3, ...
   * @return A created {@link CasaDataSet.CasaModel} object.
   */
  public static CasaDataSet.CasaModel readCasaModel(String categoryName, String modelName, String factorNamePrefix, int strength) {
    return readCasaModel(
        factorNamePrefix,
        strength,
        fileReaderFor(modelFileFor(categoryName, modelName)),
        fileReaderFor(constraintFileFor(categoryName, modelName))
    );
  }

  public static CasaDataSet.CasaModel readCasaModel(String factorNamePrefix, int strength, Iterator<String> modelData, Iterator<String> constraintModel) {
    int defaultStrength = readDefaultStrength(modelData);
    if (strength < 1)
      strength = defaultStrength;
    return new CasaDataSet.CasaModel(strength, readFactorSpace(factorNamePrefix, modelData, constraintModel));
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
    List<NormalizableConstraint> constraints = new ArrayList<>(numConstraints);
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

  private static NormalizableConstraint readConstraint(Iterator<String> modelData, List<Factor> factors) {
    int numTerms = readNumTerms(modelData);
    List<NormalizableConstraint> terms = new ArrayList<>(numTerms);
    Iterator<String> i = Arrays.asList(readString(modelData).split(" +")).iterator();
    while (i.hasNext()) {
      terms.add(readTerm(i, factors));
    }
    Checks.checkcond(numTerms == terms.size());
    return ConstraintUtils.or(terms.toArray(new NormalizableConstraint[0]));
  }

  private static NormalizableConstraint readTerm(Iterator<String> terms, List<Factor> factors) {
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
}

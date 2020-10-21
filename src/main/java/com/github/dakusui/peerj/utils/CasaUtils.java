package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.model.NormalizedConstraint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public enum CasaUtils {
  ;

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
    if (sign.equals("-"))
      return ConstraintUtils.eq(entry.getKey(), Objects.toString(entry.getValue()));
    else if (sign.equals("+"))
      return ConstraintUtils.neq(entry.getKey(), Objects.toString(entry.getValue()));
    else
      throw new RuntimeException("Unknown sign: '" + sign + "' was found");
  }

  private static Map.Entry<String, Object> entryFor(int value, List<Factor> factors) {
    AtomicInteger i = new AtomicInteger(0);
    return factors.stream()
        .filter(f -> i.get() <= value && value < i.addAndGet(f.getLevels().size()) )
        .findFirst()
        .map(f -> entry(f.getName(), f.getLevels().get(value - (i.get() - f.getLevels().size()))))
        .orElseThrow(NoSuchElementException::new);
  }

  private static int readInt(Iterator<String> data) {
    return parseInt(readString(data));
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
      public V setValue(Object value) {
        throw new UnsupportedOperationException();
      }
    };
  }
}

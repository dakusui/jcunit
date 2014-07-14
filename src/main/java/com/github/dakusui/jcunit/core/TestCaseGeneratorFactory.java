package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class TestCaseGeneratorFactory {
  public static final TestCaseGeneratorFactory INSTANCE = new TestCaseGeneratorFactory();

  public TestCaseGenerator createTestCaseGenerator(Class<?> klazz) {
    Utils.checknotnull(klazz);
    Factors factors = loadFactors(klazz);
    TestCaseGeneration testCaseGenerationAnn = getTestCaseGenerationAnnotation(
        klazz);
    Generator generatorAnn = testCaseGenerationAnn.generator();
    TestCaseGenerator generator = createTestCaseGeneratorInstance(generatorAnn);
    Constraint constraintAnn = testCaseGenerationAnn.constraint();
    ConstraintManager constraintManager = createConstraintManager(
        constraintAnn);
    ////
    // Wire objects.
    constraintManager.setFactors(factors);
    constraintManager.init(Utils.processParams(constraintAnn.params()));
    generator.setFactors(factors);
    generator.setConstraintManager(constraintManager);
    generator.init(Utils.processParams(generatorAnn.params()));
    return generator;
  }

  Factors loadFactors(Class<?> klazz) {
    // //
    // Initialize the factor levels for every '@FactorField' annotated field.
    Field[] fields = Utils.getAnnotatedFields(klazz, FactorField.class);
    Factors.Builder factorsBuilder = new Factors.Builder();
    List<String> errors = new LinkedList<String>();
    for (Field f : fields) {
      FactorLoader factorLoader = new FactorLoader(f);
      FactorLoader.ValidationResult validationResult = factorLoader.validate();
      if (!validationResult.isValid()) {
        errors.add(f.getName() + ":" + validationResult.getErrorMessage());
      }
      Factor factor = factorLoader.getFactor();
      factorsBuilder.add(factor);
    }
    if (!errors.isEmpty()) {
      errors.add(0, "One or more factors failed to be initialized.");
      throw new JCUnitException(Utils.join("\n\t", errors.toArray()));
    }

    // //
    // Instantiates the test array generator.
    Factors factors;
    factors = factorsBuilder.build();
    return factors;
  }

  TestCaseGeneration getTestCaseGenerationAnnotation(Class<?> klazz) {
    TestCaseGeneration ret;
    if (klazz.isAnnotationPresent(TestCaseGeneration.class)) {
      ret = klazz.getAnnotation(TestCaseGeneration.class);
    } else {
      ret = new TestCaseGeneration() {
        @Override public Generator generator() {
          return Utils
              .getDefaultValueOfAnnotation(TestCaseGeneration.class,
                  "generator");
        }

        @Override public Constraint constraint() {
          return Utils
              .getDefaultValueOfAnnotation(TestCaseGeneration.class,
                  "constraint");
        }

        @Override public boolean equals(Object o) {
          return super.equals(o);
        }

        @Override public int hashCode() {
          return super.hashCode();
        }

        @Override public String toString() {
          return super.toString();
        }

        @Override public Class<? extends Annotation> annotationType() {
          return TestCaseGeneration.class;
        }
      };
    }
    return ret;
  }

  TestCaseGenerator createTestCaseGeneratorInstance(
      Generator generatorAnn) {
    return Utils.createNewInstanceUsingNoParameterConstructor(
        generatorAnn.value());
  }

  ConstraintManager createConstraintManager(Constraint constraintAnn) {
    return Utils.createNewInstanceUsingNoParameterConstructor(
        constraintAnn.value());
  }

}

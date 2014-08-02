package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.Constraint;
import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class TupleGeneratorFactory {
  public static final TupleGeneratorFactory INSTANCE = new TupleGeneratorFactory();

  /**
   * Creates a {@code SchemafulTupleGenerator} using annotations attached to the class
   * for which the returned generator is created.
   */
  public TupleGenerator createTupleGeneratorFromClass(
      Class<?> klazz) {
    Utils.checknotnull(klazz);
    TupleGeneration tupleGenerationAnn = getTupleGenerationAnnotation(
        klazz);
    return createSchemafulTupleGenerator(klazz, tupleGenerationAnn);
  }

  public TupleGenerator createTupleGeneratorForField(
      Field field) {
    Utils.checknotnull(field);
    TupleGeneration tupleGenerationAnn = getTupleGenerationAnnotation(
        field);
    return createSchemafulTupleGenerator(field.getType(),
        tupleGenerationAnn);
  }

  private TupleGenerator createSchemafulTupleGenerator(Class<?> klazz,
      TupleGeneration tupleGenerationAnn) {
    Factors factors = loadFactors(klazz);
    Generator generatorAnn = tupleGenerationAnn.generator();
    TupleGenerator generator = createSchemafulTupleGeneratorInstance(
        generatorAnn);
    Constraint constraintAnn = tupleGenerationAnn.constraint();
    ConstraintManager constraintManager = createConstraintManager(
        constraintAnn);
    ////
    // Wire objects.
    constraintManager.setFactors(factors);
    constraintManager.init(ConfigUtils.processParams(constraintManager.parameterTypes(), constraintAnn.params()));
    generator.setFactors(factors);
    generator.setConstraintManager(constraintManager);
    generator.setTargetClass(klazz);
    generator.init(ConfigUtils.processParams(generator.parameterTypes(), generatorAnn.params()));
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

  TupleGeneration getTupleGenerationAnnotation(
      AnnotatedElement annotatedElement) {
    TupleGeneration ret;
    if (annotatedElement.isAnnotationPresent(TupleGeneration.class)) {
      ret = annotatedElement.getAnnotation(TupleGeneration.class);
    } else {
      ret = new TupleGeneration() {
        @Override public Generator generator() {
          return Utils
              .getDefaultValueOfAnnotation(TupleGeneration.class,
                  "generator");
        }

        @Override public Constraint constraint() {
          return Utils
              .getDefaultValueOfAnnotation(TupleGeneration.class,
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
          return TupleGeneration.class;
        }
      };
    }
    return ret;
  }

  TupleGenerator createSchemafulTupleGeneratorInstance(
      Generator generatorAnn) {
    return Utils.createNewInstanceUsingNoParameterConstructor(
        generatorAnn.value());
  }

  ConstraintManager createConstraintManager(Constraint constraintAnn) {
    return Utils.createNewInstanceUsingNoParameterConstructor(
        constraintAnn.value());
  }

}

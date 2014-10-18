package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import org.junit.runners.model.InitializationError;

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
  public TupleGenerator createTupleGeneratorForClass(
      Class<?> klazz) {
    Checks.checknotnull(klazz);
    TupleGeneration tupleGenerationAnn = getTupleGenerationAnnotation(
        klazz);
    return createTupleGenerator(klazz, tupleGenerationAnn);
  }

  public TupleGenerator createTupleGeneratorForField(
      Field field) {
    Checks.checknotnull(field);
    TupleGeneration tupleGenerationAnn = getTupleGenerationAnnotation(
        field);
    return createTupleGenerator(field.getType(),
        tupleGenerationAnn);
  }

  private TupleGenerator createTupleGenerator(Class<?> klazz,
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
    constraintManager.init(ParamType.processParams(constraintManager.parameterTypes(), constraintAnn.params()));
    generator.setFactors(factors);
    generator.setConstraintManager(constraintManager);
    generator.setTargetClass(klazz);
    generator.init(ParamType.processParams(generator.parameterTypes(), generatorAnn.params()));
    return generator;
  }

  protected Factors loadFactors(Class<?> klass) {
    // //
    // Initialize the factor levels for every '@FactorField' annotated field.
    Field[] fields = Utils.getAnnotatedFields(klass, FactorField.class);
    Factors.Builder factorsBuilder = new Factors.Builder();
    List<InvalidTestException> errors = new LinkedList<InvalidTestException>();
    for (Field f : fields) {
      try {
        FactorLoader factorLoader = new FactorLoader(f);
        Factor factor = factorLoader.getFactor();
        factorsBuilder.add(factor);
      } catch (InvalidTestException e) {
        errors.add(e);
      }
    }
    Checks.checktest(errors.isEmpty(), "One or more factors failed to be initialized.: [%s]", Utils.join(", ", new Utils.Formatter<InvalidTestException>() {
      @Override
      public String format(InvalidTestException elem) {
        return elem.getMessage();
      }
    }, errors.toArray(new InvalidTestException[errors.size()])));

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
        @Override
        public Generator generator() {
          return Utils
              .getDefaultValueOfAnnotation(TupleGeneration.class,
                  "generator");
        }

        @Override
        public Constraint constraint() {
          return Utils
              .getDefaultValueOfAnnotation(TupleGeneration.class,
                  "constraint");
        }

        @Override
        public Class<? extends Annotation> annotationType() {
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

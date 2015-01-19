package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMTupleGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TupleGeneratorFactory {
  public static final TupleGeneratorFactory INSTANCE = new TupleGeneratorFactory();

  /**
   * Creates a {@code TupleGenerator} using annotations attached to the class
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
    ////
    // Wire and build objects.
    Constraint constraintAnn = tupleGenerationAnn.constraint();
    ConstraintManager constraintManager =
            new ConstraintManager.Builder()
                    .setConstraintManagerClass(constraintAnn.value())
                    .setParameters(constraintAnn.params())
                    .setFactors(factors).build();
    Generator generatorAnn = tupleGenerationAnn.generator();
    TupleGenerator generator = new TupleGenerator.Builder()
            .setTupleGeneratorClass(generatorAnn.value())
            .setConstraintManager(constraintManager)
            .setParameters(generatorAnn.params())
            .setTargetClass(klazz)
            .setFactors(factors)
            .build();
    List<Method> methods;
    if ((methods = getFSMProviderMethod(klazz, factors)).isEmpty()) {
      Checks.checktest(methods.size() == 1, "One and only one provider method can be used.");
      Method m = methods.get(0);
      generator = new FSMTupleGenerator(generator, createFSM(m), m.getName());
    }
    return generator;
  }

  private FSM<?> createFSM(Method m) {
    FSM<?> ret = null;
    try {
      ret = (FSM<?>) m.invoke(null);
    } catch (IllegalAccessException e) {
      // Since the scope is validated in advance, this path shouldn't be executed.
      Checks.checkcond(false);
    } catch (InvocationTargetException e) {
      Checks.rethrowtesterror(e, "FSM creation was failed. ('%s' method in '%s' class)", m.getName(), m.getDeclaringClass().getCanonicalName());
    }
    return ret;
  }

  private List<Method> getFSMProviderMethod(Class<?> klazz, Factors factors) {
    Set<String> fsmNames = new HashSet<String>();
    for (Factor each : factors) {
      String fsmName;
      if ((fsmName = getFSMName(each)) != null) {
        fsmNames.add(fsmName);
      }
    }
    List<Method> ret = new LinkedList<Method>();
    for (String each : fsmNames) {

    }
    return ret;
  }

  private String getFSMName(Factor factor) {
    return null;
  }

  private void validateFSMProviderMethod(Method m) {
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
    Checks.checktest(errors.isEmpty(),
            "One or more factors failed to be initialized.: [%s]",
            Utils.join(", ", new Utils.Formatter<InvalidTestException>() {
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
}

package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.*;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMTupleGenerator;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

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
    Map<Field, LevelsProvider<Object>> levelsProviders = new LinkedHashMap<Field, LevelsProvider<Object>>();
    Factors factors = loadFactors(klazz, levelsProviders);
    ////
    // Wire and build objects.
    Constraint constraintAnn = tupleGenerationAnn.constraint();
    ConstraintManager constraintManager =
        new ConstraintManager.Builder()
            .setConstraintManagerClass(constraintAnn.value())
            .setParameters(constraintAnn.params())
            .setFactors(factors).build();
    Generator generatorAnn = tupleGenerationAnn.generator();
    TupleGenerator.Builder b = new TupleGenerator.Builder()
        .setTupleGeneratorClass(generatorAnn.value())
        .setConstraintManager(constraintManager)
        .setParameters(generatorAnn.params())
        .setTargetClass(klazz)
        .setFactors(factors);
    TupleGenerator generator;
    List<Method> methods;
    if (!(methods = getFSMProviderMethods(klazz, levelsProviders)).isEmpty()) {
      List<FactorMapper<Object>> factorMappers = new LinkedList<FactorMapper<Object>>();
      for (LevelsProvider<Object> each : levelsProviders.values()) {
        if (each instanceof FactorMapper) {
          factorMappers.add((FactorMapper<Object>) each);
        }
      }
      Map<String, FSM<Object>> fsms = new LinkedHashMap<String, FSM<Object>>();
      for (Method m : methods) {
        fsms.put(m.getName(), createFSM(m));
      }
      //noinspection unchecked
      generator = new FSMTupleGenerator(b, fsms, factorMappers);
      generator.init(new Param[] { });
    } else {
      generator = b.build();
    }
    return generator;
  }

  private FSM<Object> createFSM(Method m) {
    FSM<Object> ret = null;
    try {
      ret = (FSM<Object>) m.invoke(null);
    } catch (IllegalAccessException e) {
      // Since the scope is validated in advance, this path shouldn't be executed.
      Checks.checkcond(false);
    } catch (InvocationTargetException e) {
      Checks.rethrowtesterror(e.getTargetException(), "FSM creation was failed. ('%s' method in '%s' class)", m.getName(), m.getDeclaringClass().getCanonicalName());
    }
    return ret;
  }

  /*
   * Returns a list of methods whose names are FSMs'
   */
  private List<Method> getFSMProviderMethods(Class<?> klazz, Map<Field, LevelsProvider<Object>> levelsProviders) {
    Set<String> fsmNames = new HashSet<String>();
    for (LevelsProvider<?> each : levelsProviders.values()) {
      if (each instanceof FSMLevelsProvider) {
        fsmNames.add(((FSMLevelsProvider) each).getFSMName());
      }
    }
    List<Method> ret = new LinkedList<Method>();
    InvalidTestException invalidTestException = new InvalidTestException(String.format("Error(s) are found in '%s'", klazz.getCanonicalName()));
    for (String each : fsmNames) {
      try {
        try {
          Method m = klazz.getMethod(each);
          validateFSMProviderMethod(m);
          ret.add(m);
        } catch (NoSuchMethodException e) {
          Checks.rethrowtesterror(e, "Method '%s/0' was specified as an FSM provider but not found in '%s'", each, klazz.getCanonicalName());
        }
      } catch (InvalidTestException e) {
        invalidTestException.addChild(e);
      }
    }
    if (invalidTestException.hasChildren())
      throw invalidTestException;
    return ret;
  }

  /**
   * {@code f} Must be annotated with {@code FactorField}. Its {@code levelsProvider} must be an FSMLevelsProvider.
   * Typed with {@code Story} class.
   * @param f
   * @return
   */
  private FSM createFSM(Field f) {
    Checks.checknotnull(f);
    Class<?> clazz = (Class<?>) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
    return FSMUtils.createFSM((Class<? extends FSMSpec<Object>>) clazz);
  }

  private void validateFSMProviderMethod(Method m) {
    int mod = m.getModifiers();
    Checks.checktest(
        Modifier.isStatic(mod) && Modifier.isPublic(mod) && FSM.class.isAssignableFrom(m.getReturnType()),
        "Method '%s/0' in '%s' must be static, be public, and return '%s'",
        m.getName(), m.getDeclaringClass().getCanonicalName()
    );
  }

  protected Factors loadFactors(Class<?> klass, Map<Field, LevelsProvider<Object>> providers) {
    // //
    // Initialize the factor levels for every '@FactorField' annotated field.
    Field[] fields = Utils.getAnnotatedFields(klass, FactorField.class);
    Factors.Builder factorsBuilder = new Factors.Builder();
    InvalidTestException invalidTestException = new InvalidTestException("One or more factors failed to be initialized.");
    for (Field f : fields) {
      try {
        FactorLoader factorLoader = new FactorLoader(f);
        Factor factor = factorLoader.getFactor();
        factorsBuilder.add(factor);
        providers.put(f, (LevelsProvider<Object>) factorLoader.getLevelsProvider());
      } catch (InvalidTestException e) {
        invalidTestException.addChild(e);
      }
    }
    if (invalidTestException.hasChildren()) {
      throw invalidTestException;
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

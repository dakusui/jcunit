package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.*;
import com.github.dakusui.jcunit.exceptions.Errors;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    Map<Field, LevelsProvider> levelsProviders = new LinkedHashMap<Field, LevelsProvider>();
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
    List<Field> fsmFields;
    if (!(fsmFields = extractFSMFactorFields(levelsProviders)).isEmpty()) {
      List<FactorMapper> factorMappers = new LinkedList<FactorMapper>();
      for (LevelsProvider each : levelsProviders.values()) {
        if (each instanceof FactorMapper) {
          factorMappers.add((FactorMapper) each);
        }
      }
      Map<String, FSM> fsms = new LinkedHashMap<String, FSM>();
      Errors.Builder bb = new Errors.Builder();
      for (Field each : fsmFields) {
        validateFSMFactorField(bb, each);
        fsms.put(each.getName(), createFSM(each));
      }
      Errors errors = bb.build();
      Checks.checktest(
          errors.size() == 0,
          "Error(s) are found in test class.'%s' : %s",
          klazz.getCanonicalName(),
          errors);
      //noinspection unchecked
      generator = new FSMTupleGenerator(b, fsms, factorMappers);
      generator.init(new Param[] {});
    } else {
      generator = b.build();
    }
    return generator;
  }

  private static List<Field> extractFSMFactorFields(Map<Field, LevelsProvider> providers) {
    List<Field> ret = new LinkedList<Field>();
    for (Map.Entry<Field, LevelsProvider> each : providers.entrySet()) {
      if (each.getValue() instanceof FSMLevelsProvider) {
        ret.add(each.getKey());
      }
    }
    return ret;
  }

  /**
   * {@code f} Must be annotated with {@code FactorField}. Its {@code levelsProvider} must be an FSMLevelsProvider.
   * Typed with {@code Story} class.
   *
   * @param f A field from which an FSM is created.
   * @return Created FSM object
   */
  private static FSM<?> createFSM(Field f) {
    Checks.checknotnull(f);
    Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
    //noinspection unchecked
    return FSMUtils.createFSM((Class<? extends FSMSpec<Object>>) clazz);
  }

  private static void validateFSMFactorField(Errors.Builder errors, Field f) {
    Checks.checknotnull(f);
    FactorField ann = f.getAnnotation(FactorField.class);
    Checks.checknotnull(ann);
    Checks.checkcond(ann.levelsProvider() != null);
    Class<? extends LevelsProvider> levelsProvider = ann.levelsProvider();
    Checks.checknotnull(levelsProvider);
    Checks.checkcond(
        FSMLevelsProvider.class.isAssignableFrom(levelsProvider),
        "'%s' must be a sub-class of '%s', but isn't",
        levelsProvider.getCanonicalName(),
        FSMLevelsProvider.class.getCanonicalName()
    );
    ////
    // Another design choice is to allow sub types of Story for FSM factor fields.
    // But Dakusui considered it hurts readability of tests and thus allowed
    // to use Story<FSMSpec<SUT>, SUT> directly.
    if (!(Story.class.equals(f.getType()))) {
      errors.add(
          "For FSM factor field (field annotated with '%s' whose levelsProvider is '%s') must be exactly '%s', but was '%s'",
          FactorField.class.getSimpleName(),
          FSMLevelsProvider.class.getSimpleName(),
          Story.class.getCanonicalName(),
          f.getType()
      );
    }
    Type genericType = f.getGenericType();
    if (!(genericType instanceof ParameterizedType)) {
      errors.add(
          "FSM factor field must have a parameterized type as its generic type. But '%s'(%s)'s generic type was '%s'",
          f.getName(),
          f.getDeclaringClass().getCanonicalName(),
          genericType != null
              ? genericType.getClass().getCanonicalName()
              : null
      );
    }

  }

  protected Factors loadFactors(Class<?> klass, Map<Field, LevelsProvider> providers) {
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
        providers.put(f, (LevelsProvider) factorLoader.getLevelsProvider());
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

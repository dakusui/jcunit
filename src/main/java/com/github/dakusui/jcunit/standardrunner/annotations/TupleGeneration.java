package com.github.dakusui.jcunit.standardrunner.annotations;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.Errors;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Target({ ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TupleGeneration {
  Generator generator() default @Generator();

  Constraint constraint() default @Constraint();

  interface TupleGeneratorFactory {
    TupleGeneratorFactory INSTANCE = new TupleGeneratorFactory.Default();

    TupleGenerator createFromClass(Class<?> clazz);

    class Default implements TupleGeneratorFactory {
      /**
       * Creates a {@code TupleGenerator} using annotations attached to the class
       * for which the returned generator is created.
       */
      public TupleGenerator createFromClass(
          Class<?> klazz) {
        Checks.checknotnull(klazz);
        TupleGeneration tupleGenerationAnn = getTupleGenerationAnnotation(
            klazz);
        return createTupleGenerator(klazz, tupleGenerationAnn);
      }

      public TupleGenerator createTupleGenerator(Class<?> klazz,
          TupleGeneration tupleGenerationAnn) {
        Checks.checknotnull(klazz);
        Checks.checknotnull(tupleGenerationAnn);
        Map<Field, Integer> switchCoverages = new LinkedHashMap<Field, Integer>();
        Factors factors = loadFactors(klazz, switchCoverages);
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
        if (!(fsmFields = extractFSMFactorFields(switchCoverages.keySet())).isEmpty()) {
          Map<String, FSM> fsms = new LinkedHashMap<String, FSM>();
          List<Parameters.LocalConstraintManager> localCMs = new LinkedList<Parameters.LocalConstraintManager>();
          Errors.Builder bb = new Errors.Builder();
          for (Field each : fsmFields) {
            ////
            // It's safe to assume fsmLevelsProvider becomes non-null since we are
            // iterating over fsmFields.
            int fsmSwitchCoverage =  switchCoverages.get(each);
            validateFSMFactorField(bb, each);
            String fsmName = each.getName();
            FSM fsm = createFSM(each, fsmSwitchCoverage);
            fsms.put(fsmName, fsm);
            collectLocalConstraintManagers(localCMs, fsmName, fsm);
          }
          Errors errors = bb.build();
          Checks.checktest(
              errors.size() == 0,
              "Error(s) are found in test class.'%s' : %s",
              klazz.getCanonicalName(),
              errors);
          //noinspection unchecked
          generator = new FSMTupleGenerator(b, fsms, localCMs);
          generator.init(Param.EMPTY_ARRAY);
        } else {
          generator = b.build();
        }
        return generator;
      }

      private static void collectLocalConstraintManagers(List<Parameters.LocalConstraintManager> localCMs, String fsmName, FSM fsm) {
        for (int i = 0; i < fsm.historyLength(); i++) {
          for (Action eachAction : (List<Action>) fsm.actions()) {
            Parameters parameters = eachAction.parameters();
            ConstraintManager baseLocalCM = parameters.getConstraintManager();
            if (ConstraintManager.DEFAULT_CONSTRAINT_MANAGER.equals(baseLocalCM))
              continue;
            List<String> localPlainParameterNames = Utils.transform(parameters, new Utils.Form<Factor, String>() {
              @Override
              public String apply(Factor in) {
                return Checks.checknotnull(in).name;
              }
            });
            Parameters.LocalConstraintManager localCM = new Parameters.LocalConstraintManager(baseLocalCM, localPlainParameterNames, fsmName, i);
            localCMs.add(localCM);
          }
        }
      }

      private static List<Field> extractFSMFactorFields(Set<Field> factorFields) {
        List<Field> ret = new LinkedList<Field>();
        for (Field each : factorFields) {
          if (FSMUtils.isStoryField(each)) {
            ret.add(each);
          }
        }
        return ret;
      }

      /**
       * {@code f} Must be annotated with {@code FactorField}. Its {@code levelsProvider} must be an FSMLevelsProvider.
       * Typed with {@code Story} class.
       *
       * @param f              A field from which an FSM is created.
       * @param switchCoverage A switch coverage number, which is equal to number of scenarios in a main sequence -1.
       * @return Created FSM object
       */
      private static FSM<?> createFSM(Field f, int switchCoverage) {
        Checks.checknotnull(f);
        Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[1];
        //noinspection unchecked
        return createFSM(f.getName(), (Class<? extends FSMSpec<Object>>) clazz, switchCoverage + 1);
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

      protected Factors loadFactors(Class<?> klass, Map<Field, Integer> providers) {
        // //
        // Initialize the factor levels for every '@FactorField' annotated field.
        Field[] fields = ReflectionUtils.getAnnotatedFields(klass, FactorField.class);
        Factors.Builder factorsBuilder = new Factors.Builder();
        InvalidTestException invalidTestException = new InvalidTestException("One or more factors failed to be initialized.");
        for (Field f : fields) {
          try {
            Factor factor = FactorField.FactorFactory.INSTANCE.createFromField(f);
            factorsBuilder.add(factor);
            // FIXME. switch coverage should be figured out from fsm provider parameters.
            providers.put(f, 1);
          } catch (InvalidTestException e) {
            invalidTestException.addChild(e);
          }
        }
        if (invalidTestException.hasChildren()) {
          invalidTestException.fillInStackTrace();
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
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(TupleGeneration.class,
                      "generator");
            }

            @Override
            public Constraint constraint() {
              return ReflectionUtils
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

      public static <SUT> FSM<SUT> createFSM(String fsmName, Class<? extends FSMSpec<SUT>> fsmSpecClass, int historyLength) {
        return new FSM.Base<SUT>(fsmName, fsmSpecClass, historyLength);
      }
    }
  }
}

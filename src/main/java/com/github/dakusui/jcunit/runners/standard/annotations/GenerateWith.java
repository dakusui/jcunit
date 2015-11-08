package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.ToplevelCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;

import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateWith {

  Generator generator() default @Generator();

  Checker checker() default @Checker();

  Reporter reporter() default @Reporter();


  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedClass(TestClass testClass) {
      // TODO
      return super.validateAnnotatedClass(testClass);
    }

    @Override
    public List<Exception> validateAnnotatedField(FrameworkField f) {
      List<Exception> errors = new LinkedList<Exception>();
      ////
      // The following logic is for validating so called 'FSMfactor fields' , whose
      // type is Story<FSMSpec<SUT>, SUT>.
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
      // But author considered it hurts readability of tests and thus allowed
      // to use Story<FSMSpec<SUT>, SUT> directly.
      if (!(Story.class.equals(f.getType()))) {
        errors.add(new Exception(String.format(
            "For FSM factor field (field annotated with '%s' whose levelsProvider is '%s') must be exactly '%s', but was '%s'",
            FactorField.class.getSimpleName(),
            FSMLevelsProvider.class.getSimpleName(),
            Story.class.getCanonicalName(),
            f.getType()
        )));
      }
      Type genericType = f.getField().getGenericType();
      if (!(genericType instanceof ParameterizedType)) {
        errors.add(new Exception(String.format(
            "FSM factor field must have a parameterized type as its generic type. But '%s'(%s)'s generic type was '%s'",
            f.getName(),
            f.getDeclaringClass().getCanonicalName(),
            genericType != null
                ? genericType.getClass().getCanonicalName()
                : null
        )));
      }
      return errors;
    }
  }

  interface CoveringArrayEngineFactory {
    CoveringArrayEngineFactory INSTANCE = new CoveringArrayEngineFactory.Default();

    CoveringArrayEngine createFromClass(Class<?> clazz);

    class Default implements CoveringArrayEngineFactory {
      final Value.Resolver resolver;

      public Default() {
        this.resolver = createResolver();
      }

      /**
       * Creates a {@code CAEngine} using annotations attached to the class
       * for which the returned generator is created.
       */
      public CoveringArrayEngine createFromClass(
          Class<?> klazz) {
        Checks.checknotnull(klazz);
        GenerateWith generateWithAnn = getAnnotation(
            klazz);
        return createCoveringArrayEngine(klazz, generateWithAnn);
      }

      private Value.Resolver createResolver() {
        return new Value.Resolver();
      }

      public CoveringArrayEngine createCoveringArrayEngine(
          Class<?> klazz,
          GenerateWith generateWithAnn) {
        Checks.checknotnull(klazz);
        Checks.checknotnull(generateWithAnn);
        RunnerContext runnerContext = new RunnerContext.Base(klazz);
        Map<Field, Integer> switchCoverages = new LinkedHashMap<Field, Integer>();
        Factors factors = loadFactors(klazz, switchCoverages);
        ////
        // Wire and build objects.
        Checker checkerAnn = generateWithAnn.checker();
        Constraint constraint =
            new Constraint.Builder()
                .setConstraintManagerClass(checkerAnn.value())
                .setFactors(factors).build();
        Generator generatorAnn = generateWithAnn.generator();
        Class<? extends CoveringArrayEngine> tupleGeneratorClass = generatorAnn.value();
        CoveringArrayEngine.Builder b = new CoveringArrayEngine.Builder(
            runnerContext,
            factors,
            constraint,
            tupleGeneratorClass)
            .setResolver(resolver)
            .setConfigArgsForEngine(Arrays.asList(generatorAnn.args()));
        Checks.checkcond(factors.size() > 0, "No factors are found. Check if your factor fields are public.");
        CoveringArrayEngine generator;
        List<Field> fsmFields = extractFSMFactorFields(switchCoverages.keySet());
        Map<String, FSM> fsms = Utils.newMap();
        List<Parameters.LocalConstraint> localCMs = Utils.newList();
        for (Field each : fsmFields) {
          ////
          // It's safe to assume fsmLevelsProvider becomes non-null since we are
          // iterating over fsmFields.
          int fsmSwitchCoverage = switchCoverages.get(each);
          String fsmName = each.getName();
          FSM<Object> fsm = createFSM(each, fsmSwitchCoverage);
          fsms.put(fsmName, fsm);
          collectLocalConstraintManagers(localCMs, fsmName, fsm);
        }
        generator = new ToplevelCoveringArrayEngine(runnerContext, b, fsms, localCMs);
        return generator;
      }

      private void collectLocalConstraintManagers(List<Parameters.LocalConstraint> localCMs, String fsmName, FSM<Object> fsm) {
        for (int i = 0; i < fsm.historyLength(); i++)
          for (Action<Object> eachAction : fsm.actions()) {
            Parameters parameters = eachAction.parameters();
            Constraint baseLocalCM = parameters.getConstraint();
            if (Constraint.DEFAULT_CONSTRAINT.equals(baseLocalCM)) {
              continue;
            }
            List<String> localPlainParameterNames = Utils.transform(parameters, new Utils.Form<Factor, String>() {
              @Override
              public String apply(Factor in) {
                return Checks.checknotnull(in).name;
              }
            });
            Parameters.LocalConstraint localCM = new Parameters.LocalConstraint(
                baseLocalCM,
                localPlainParameterNames,
                fsmName,
                i
            );
            localCMs.add(localCM);
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
      private static FSM<Object> createFSM(Field f, int switchCoverage) {
        Checks.checknotnull(f);
        Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[1];
        //noinspection unchecked
        return createFSM(f.getName(), (Class<? extends FSMSpec<Object>>) clazz, switchCoverage + 1);
      }

      protected Factors loadFactors(Class<?> klass, Map<Field, Integer> providers) {
        // //
        // Initialize the factor levels for every '@FactorField' annotated field.
        Field[] fields = ReflectionUtils.getAnnotatedFields(klass, FactorField.class);
        Factors.Builder factorsBuilder = new Factors.Builder();
        for (Field f : fields) {
          Factor factor = FactorField.FactorFactory.INSTANCE.createFromField(f);
          factorsBuilder.add(factor);
          // FIXME. switch coverage should be figured out from fsm provider parameters.
          providers.put(f, 1);
        }
        // //
        // Instantiates the test array generator.
        Factors factors;
        factors = factorsBuilder.build();
        return factors;
      }

      GenerateWith getAnnotation(
          AnnotatedElement annotatedElement) {
        GenerateWith ret;
        if (annotatedElement.isAnnotationPresent(GenerateWith.class)) {
          ret = annotatedElement.getAnnotation(GenerateWith.class);
        } else {
          // Fall back to default in case annotation isn't given at all.
          ret = new GenerateWith() {
            @Override
            public Generator generator() {
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(GenerateWith.class,
                      "generator");
            }

            @Override
            public Checker checker() {
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(GenerateWith.class,
                      "checker");
            }

            @Override
            public Reporter reporter() {
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(GenerateWith.class,
                      "reporter");
            }

            @Override
            public Class<? extends Annotation> annotationType() {
              return GenerateWith.class;
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

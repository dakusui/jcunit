package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.ToplevelCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
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
        GenerateCoveringArrayWith generateWithAnn = getAnnotation(klazz);


        return createCoveringArrayEngine(klazz, generateWithAnn);
      }

      private Value.Resolver createResolver() {
        return new Value.Resolver();
      }

      public CoveringArrayEngine createCoveringArrayEngine(
          Class<?> klazz,
          GenerateCoveringArrayWith generateWithAnn) {
        Checks.checknotnull(klazz);
        Checks.checknotnull(generateWithAnn);
        RunnerContext runnerContext = new RunnerContext.Base(klazz);
        Map<Field, Integer> switchCoverages = new LinkedHashMap<Field, Integer>();
        Factors factors = loadFactors(klazz, switchCoverages);
        ////
        // Wire and build objects.
        Checker checkerAnn = generateWithAnn.checker();
        ConstraintChecker constraintChecker = new Checker.Base(checkerAnn, runnerContext).build();
        Generator generatorAnn = generateWithAnn.engine();
        Class<? extends CoveringArrayEngine> tupleGeneratorClass = generatorAnn.value();
        CoveringArrayEngine.Builder b = new CoveringArrayEngine.Builder(
            runnerContext,
            factors,
            constraintChecker,
            tupleGeneratorClass)
            .setResolver(resolver)
            .setConfigArgsForEngine(Arrays.asList(generatorAnn.configValues()));
        Checks.checkcond(factors.size() > 0, "No factors are found. Check if your factor fields are public.");
        CoveringArrayEngine generator;
        List<Field> fsmFields = extractFSMFactorFields(switchCoverages.keySet());
        Map<String, FSM> fsms = Utils.newMap();
        List<Parameters.LocalConstraintChecker> localCMs = Utils.newList();
        for (Field each : fsmFields) {
          ////
          // It's safe to assume fsmLevelsProvider becomes non-null since we are
          // iterating over fsmFields.
          int fsmSwitchCoverage = switchCoverages.get(each);
          String fsmName = each.getName();
          FSM<Object> fsm = JCUnit.createFSM(each, fsmSwitchCoverage);
          fsms.put(fsmName, fsm);
          collectLocalConstraintManagers(localCMs, fsmName, fsm);
        }
        generator = new ToplevelCoveringArrayEngine(runnerContext, b, fsms, localCMs);
        return generator;
      }

      private void collectLocalConstraintManagers(List<Parameters.LocalConstraintChecker> localCMs, String fsmName, FSM<Object> fsm) {
        for (int i = 0; i < 2/* TODO: Fix this appropriately: fsm.historyLength()*/ ; i++)
          for (Action<Object> eachAction : fsm.actions()) {
            Parameters parameters = eachAction.parameters();
            ConstraintChecker baseLocalCM = parameters.getConstraintChecker();
            if (ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER.equals(baseLocalCM)) {
              continue;
            }
            List<String> localPlainParameterNames = Utils.transform(parameters, new Utils.Form<Factor, String>() {
              @Override
              public String apply(Factor in) {
                return Checks.checknotnull(in).name;
              }
            });
            Parameters.LocalConstraintChecker localCM = new Parameters.LocalConstraintChecker(
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

      GenerateCoveringArrayWith getAnnotation(
          AnnotatedElement annotatedElement) {
        GenerateCoveringArrayWith ret;
        if (annotatedElement.isAnnotationPresent(GenerateCoveringArrayWith.class)) {
          ret = annotatedElement.getAnnotation(GenerateCoveringArrayWith.class);
        } else {
          // Fall back to default in case annotation isn't given at all.
          ret = new GenerateCoveringArrayWith() {
            @Override
            public Generator engine() {
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(GenerateWith.class,
                      "engine");
            }

            @Override
            public Checker checker() {
              return ReflectionUtils
                  .getDefaultValueOfAnnotation(GenerateWith.class,
                      "checker");
            }

            @Override
            public Class<? extends Annotation> annotationType() {
              return GenerateWith.class;
            }
          };
        }
        return ret;
      }

    }
  }
}

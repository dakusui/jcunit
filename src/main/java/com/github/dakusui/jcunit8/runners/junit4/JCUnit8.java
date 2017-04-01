package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Oracle;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class JCUnit8 extends org.junit.runners.Parameterized {
  private final SortedMap<String, TestPredicate> predicates;
  private       TestSuite<Tuple>                 testSuite;
  private final List<Runner>                     runners;

  public JCUnit8(Class<?> klass) throws Throwable {
    super(klass);
    ConfigFactory configFactory = getConfigFactory();
    this.predicates = buildTestConstraintMap(configFactory);
    this.testSuite = buildTestSuite(
        configFactory.create(),
        buildParameterSpace(
            buildParameterMap(configFactory).values().stream()
                .collect(toList()),
            this.predicates.values().stream()
                .filter(each -> each instanceof Constraint)
                .map(Constraint.class::cast)
                .collect(toList())
        ));
    this.runners = createRunners();
  }

  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> clazz) {
    return new TestClass(clazz) {
      public List<FrameworkMethod> getAnnotatedMethods(
          Class<? extends Annotation> annotationClass) {
        if (Parameterized.Parameters.class.equals(annotationClass)) {
          return singletonList(new FrameworkMethod(ReflectionUtils.getMethod(DummyMethodHolderForParameterizedRunner.class, "dummy")));

        }
        return super.getAnnotatedMethods(annotationClass);
      }
    };
  }

  private ConfigFactory getConfigFactory() {
    try {
      //noinspection unchecked
      return this.getTestClass().getAnnotation(ConfigureWith.class).value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw unexpectedByDesign(e);
    }
  }

  private ParameterSpace buildParameterSpace(List<com.github.dakusui.jcunit8.factorspace.Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  private List<Runner> createRunners() {
    AtomicInteger i = new AtomicInteger(0);
    return this.testSuite.stream()
        .map(new Function<TestCase<Tuple>, Runner>() {
          @Override
          public Runner apply(TestCase<Tuple> tupleTestCase) {
            try {
              return new BlockJUnit4ClassRunner(getTestClass().getClass()) {
                int id = i.getAndIncrement();

                @Override
                protected String getName() {
                  return String.format("[%d]", this.id);
                }

                @Override
                protected String testName(final FrameworkMethod method) {
                  return String.format("%s[%d]", method.getName(), this.id);
                }

                @Override
                protected void validateConstructor(List<Throwable> errors) {
                  validateZeroArgConstructor(errors);
                }

                protected void validateTestMethods(List<Throwable> errors) {
                  validatePublicVoidNoArgMethods(Oracle.class, false, errors);
                }

                @Override
                protected Description describeChild(FrameworkMethod method) {
                  String name = testName(method);
                  List<? super Annotation> annotations = asList(method.getAnnotations());
                  ////
                  // Elements in the list are all annotations.
                  //noinspection SuspiciousToArrayCall
                  return Description.createTestDescription(
                      getTestClass().getJavaClass(),
                      name,
                      annotations.toArray(new Annotation[annotations.size()]));
                }


                @Override
                public List<FrameworkMethod> getChildren() {
                  List<FrameworkMethod> ret = new LinkedList<>();
                  for (FrameworkMethod each : computeTestMethods()) {
                    if (shouldInvoke(each, createTest()))
                      ret.add(each);
                  }
                  if (ret.isEmpty())
                    ret.add(getDummyMethodForNoMatchingMethodFound());
                  return ret;
                }

                private boolean shouldInvoke(FrameworkMethod each, Object test) {
                  return true;
                }

                @Override
                public Object createTest() {
                  try {
                    return getTestClass().getOnlyConstructor().newInstance();
                  } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw unexpectedByDesign(e);
                  }
                }


                private FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
                  try {
                    return new FrameworkMethod(getClass().getMethod("noMatchingTestMethodIsFoundForThisTestCase")) {
                      @Override
                      public String getName() {
                        return String.format("%s:%s", super.getName(), TupleUtils.toString(tupleTestCase.get()));
                      }
                    };
                  } catch (NoSuchMethodException e) {
                    throw unexpectedByDesign(e);
                  }
                }
              };
            } catch (InitializationError initializationError) {
              throw unexpectedByDesign(initializationError);
            }
          }

        })
        .collect(toList());
  }

  /**
   * A class referenced by createTestClass method.
   * This is only used to mock JUnit's Parameterized runner.
   */
  public static class DummyMethodHolderForParameterizedRunner {
    @SuppressWarnings("unused") // This method is referenced reflectively.
    @Parameters
    public static Object[][] dummy() {
      return new Object[][] { {} };
    }
  }

  private static SortedMap<String, com.github.dakusui.jcunit8.factorspace.Parameter> buildParameterMap(ConfigFactory configFactory) {
    return new TreeMap<String, com.github.dakusui.jcunit8.factorspace.Parameter>() {{
      new TestClass(configFactory.getClass()).getAnnotatedMethods(ParameterSource.class).forEach(
          frameworkMethod -> put(frameworkMethod.getName(),
              buildParameterFactoryCreatorFrom(frameworkMethod)
                  .apply(configFactory)
                  .create(frameworkMethod.getName())
          ));
    }};
  }

  private static SortedMap<String, TestPredicate> buildTestConstraintMap(ConfigFactory configFactory) {
    return new TreeMap<String, TestPredicate>() {
      {
        new TestClass(configFactory.getClass()).getAnnotatedMethods(Condition.class).stream()
            .filter((FrameworkMethod frameworkMethod) -> frameworkMethod.getAnnotation(Condition.class).constraint())
            .forEach((FrameworkMethod frameworkMethod) -> put(
                frameworkMethod.getName(),
                frameworkMethod.getAnnotation(Condition.class).constraint() ?
                    buildConstraintCreatorFrom(frameworkMethod).apply(configFactory) :
                    buildTestPredicateCreatorFrom(frameworkMethod).apply(configFactory)
            ));
      }
    };
  }

  private static Function<Object, TestPredicate> buildTestPredicateCreatorFrom(FrameworkMethod method) {
    List<String> involvedParameterNames = getParameterAnnotationsFrom(method, From.class).stream()
        .map(From::value)
        .collect(toList());
    return o -> new TestPredicate() {
      @Override
      public boolean test(Tuple testObject) {
        try {
          return (boolean) method.invokeExplosively(o, involvedParameterNames.stream()
              .map(testObject::get)
              .toArray()
          );
        } catch (Throwable e) {
          throw unexpectedByDesign(e);
        }
      }

      @Override
      public List<String> involvedKeys() {
        return involvedParameterNames;
      }
    };
  }

  private static TestSuite<Tuple> buildTestSuite(Config<Tuple> config, ParameterSpace parameterSpace) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace);
  }

  private static Function<Object, Constraint> buildConstraintCreatorFrom(FrameworkMethod method) {
    return o -> Constraint.fromCondition(buildTestPredicateCreatorFrom(method).apply(o));
  }

  private static Function<Object, com.github.dakusui.jcunit8.factorspace.Parameter.Factory> buildParameterFactoryCreatorFrom(FrameworkMethod method) {
    return (Object o) -> {
      try {
        return (com.github.dakusui.jcunit8.factorspace.Parameter.Factory) method.invokeExplosively(o);
      } catch (Throwable throwable) {
        throw unexpectedByDesign(throwable);
      }
    };
  }

  private static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map((List<? extends Annotation> annotations) -> {
          //noinspection unchecked
          return (A) annotations.stream()
              .filter((Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass()))
              .findFirst().orElseThrow(RuntimeException::new);
        }).collect(toList());
  }
}

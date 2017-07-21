package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationsValidator;
import org.junit.validator.PublicClassValidator;
import org.junit.validator.TestClassValidator;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static com.github.dakusui.jcunit8.exceptions.TestDefinitionException.parameterWithoutAnnotation;
import static com.github.dakusui.jcunit8.factorspace.Parameter.Factory;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class JCUnit8 extends org.junit.runners.Parameterized {
  private static Map<Class, Class> PRIMITIVE_TO_WRAPPER = new HashMap<Class, Class>() {{
    put(boolean.class, Boolean.class);
    put(byte.class, Byte.class);
    put(char.class, Character.class);
    put(double.class, Double.class);
    put(float.class, Float.class);
    put(int.class, Integer.class);
    put(long.class, Long.class);
    put(short.class, Short.class);
    put(void.class, Void.class);
  }};

  private final TestSuite    testSuite;
  private final List<Runner> runners;

  public JCUnit8(Class<?> klass) throws Throwable {
    super(klass);
    ConfigFactory configFactory = getConfigFactory();
    TestClass parameterSpaceDefinition = createParameterSpaceDefinitionTestClass();
    this.testSuite = buildTestSuite(
        configFactory.create(),
        buildParameterSpace(
            new ArrayList<>(buildParameterMap(parameterSpaceDefinition).values()),
            NodeUtils.allTestPredicates(createParameterSpaceDefinitionTestClass()).values().stream()
                .filter(each -> each instanceof Constraint)
                .map(Constraint.class::cast)
                .collect(toList())
        ));
    this.runners = createRunners();
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    this.applyValidators(errors);
  }

  private void applyValidators(List<Throwable> errors) {
    if (getTestClass().getJavaClass() != null) {
      for (TestClassValidator each : createValidatorsFor(createParameterSpaceDefinitionTestClass())) {
        errors.addAll(each.validateTestClass(getTestClass()));
      }
    }
  }


  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  private TestClassValidator[] createValidatorsFor(TestClass parameterSpaceDefinitionClass) {
    return new TestClassValidator[] {
        new AnnotationsValidator(),
        new PublicClassValidator(),
        new TestClassValidator() {
          @Override
          public List<Exception> validateTestClass(TestClass testClass) {
            return new LinkedList<Exception>() {{
              validateFromAnnotationsAreReferencingExistingParameterSourceMethods(testClass, this);
            }};
          }

          private void validateFromAnnotationsAreReferencingExistingParameterSourceMethods(TestClass testClass, List<Exception> errors) {
            testClass.getAnnotatedMethods(Test.class)
                .forEach(
                    frameworkMethod -> Stream.of(frameworkMethod.getMethod().getParameterAnnotations())
                        .forEach((Annotation[] annotations) -> Stream.of(annotations)
                            .filter((Annotation annotation) -> annotation instanceof From)
                            .forEach((Annotation annotation) -> {
                              List<FrameworkMethod> methods = parameterSpaceDefinitionClass.getAnnotatedMethods(ParameterSource.class).stream()
                                  .filter(
                                      (FrameworkMethod each) ->
                                          Objects.equals(each.getName(), From.class.cast(annotation).value()))
                                  .collect(toList());
                              if (methods.isEmpty())
                                errors.add(new Exception(
                                    format(
                                        "A method '%s' annotated with '%s' is not defined in '%s'",
                                        From.class.cast(annotation).value(),
                                        ParameterSource.class.getSimpleName(),
                                        parameterSpaceDefinitionClass.getJavaClass().getCanonicalName()
                                    )));
                            })));
          }
        }
    };
  }

  private ConfigFactory getConfigFactory() {
    try {
      return getConfigureWithAnnotation().value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw TestDefinitionException.wrap(e);
    }
  }

  private TestClass createParameterSpaceDefinitionTestClass() {
    Class parameterSpaceClass = getConfigureWithAnnotation().parameterSpace();
    return Objects.equals(parameterSpaceClass, ConfigureWith.DEFAULT_INSTANCE.parameterSpace()) ?
        this.getTestClass() :
        new TestClass(parameterSpaceClass);
  }

  private ConfigureWith getConfigureWithAnnotation() {
    ConfigureWith ret = this.getTestClass().getAnnotation(ConfigureWith.class);
    if (ret == null)
      ret = ConfigureWith.DEFAULT_INSTANCE;
    return ret;
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
        .map((Function<TestCase, Runner>) tupleTestCase -> {
          try {
            return new MyBlockJUnit4ClassRunner(i.getAndIncrement(), tupleTestCase);
          } catch (InitializationError initializationError) {
            throw unexpectedByDesign(initializationError);
          }
        })
        .collect(toList());
  }

  private static SortedMap<String, com.github.dakusui.jcunit8.factorspace.Parameter> buildParameterMap(TestClass parameterSpaceDefinitionTestClass) {
    return new TreeMap<String, com.github.dakusui.jcunit8.factorspace.Parameter>() {
      {
        parameterSpaceDefinitionTestClass.getAnnotatedMethods(ParameterSource.class).forEach(
            frameworkMethod -> put(frameworkMethod.getName(),
                buildParameterFactoryCreatorFrom(frameworkMethod)
                    .apply(Utils.createInstanceOf(parameterSpaceDefinitionTestClass))
                    .create(frameworkMethod.getName())
            ));
      }
    };
  }

  private static TestSuite buildTestSuite(Config config, ParameterSpace parameterSpace) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace);
  }

  private static Function<Object, com.github.dakusui.jcunit8.factorspace.Parameter.Factory> buildParameterFactoryCreatorFrom(FrameworkMethod method) {
    return (Object o) -> {
      try {
        return (Factory) method.invokeExplosively(o);
      } catch (Throwable throwable) {
        throw unexpectedByDesign(throwable);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map((List<? extends Annotation> annotations) ->
            (A) annotations.stream()
                .filter((Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass()))
                .findFirst()
                .orElseThrow(
                    () -> parameterWithoutAnnotation(
                        format(
                            "%s.%s",
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName()
                        )))
        )
        .collect(toList());
  }

  /**
   * This method is only used through reflection to let JUnit know the test case is ignored since
   * no matching test method is defined for it.
   *
   * @see JCUnit8.MyBlockJUnit4ClassRunner#getDummyMethodForNoMatchingMethodFound()
   */
  @Ignore
  @SuppressWarnings({ "unused", "WeakerAccess" })
  public static void noMatchingTestMethodIsFoundForThisTestCase() {
  }


  private class MyBlockJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private final TestCase tupleTestCase;
    int id;

    MyBlockJUnit4ClassRunner(int id, TestCase tupleTestCase) throws InitializationError {
      super(JCUnit8.this.getTestClass().getJavaClass());
      this.tupleTestCase = tupleTestCase;
      this.id = id;
    }

    @Override
    protected String getName() {
      return format("[%d]", this.id);
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      return format("%s[%d]", method.getName(), this.id);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateZeroArgConstructor(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
      return Description.createTestDescription(
          getTestClass().getJavaClass(),
          testName(method),
          method.getAnnotations()
      );
    }

    @Override
    public List<FrameworkMethod> getChildren() {
      try {
        List<FrameworkMethod> ret = new LinkedList<>();
        for (FrameworkMethod each : computeTestMethods()) {
          if (shouldInvoke(each, tupleTestCase.get()))
            ret.add(each);
        }
        if (ret.isEmpty())
          ret.add(getDummyMethodForNoMatchingMethodFound());
        return ret;
      } catch (Throwable t) {
        throw unexpectedByDesign(t);
      }
    }

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
      return new InvokeMethod(method, test) {
        @Override
        public void evaluate() throws Throwable {
          Object[] args = validateArguments(
              method,
              method.getMethod().getParameterTypes(),
              getParameterAnnotationsFrom(method, From.class).stream()
                  .map(From::value)
                  .map(s -> tupleTestCase.get().get(s))
                  .collect(toList())
                  .toArray()
          );
          method.invokeExplosively(test, args);
        }
      };
    }

    @SuppressWarnings("unchecked")
    private Object[] validateArguments(FrameworkMethod method, Class[] parameterClasses, Object[] argumentValues) {
      // we can assume parameterClasses.length == argumentValues.length
      for (int i = 0; i < argumentValues.length; i++) {
        if (parameterClasses[i].isPrimitive()) {
          if (argumentValues[i] == null || !PRIMITIVE_TO_WRAPPER.get(parameterClasses[i]).isAssignableFrom(argumentValues[i].getClass())) {
            throw new IllegalArgumentException(composeErrorMessageForTypeMismatch(argumentValues[i], method, i));
          }
        } else {
          if (argumentValues[i] != null && !parameterClasses[i].isAssignableFrom(argumentValues[i].getClass())) {
            throw new IllegalArgumentException(composeErrorMessageForTypeMismatch(argumentValues[i], method, i));
          }
        }
      }
      return argumentValues;
    }

    String composeErrorMessageForTypeMismatch(Object argumentValue, FrameworkMethod method, int parameterIndex) {
      return String.format("'%s' is not compatible with parameter %s of '%s(%s)'",
          argumentValue,
          parameterIndex,
          method.getName(),
          Arrays.stream(method.getMethod().getParameterTypes())
              .map(Class::getSimpleName)
              .collect(joining(","))
      );
    }

    @Override
    public Object createTest() {
      return Utils.createInstanceOf(getTestClass());
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }

    private boolean shouldInvoke(FrameworkMethod each, Tuple tuple) {
      //noinspection SimplifiableIfStatement
      if (each.getAnnotation(Given.class) == null)
        return true;
      return NodeUtils.buildPredicate(
          each.getAnnotation(Given.class).value(),
          createParameterSpaceDefinitionTestClass()
      ).test(tuple);
    }

    private FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
      try {
        return new FrameworkMethod(JCUnit8.class.getMethod("noMatchingTestMethodIsFoundForThisTestCase")) {
          @Override
          public String getName() {

            return format("%s:%s", super.getName(), Objects.toString(tupleTestCase));
          }
        };
      } catch (NoSuchMethodException e) {
        throw unexpectedByDesign(e);
      }
    }
  }
}
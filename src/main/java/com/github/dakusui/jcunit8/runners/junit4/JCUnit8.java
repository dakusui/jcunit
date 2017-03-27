package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static com.github.dakusui.jcunit8.exceptions.TestDefinitionException.testClassIsInvalid;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

public class JCUnit8 extends Theories {
  private final SortedMap<String, TestPredicate> predicates;
  private       TestSuite<Tuple>                 testSuite;

  public JCUnit8(Class<?> klass) throws InitializationError {
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
  }

  @Override
  public Statement methodBlock(final FrameworkMethod testMethod) {
    final TestClass testClass = getTestClass();
    return new TheoryAnchor(testMethod, testClass) {
      int successes = 0;
      List<AssumptionViolatedException> fInvalidParameters = new ArrayList<>();

      @Override
      public void evaluate() throws Throwable {
        AtomicInteger i = new AtomicInteger(0);
        testSuite.stream()
            .map(TestCase::get)
            .forEach((Tuple tuple) -> {
              try {
                runWithCompleteAssignment(tuple2assignments(testClass, testMethod.getMethod(), i.getAndIncrement(), tuple));
              } catch (Throwable throwable) {
                throw unexpectedByDesign(throwable);
              }
            });
        //if this test method is not annotated with Theory, then no successes is a valid case
        boolean hasTheoryAnnotation = testMethod.getAnnotation(Theory.class) != null;
        if (successes == 0 && hasTheoryAnnotation) {
          Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: "
              + fInvalidParameters);
        }
      }

      protected void runWithCompleteAssignment(final Assignments complete)
          throws Throwable {
        new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
          @Override
          protected void collectInitializationErrors(
              List<Throwable> errors) {
            // do nothing
          }

          @Override
          public Statement methodBlock(FrameworkMethod method) {
            final Statement statement = super.methodBlock(method);
            return new Statement() {
              @Override
              public void evaluate() throws Throwable {
                try {
                  assumeTrue(satisfiesAnyOf(assignment2tuple(complete), givens(method)));
                  statement.evaluate();
                  handleDataPointSuccess();
                } catch (AssumptionViolatedException e) {
                  handleAssumptionViolation(e);
                } catch (Throwable e) {
                  reportParameterizedError(e, complete.getArgumentStrings(nullsOk()));
                }
              }

              private boolean satisfiesAnyOf(Tuple tuple, List<TestPredicate> givens) {
                for (TestPredicate eachPredicate : givens) {
                  if (!eachPredicate.test(tuple))
                    return false;
                }
                return true;
              }

              private List<TestPredicate> givens(FrameworkMethod method) {
                return Stream.of(method.getAnnotation(Given.class).value())
                    .map(this::composeTestPredicate)
                    .collect(toList());
              }

              private TestPredicate composeTestPredicate(String term) {
                List<String> involvedKeys = new LinkedList<>();
                List<Predicate<Tuple>> work = new LinkedList<>();
                Stream.of(term.split("&&"))
                    .forEach((String atom) -> {
                      TestPredicate cur = predicates.get(term);
                      involvedKeys.addAll(cur.involvedKeys());
                      work.add(atom.startsWith("!") ?
                          cur.negate() :
                          cur);
                    });
                return new TestPredicate() {
                  Predicate<Tuple> p = work.stream()
                      .reduce(Predicate::and)
                      .orElseThrow(() -> testClassIsInvalid(getTestClass().getJavaClass()));

                  @Override
                  public boolean test(Tuple tuple) {
                    return p.test(tuple);
                  }

                  @Override
                  public List<String> involvedKeys() {
                    return Utils.unique(involvedKeys);
                  }
                };
              }


              private Tuple assignment2tuple(Assignments assignments) throws PotentialAssignment.CouldNotGenerateValueException {
                AtomicInteger i = new AtomicInteger(0);
                return new Tuple.Builder() {{
                  Stream.of(assignments.getAllArguments()).forEach(o -> put(attributeNameOf(i.getAndIncrement()), o));
                }}.build();
              }

              private String attributeNameOf(int i) {
                return getParameterAnnotationsFrom(method, From.class).get(i).value();
              }
            };
          }

          @Override
          protected Statement methodInvoker(FrameworkMethod method, Object test) {
            return methodCompletesWithParameters(method, complete, test);
          }

          @Override
          public Object createTest() throws Exception {
            Object[] params = complete.getConstructorArguments();

            if (!nullsOk()) {
              Assume.assumeNotNull(params);
            }

            return getTestClass().getOnlyConstructor().newInstance(params);
          }

          private Statement methodCompletesWithParameters(
              final FrameworkMethod method, final Assignments complete, final Object freshInstance) {
            return new Statement() {
              @Override
              public void evaluate() throws Throwable {
                final Object[] values = complete.getMethodArguments();

                if (!nullsOk()) {
                  Assume.assumeNotNull(values);
                }

                method.invokeExplosively(freshInstance, values);
              }
            };
          }

          private boolean nullsOk() {
            Theory annotation = testMethod.getMethod().getAnnotation(
                Theory.class);
            return annotation != null && annotation.nullsAccepted();
          }
        }.methodBlock(testMethod).evaluate();
      }

      @Override
      protected void handleAssumptionViolation(AssumptionViolatedException e) {
        fInvalidParameters.add(e);
      }

      @Override
      protected void handleDataPointSuccess() {
        successes++;
      }
    };
  }

  private static TestSuite<Tuple> buildTestSuite(Config<Tuple> config, ParameterSpace parameterSpace) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace);
  }

  private ConfigFactory getConfigFactory() {
    try {
      //noinspection unchecked
      return this.getTestClass().getAnnotation(ConfigureWith.class).value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw unexpectedByDesign(e);
    }
  }

  private ParameterSpace buildParameterSpace(List<Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  private static SortedMap<String, Parameter> buildParameterMap(ConfigFactory configFactory) {
    return new TreeMap<String, Parameter>() {{
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

  private static Function<Object, Constraint> buildConstraintCreatorFrom(FrameworkMethod method) {
    return o -> Constraint.fromCondition(buildTestPredicateCreatorFrom(method).apply(o));
  }

  private static Function<Object, Parameter.Factory> buildParameterFactoryCreatorFrom(FrameworkMethod method) {
    return (Object o) -> {
      try {
        return (Parameter.Factory) method.invokeExplosively(o);
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

  private static Assignments tuple2assignments(TestClass testClass, Method method, int i, Tuple t) {
    Assignments ret = Assignments.allUnassigned(method, testClass);
    while (!ret.isComplete()) {
      String supplierName = ret.nextUnassigned().getAnnotation(From.class).value();
      ret.assignNext(
          PotentialAssignment.forValue(
              "[" + i + "]" + testClass.getName() + "#" + supplierName,
              t.get(supplierName)
          ));
    }
    return ret;
  }

}

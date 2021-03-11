package com.github.dakusui.jcunitx.engine.junit5;

import com.github.dakusui.jcunitx.annotations.CombinatorialTest;
import com.github.dakusui.jcunitx.engine.junit5.compat.AnnotationConsumerInitializer;
import com.github.dakusui.jcunitx.engine.junit5.compat.Arguments;
import com.github.dakusui.jcunitx.engine.junit5.compat.ArgumentsProvider;
import com.github.dakusui.jcunitx.engine.junit5.compat.ArgumentsSource;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.AnnotationUtils.*;

/**
 * This class is implemented based on `ParameterizedTestExtension`, which is a
 * part of `junit-jupiter-params`.
 */
public class JCUnitExtension implements TestTemplateInvocationContextProvider, BeforeAllCallback {
  private static final String METHOD_CONTEXT_KEY = "context";

  public JCUnitExtension() {
    System.out.println("Instantiated!");
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    System.out.println("beforeAllContext:" + context);
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    if (!context.getTestMethod().isPresent()) {
      return false;
    }

    Method testMethod = context.getTestMethod().get();
    if (!isAnnotated(testMethod, CombinatorialTest.class)) {
      return false;
    }

    JCUnitTestMethodContext methodContext = new JCUnitTestMethodContext(testMethod);
    classLevelStore(context).getOrComputeIfAbsent("BEFORE_ALL_KEY", (key) -> "BeforeAll:" + System.currentTimeMillis());

    Preconditions.condition(methodContext.hasPotentiallyValidSignature(),
        () -> String.format(
            "@CombinatorialTest method [%s] declares formal parameters in an invalid order: "
                + "argument aggregators must be declared after any indexed arguments "
                + "and before any arguments resolved by another ParameterResolver.",
            testMethod.toGenericString()));
    methodLevelStore(context).put(METHOD_CONTEXT_KEY, methodContext);
    return true;
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    Method templateMethod = context.getRequiredTestMethod();
    String displayName = context.getDisplayName();
    JCUnitTestMethodContext methodContext = methodLevelStore(context)//
        .get(METHOD_CONTEXT_KEY, JCUnitTestMethodContext.class);
    JCUnitTestNameFormatter formatter = createNameFormatter(templateMethod, displayName);
    AtomicLong invocationCount = new AtomicLong(0);

    System.out.println("testClass=" + context.getTestClass() + ":context=" + context + ": key" + classLevelStore(context).get("BEFORE_ALL_KEY"));
    return findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
        .stream()
        .map(ArgumentsSource::value)
        .map(this::instantiateArgumentsProvider)
        .map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
        .flatMap(provider -> arguments(provider, context))
        .map(Arguments::get)
        .map(arguments -> consumedArguments(arguments, methodContext))
        .map(arguments -> createInvocationContext(formatter, methodContext, arguments))
        .peek(invocationContext -> invocationCount.incrementAndGet())
        .onClose(() ->
            Preconditions.condition(invocationCount.get() > 0,
                "Configuration error: You must configure at least one set of arguments for this @CombinatorialTest"));

  }

  private ExtensionContext.Store methodLevelStore(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.create(JCUnitExtension.class, context.getRequiredTestMethod()));
  }

  private ExtensionContext.Store classLevelStore(ExtensionContext context) {
    return context.getRoot().getStore(ExtensionContext.Namespace.create(context.getTestClass().orElseThrow(RuntimeException::new)));
  }

  private JCUnitTestNameFormatter createNameFormatter(Method templateMethod, String displayName) {
    CombinatorialTest combinatorial = findAnnotation(templateMethod, CombinatorialTest.class).orElseThrow(RuntimeException::new);
    String pattern = Preconditions.notBlank(combinatorial.name().trim(),
        () -> String.format(
            "Configuration error: @CombinatorialTest on method [%s] must be declared with a non-empty name.",
            templateMethod));
    return new JCUnitTestNameFormatter(pattern, displayName);
  }

  private Object[] consumedArguments(Object[] arguments, JCUnitTestMethodContext methodContext) {
    int parameterCount = methodContext.getParameterCount();
    return methodContext.hasAggregator() ? arguments
        : (arguments.length > parameterCount ? Arrays.copyOf(arguments, parameterCount) : arguments);
  }

  private TestTemplateInvocationContext createInvocationContext(JCUnitTestNameFormatter formatter, JCUnitTestMethodContext methodContext, Object[] arguments) {
    return new JCUnitTestInvocationContext(formatter, methodContext, arguments);
  }

  private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz) {
    try {
      return ReflectionUtils.newInstance(clazz);
    } catch (Exception ex) {
      if (ex instanceof NoSuchMethodException) {
        String message = String.format("Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
                + "Please ensure that a no-argument constructor exists and "
                + "that the class is either a top-level class or a static nested class",
            clazz.getName());
        throw new JUnitException(message, ex);
      }
      throw ex;
    }
  }

  protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
    try {
      return provider.provideArguments(context);
    } catch (Exception e) {
      throw ExceptionUtils.throwAsUncheckedException(e);
    }
  }
}

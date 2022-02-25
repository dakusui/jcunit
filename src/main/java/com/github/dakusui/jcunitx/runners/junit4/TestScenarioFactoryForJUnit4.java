package com.github.dakusui.jcunitx.runners.junit4;

import com.github.dakusui.jcunitx.runners.core.NodeUtils;
import com.github.dakusui.jcunitx.runners.core.TestInputPredicate;
import com.github.dakusui.jcunitx.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunitx.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunitx.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunitx.testsuite.TestInputConsumer;
import com.github.dakusui.jcunitx.testsuite.TestOracle;
import com.github.dakusui.jcunitx.testsuite.TestScenario;
import org.junit.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.SortedMap;

import static com.github.dakusui.jcunitx.runners.junit4.utils.InternalUtils.toTestOracle;
import static java.util.stream.Collectors.toList;

public enum TestScenarioFactoryForJUnit4 {
  ;

  public static TestScenario create(TestClass testClass) {
    SortedMap<String, TestInputPredicate> predicates = NodeUtils.allTestPredicates(testClass);
    return new TestScenario() {
      @Override
      public List<TestInputConsumer> preSuiteProcedures() {
        return toTestInputConsumer(testClass, BeforeClass.class);
      }

      @Override
      public List<TestInputConsumer> preTestInputProcedures() {
        return toTestInputConsumer(testClass, BeforeTestCase.class);
      }

      @Override
      public List<TestInputConsumer> preOracleProcedures() {
        return toTestInputConsumer(testClass, Before.class);
      }

      @Override
      public List<TestOracle> oracles() {
        return testClass.getAnnotatedMethods(Test.class)
            .stream()
            .map((FrameworkMethod method) -> toTestOracle(method, predicates))
            .collect(toList());
      }

      @Override
      public List<TestInputConsumer> postOracleProcedures() {
        return toTestInputConsumer(testClass, After.class);
      }

      @Override
      public List<TestInputConsumer> postTestInputProcedures() {
        return toTestInputConsumer(testClass, AfterTestCase.class);
      }

      @Override
      public List<TestInputConsumer> postSuiteProcedures() {
        return toTestInputConsumer(testClass, AfterClass.class);
      }
    };
  }

  private static List<TestInputConsumer> toTestInputConsumer(TestClass testClass, Class<? extends Annotation> annotationClass) {
    return testClass.getAnnotatedMethods(annotationClass)
        .stream()
        .map(InternalUtils::toTupleConsumer)
        .collect(toList());
  }
}

package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import com.github.dakusui.jcunit8.testsuite.TestScenario;
import com.github.dakusui.jcunit8.testsuite.TupleConsumer;
import org.junit.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.SortedMap;

import static com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils.toTestOracle;
import static java.util.stream.Collectors.toList;

public enum TestScenarioFactoryForJUnit4 {
  ;

  public static TestScenario create(TestClass testClass) {
    SortedMap<String, TestPredicate> predicates = NodeUtils.allTestPredicates(testClass);
    return new TestScenario() {
      @Override
      public List<TupleConsumer> preSuiteProcedures() {
        return toTupleConsumer(testClass, BeforeClass.class);
      }

      @Override
      public List<TupleConsumer> preTestInputProcedures() {
        return toTupleConsumer(testClass, BeforeTestCase.class);
      }

      @Override
      public List<TupleConsumer> preOracleProcedures() {
        return toTupleConsumer(testClass, Before.class);
      }

      @Override
      public List<TestOracle> oracles() {
        return testClass.getAnnotatedMethods(Test.class).stream(
        ).map(
            (FrameworkMethod method) -> toTestOracle(method, predicates)
        ).collect(
            toList()
        );
      }

      @Override
      public List<TupleConsumer> postOracleProcedures() {
        return toTupleConsumer(testClass, After.class);
      }

      @Override
      public List<TupleConsumer> postTestInputProcedures() {
        return toTupleConsumer(testClass, AfterTestCase.class);
      }

      @Override
      public List<TupleConsumer> postSuiteProcedures() {
        return toTupleConsumer(testClass, AfterClass.class);
      }
    };
  }

  private static List<TupleConsumer> toTupleConsumer(TestClass testClass, Class<? extends Annotation> annotationClass) {
    return testClass.getAnnotatedMethods(annotationClass).stream(
    ).map(
        InternalUtils::toTupleConsumer
    ).collect(
        toList()
    );
  }
}

package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.plugins.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

public interface Metrics extends Plugin {
  interface Metric<T> {
    String name();

    T value();

    Class<T> type();

    void processTestCase(Tuple tuple);

    abstract class CoverageMetric<T> implements Metric<Double> {
      private final Set<T> notCovered;
      private final int total;

      public CoverageMetric(Set<T> toCover) {
        Checks.checknotnull(toCover);
        Checks.checkcond(toCover.size() > 0);
        this.notCovered = new HashSet<T>(toCover);
        this.total = this.notCovered.size();
      }

      @Override
      public Double value() {
        return (double) this.getCovered() / (double) this.getTotal();
      }

      @Override
      public Class<Double> type() {
        return Double.class;
      }

      @Override
      public void processTestCase(Tuple tuple) {
        this.notCovered.removeAll(this.getCoveredItemsBy(tuple));
      }

      abstract protected Set<T> getCoveredItemsBy(Tuple tuple);

      protected int getCovered() {
        return getTotal() - this.notCovered.size();
      };

      protected int getTotal() {
        return this.total;
      };
    }
  }

  void processTestSuite(List<Tuple> testSuite);

  List<Metric<?>> metrics();

  abstract class Base<T> implements Metrics{
    private final List<Metric<?>> metrics;
    private boolean processedTestSuite = false;

    public Base() {
      this.metrics = createMetrics();
    }

    @Override
    public void processTestSuite(List<Tuple> testSuite) {
      Checks.checkcond(!this.processedTestSuite, "This object has already processed a test suite. Create a new one.");
      for (Tuple testCase : testSuite) {
        for (Metric<?> eachMetric : this.metrics()) {
          eachMetric.processTestCase(testCase);
        }
      }
      this.processedTestSuite = true;
    }

    @Override
    public List<Metric<?>> metrics() {
      return this.metrics;
    }

    private List<Metric<?>> createMetrics() {
      List<Metric<?>> ret = new LinkedList<Metric<?>>();
      List<Method> invalidMethods = new LinkedList<Method>();
      List<Method> validMethods = new LinkedList<Method>();

      for (Method each : ReflectionUtils.getMethods(this.getClass())) {
        if (each.getAnnotation(MetricItem.class) != null) {
          if (validateMetricMethod(each)) {
            validMethods.add(each);
          } else {
            invalidMethods.add(each);
          }
        }
      }
      if (invalidMethods.isEmpty()) {
        for (Method each : validMethods) {
          ret.add((Metric) ReflectionUtils.invoke(this, each));
        }
      } else {
        throw invalidMetrics(invalidMethods);
      }
      return ret;
    }

    private InvalidPluginException invalidMetrics(List<Method> invalidMethods) {
      String msg = String.format(
          "(%s)Following methods in class %s are not valid. Check they return %s, have no parameter, and have public access:%s",
          "Metrics",
          this.getClass().getCanonicalName(),
          Metric.class.getCanonicalName(),
          Utils.transform(invalidMethods, new Utils.Form<Method, String>() {
            @Override
            public String apply(Method in) {
              return String.format("%s/%d", in.getName(), in.getParameterTypes().length);
            }
          })
      );
      return new InvalidPluginException(msg);
    }

    protected boolean validateMetricMethod(Method method) {
      if (!Metric.class.isAssignableFrom(method.getReturnType()))
        return false;
      if (method.getParameterTypes().length != 0)
        return false;
      return true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MetricItem {
    }

  }
}

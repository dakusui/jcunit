package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.InvalidPluginException;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.Reporter;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An interface of classes that calculate some metrics about a set of {@code T} instances.
 *
 * @param <T> Typically {@code Tuple}
 * @see FSMMetrics
 */
public interface Metrics<T> extends Plugin {
  void process(List<T> testSuite);

  List<Metric<T>> metrics();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface Item {
  }

  interface Metric<T> {
    void processEach(T tuple);

    String name();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Attribute {
      String value();
    }

    class Value {
      public final Class<?> type;
      public final Object   value;
      public final String name;

      public Value(String name, Class<?> type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
      }
    }

    enum Utils {
      ;
      static List<Value> valuesOf(Metric<?> metric) {
        Checks.checknotnull(metric);
        LinkedList<Metric.Value> ret = new LinkedList<Value>();
        for (Method each : ReflectionUtils.getAnnotatedMethods(metric.getClass(), Attribute.class)) {
          String value = each.getAnnotation(Attribute.class).value();
          ret.add(new Value(
              value,
              each.getReturnType(),
              ReflectionUtils.invoke(metric, each)
          ));
        }
        return ret;
      }
    }
  }

  abstract class RatioMetric<T> implements Metric<T> {
    @Attribute("ratio")
    public double getRatio() {
      return (double) this.getNumerator() / (double) this.getDenominator();
    }

    @Attribute("count")
    public abstract int getNumerator();

    @Attribute("total")
    public abstract int getDenominator();
  }

  abstract class CountMetric<T> extends RatioMetric<T> {
    private int count   = 0;
    private int matched = 0;

    @Override
    public void processEach(T each) {
      this.count++;
      if (matches(each)) {
        this.matched++;
      }
    }

    abstract protected boolean matches(T each);

    @Override
    @Attribute("count")
    public int getNumerator() {
      return this.matched;
    }

    @Override
    @Attribute("total")
    public int getDenominator() {
      return this.count;
    }
  }

  /**
   * A metrics model class that calculates figures about how registered {@code C} instances are
   * covered by {@code T} instances given by {@code process} method.
   *
   * @param <C> {@code Switch} for instance.
   * @param <T> Typically {@code Tuple}.
   * @see FSMMetrics#switchCoverage()
   */
  abstract class CoverageMetric<C, T> extends RatioMetric<T> {
    private final Set<C> notCovered;
    private final int    total;

    public CoverageMetric(Set<C> toCover) {
      Checks.checknotnull(toCover);
      Checks.checkcond(toCover.size() > 0);
      this.notCovered = new HashSet<C>(toCover);
      this.total = this.notCovered.size();
    }

    @Override
    @Attribute("count")
    public int getNumerator() {
      return getDenominator() - this.notCovered.size();
    }

    @Override
    @Attribute("total")
    public int getDenominator() {
      return this.total;
    }

    @Override
    public void processEach(T t) {
      this.notCovered.removeAll(this.getCoveredItemsBy(t));
    }

    abstract protected Set<C> getCoveredItemsBy(T t);
  }

  abstract class Base<T> extends Plugin.Base implements Metrics<T> {
    private List<Metric<T>> metrics            = null;
    private boolean         testSuiteProcessed = false;

    public Base() {
    }


    @Override
    public void process(List<T> targetList) {
      Checks.checkcond(
          !this.testSuiteProcessed,
          "This object has already processed a test suite. Create a new one."
      );
      this.metrics = createMetrics();
      for (T eachTarget : targetList) {
        this.processEach(eachTarget);
      }
      this.testSuiteProcessed = true;
    }

    protected void processEach(T testCase) {
      for (Metric<T> eachCoverageMetric : this.metrics()) {
        eachCoverageMetric.processEach(testCase);
      }
    }

    @Override
    public List<Metric<T>> metrics() {
      return this.metrics;
    }

    private List<Metric<T>> createMetrics() {
      List<Metric<T>> ret = new LinkedList<Metric<T>>();
      List<Method> invalidMethods = new LinkedList<Method>();
      List<Method> validMethods = new LinkedList<Method>();

      for (Method each : ReflectionUtils.getMethods(this.getClass())) {
        if (each.getAnnotation(Item.class) != null) {
          if (validateMetricMethod(each)) {
            validMethods.add(each);
          } else {
            invalidMethods.add(each);
          }
        }
      }
      if (invalidMethods.isEmpty()) {
        for (Method each : validMethods) {
          //noinspection unchecked
          ret.add((Metric<T>) Checks.cast(Metric.class, ReflectionUtils.invoke(this, each)));
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
          CoverageMetric.class.getCanonicalName(),
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
      return Metric.class.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 0;
    }
  }

  class Builder {
    private final Class<? extends Metrics<?>> metricsClass;
    private final RunnerContext               runnerContext;
    private final List<Value>                 configValues;

    public Builder(Reporter reporter, RunnerContext runnerContext) {
      //noinspection unchecked
      this((Class<? extends Metrics<?>>) reporter.value(), runnerContext, Utils.asList(reporter.args()));
    }

    private Builder(Class<? extends Metrics<?>> metricsClass, RunnerContext runnerContext, List<Value> configValues) {
      this.metricsClass = metricsClass;
      this.runnerContext = runnerContext;
      this.configValues = configValues;
    }


    public <T> Metrics<T> build() {
      Plugin.Factory<Metrics<?>, Value> pluginFactory
          = Factory.newFactory(this.metricsClass, new Value.Resolver(), this.runnerContext);
      //noinspection unchecked
      return (Metrics<T>) pluginFactory.create(this.configValues);
    }
  }
}

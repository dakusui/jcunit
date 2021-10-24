package com.github.dakusui.jcunit8.models.scenario;

import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit8.models.Parameter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public interface Scenario extends Parameter.Regex<ParameterizedAction> {
  @Override
  ActionSequence composeValue(KeyValuePairs tuple);

  class Impl extends Regex.Impl<ParameterizedAction> implements Scenario {
    private final Supplier<Object> driverObjectSupplier;

    public Impl(String name, String regex, List<List<ParameterizedAction>> knownValues, Supplier<Object> driverObjectSupplier, Function<String, ParameterizedAction> func) {
      super(name, regex, knownValues, func);
      this.driverObjectSupplier = driverObjectSupplier;
    }

    @Override
    public ActionSequence composeValue(KeyValuePairs tuple) {
      return new ActionSequence.Impl(
          composeStringValueFrom(tuple).stream()
              .map(func)
              .collect(toList()));
    }
  }

  class Factory extends Regex.Factory<ParameterizedAction> {
    private Supplier<?> driverObjectSupplier;

    public Factory(String regex) {
      this(regex, s -> (ParameterizedAction) ActionSupport.simple(s, c -> {
      }));
    }

    private Factory(String regex, Function<String, ParameterizedAction> func) {
      super(regex, func);
    }

    public <T> Factory addParameter(String actionName, Simple.Factory<T> parameter) {
      return this;
    }

    public <D> Factory driverObjectSupplier(Supplier<D> driverObjectSupplier) {
      this.driverObjectSupplier = driverObjectSupplier;
      return this;
    }

    @Override
    public Scenario create(String name) {
      return new Scenario.Impl(name, null, null, null, null);
    }
  }
}

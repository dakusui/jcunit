package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends CoveringArrayEngine> value() default IPO2CoveringArrayEngine.class;

  Value[] args() default {};

  class Base implements CoreBuilder<CoveringArrayEngine> {
    private final Class<? extends CoveringArrayEngine> engineClass;
    private final RunnerContext                        runnerContext;
    private final List<Value>                          configValues;

    public Base(Generator engine, RunnerContext runnerContext) {
      this(engine.value(), runnerContext, Utils.asList(engine.args()));
    }

    private Base(Class<? extends CoveringArrayEngine> engineClass, RunnerContext context, List<Value> configValues) {
      this.engineClass = checknotnull(engineClass);
      this.runnerContext = checknotnull(context);
      this.configValues = checknotnull(Collections.unmodifiableList(configValues));
    }

    @Override
    public CoveringArrayEngine build() {
      Plugin.Factory<CoveringArrayEngine, Value> pluginFactory
          = Plugin.Factory.newFactory(engineClass, new Value.Resolver(), this.runnerContext);
      return pluginFactory.create(this.configValues);
    }
  }
}

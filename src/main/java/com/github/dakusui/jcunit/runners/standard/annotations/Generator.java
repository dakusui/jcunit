package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Generator DEFAULT= new Generator() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Generator.class;
    }

    @Override
    public Class<? extends CoveringArrayEngine> value() {
      return IPO2CoveringArrayEngine.class;
    }

    @Override
    public Value[] configValues() {
      return new Value[] { new Value.Builder().add("2").build()};
    }
  };
  Class<? extends CoveringArrayEngine> value() default IPO2CoveringArrayEngine.class;

  Value[] configValues() default {};

  class Base implements CoreBuilder<CoveringArrayEngine> {
    private final Class<? extends CoveringArrayEngine> engineClass;
    private final RunnerContext                        runnerContext;
    private final List<Value>                          configValues;

    public Base(Generator engine, RunnerContext runnerContext) {
      this(engine.value(), runnerContext, Utils.asList(engine.configValues()));
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

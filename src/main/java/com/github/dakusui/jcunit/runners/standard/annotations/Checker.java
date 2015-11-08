package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.NullConstraint;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

@Retention(RetentionPolicy.RUNTIME)
public @interface Checker {
  Class<? extends Constraint> value() default NullConstraint.class;

  Value[] args() default {};

  class Base implements CoreBuilder<Constraint> {
    private final Class<? extends Constraint> constraintClass;
    private final RunnerContext               runnerContext;
    private final List<Value>                 configValues;

    public Base(Checker checker, RunnerContext runnerContext) {
      this(checker.value(), runnerContext, Utils.asList(checker.args()));
    }

    private Base(Class<? extends Constraint> constraintClass, RunnerContext runnerContext, List<Value> configValues) {
      this.constraintClass = checknotnull(constraintClass);
      this.runnerContext = checknotnull(runnerContext);
      this.configValues = checknotnull(configValues);
    }

    @Override
    public Constraint build() {
      Plugin.Factory<Constraint, Value> pluginFactory
          = Plugin.Factory.newFactory(constraintClass, new Value.Resolver(), this.runnerContext);
      return pluginFactory.create(this.configValues);
    }
  }

}

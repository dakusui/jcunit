package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit8.pipeline.Config;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ConfigureWith {
  Class<? extends Config> value();
}

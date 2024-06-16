package com.github.jcunit.pipeline;

import com.github.jcunit.annotations.ConfigureWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ParseRequirementWith {
  Class<? extends ConfigureWith.RequirementParser> value();
}

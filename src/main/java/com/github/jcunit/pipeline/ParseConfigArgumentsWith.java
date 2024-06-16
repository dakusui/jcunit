package com.github.jcunit.pipeline;

import com.github.jcunit.annotations.ConfigurePipelineWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ParseConfigArgumentsWith {
  Class<? extends ConfigurePipelineWith.PipelineConfigArgumentsParser> value();
}

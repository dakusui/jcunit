package com.github.jcunit.pipeline;

import com.github.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface PipelineConfig {
  int strength();

  boolean generateNegativeTests();

  List<Tuple> seeds();
}


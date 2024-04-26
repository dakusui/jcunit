package com.github.jcunit.core.model;

import java.util.List;
import java.util.function.Predicate;

public interface TestSuite {
  String description();
  List<TestCase<?, ?>> testCases(Predicate<TestCase<?, ?>> filter);
}

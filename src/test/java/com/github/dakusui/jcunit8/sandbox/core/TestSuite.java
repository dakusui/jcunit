package com.github.dakusui.jcunit8.sandbox.core;

import java.util.List;
import java.util.function.Predicate;

public interface TestSuite {
  String description();
  List<TestCase<?, ?>> testCases(Predicate<TestCase<?, ?>> filter);
}

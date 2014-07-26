package com.github.dakusui.jcunit.core;

import java.util.Arrays;

class JCUnitTestCaseFilter {
  private final int[] include;
  private final int[] exclude;

  JCUnitTestCaseFilter(int[] include, int[] exclude) {
    if (include != null) {
      for (int i : include) Utils.checkcond(i >= 0, "include mustn't contain negative index. (%d was found)", i);
      this.include = Arrays.copyOf(include, include.length);
      Arrays.sort(this.include);
    } else {
      this.include = null;
    }
    Utils.checknotnull(exclude);
    for (int i : exclude) Utils.checkcond(i >= 0, "exclude mustn't contain negative index. (%d was found)", i);
    this.exclude = Arrays.copyOf(exclude, exclude.length);
    Arrays.sort(this.exclude);
  }

  boolean shouldBeExecuted(int testCaseIndex) {
    if (this.include == null) {
      return Arrays.binarySearch(this.exclude, testCaseIndex) < 0;
    }
    return Arrays.binarySearch(this.include, testCaseIndex) >= 0 && Arrays.binarySearch(this.exclude, testCaseIndex) < 0;
  }

  public static JCUnitTestCaseFilter createTestCaseFilter(TestExecution annotation) {
    int[] included = annotation.include();
    if (included.length == 1) {
      if (included[0] == -1)
        included = null;
    }
    int[] excluded = annotation.exclude();
    return new JCUnitTestCaseFilter(included, excluded);
  }

  public static JCUnitTestCaseFilter createTestCaseFilter() {
    return new JCUnitTestCaseFilter(null, new int[]{});
  }

}

package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Param;

public class Sample {
  public static final int   a = 0;
  public static final int[] b = { 1, 2, 3 };

  @FactorField(levelsFactory = DefaultLevelsFactory.class)
  Object obj;

  @FactorField(levelsFactory = DefaultLevelsFactory.class,
      factoryParams = { @Param({ "Hello", "world" }) })
  int test;

  @FactorField(intLevels = { 1, 2, 100 })
  int test2;
}

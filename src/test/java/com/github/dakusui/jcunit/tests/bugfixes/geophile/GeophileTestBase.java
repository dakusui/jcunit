package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;

public class GeophileTestBase {
  @FactorField(stringLevels = { "INCLUDED", "EXCLUDED" })
  public String  duplicates;
  @FactorField(doubleLevels = { 1, 64, 1024, 1000000 })
  public double  X;
  @FactorField(doubleLevels = { 1, 64, 1024, 1000000 })
  public double  Y;
  @FactorField(intLevels = { 0, 1, 20, 30 })
  public int     X_BITS;
  @FactorField(intLevels = { 0, 1, 20, 27 })
  public int     Y_BITS;
  @FactorField(booleanLevels = { false, true })
  public boolean indexForLeft;
  @FactorField(booleanLevels = { false, true })
  public boolean indexForRight;
  @FactorField(intLevels = { 1000, 10000 })
  public int     numBoxes;
  @FactorField(doubleLevels = { 0, 0.1, 1, 2, 21 })
  public double  boxWidth;
  @FactorField(doubleLevels = { 0, 0.1, 1, 2, 21 })
  public double  boxHeight;
}

package com.github.dakusui.jcunit8.tests.features.generators;

import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.testutils.testsuitequality.GenerationTestBase;
import org.junit.Test;

public class CoveringArrayQualityTest extends GenerationTestBase {

  @Test
  public void generate3_3$t2() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(3, 3), 2, 0);
  }

  @Test
  public void generate3_4$t2() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(3, 4), 2, 0);
  }

  @Test
  public void generate3_5$t2() {
    // [{F-02=2, F-04=1}, {F-03=0, F-04=1}]
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(3, 5), 2, 0);
  }


  @Test
  public void generate2_3$t3() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(2, 3), 3, 0);
  }

  @Test
  public void generate2_4$t3() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(2, 3), 3, 0);
  }

  @Test
  public void generate2_5$t3() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(2, 3), 3, 0);
  }

  @Test
  public void generate2_6$t3() {
    exerciseGeneration(new CompatFactorSpaceSpecForExperiments("F").addFactors(2, 3), 3, 0);
  }
}

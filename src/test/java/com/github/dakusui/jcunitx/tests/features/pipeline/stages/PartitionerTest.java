package com.github.dakusui.jcunitx.tests.features.pipeline.stages;

import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunitx.testutils.FactorSpaceUtils;
import com.github.dakusui.jcunitx.testutils.PipelineTestBase;
import com.github.dakusui.jcunitx.testutils.UTUtils;
import org.junit.Test;

import java.util.List;

public class PartitionerTest extends PipelineTestBase {
  @Test
  public void givenSimpleFactorSpace$whenPartition$thenGivenFactorSpacePassedThrough() {
    List<FactorSpace> factorSpaces = partition(
        buildSimpleFactorSpaceWithoutConstraint()
    );

    FactorSpaceUtils.validateFactorSpace(
        factorSpaces.get(0),
        UTUtils.matcher()
    );
  }

  @Test
  public void givenFactorSpaceWithConstraint$whenPartition$thenGivenFactorSpacePassedThrough() {
    List<FactorSpace> factorSpaces = partition(
        buildSimpleFactorSpaceWithImpossibleConstraint()
    );

    FactorSpaceUtils.validateFactorSpace(
        factorSpaces.get(0),
        UTUtils.matcher()
    );

    System.out.println(
        new Cartesian(factorSpaces.get(0), requirement()).generate().size()
    );
  }
}
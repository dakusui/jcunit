package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.utils.CasaUtils;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.NOP;
import static com.github.dakusui.peerj.utils.CasaUtils.simplePartitioner;

public class CasaExperimentSimple extends CasaExperimentBase {
  @Before
  public void before() {
    System.setErr(NOP);
  }

  @Test
  public void generateCoveringArrayByActs() {
    for (CasaUtils each : CasaUtils.values()) {
      long before = System.currentTimeMillis();
      List<Tuple> result = this.conductActsExperiment(each);
      System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
    }
  }


  @Test
  public void generateCoveringArrayFileByJoining() {
    for (CasaUtils each : CasaUtils.values()) {
      long before = System.currentTimeMillis();
      try {
        List<Tuple> result = this.conductJoinExperiment(each, simplePartitioner());
        System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
      } catch (CasaUtils.NotCombinatorialJoinApplicable | ProcessStreamer.Failure e) {
        System.out.println("name:" + each + ",FAILED");
      }
    }
  }

  @Override
  protected ConstraintHandlingMethod constraintHandlingMethod() {
    return ConstraintHandlingMethod.FORBIDDEN_TUPLES;
  }

  @Override
  protected Algorithm algorithm() {
    return Algorithm.IPOG;
  }

  @Override
  protected int strength() {
    return 2;
  }
}

package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.testbases.CasaExperimentBase;
import com.github.dakusui.peerj.utils.CasaDataSet;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.NOP;
import static com.github.dakusui.peerj.utils.PeerJUtils2.simplePartitioner;

public class CasaExperimentSimple extends CasaExperimentBase {
  @Before
  public void before() {
    System.setErr(NOP);
  }

  @Test
  public void generateCoveringArrayByActs() {
    for (CasaDataSet each : CasaDataSet.values()) {
      long before = System.currentTimeMillis();
      List<Tuple> result = this.conductActsExperimentForCasa(each);
      System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
    }
  }


  @Test
  public void generateCoveringArrayFileByJoining() {
    for (CasaDataSet each : CasaDataSet.values()) {
      long before = System.currentTimeMillis();
      try {
        List<Tuple> result = this.conductJoinExperimentForCasa(each, simplePartitioner());
        System.out.println("name:" + each + ",time:" + (System.currentTimeMillis() - before) + ",size:" + result.size());
      } catch (CasaDataSet.NotCombinatorialJoinApplicable | ProcessStreamer.Failure e) {
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

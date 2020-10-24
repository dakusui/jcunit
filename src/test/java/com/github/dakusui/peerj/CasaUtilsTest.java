package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.utils.CasaUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.peerj.CasaExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.CasaExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class CasaUtilsTest extends CasaExperimentBase {
  @Test
  public void testInsurance() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "prefix",
        -1,
        asList(
            "2",
            "14",
            "2 13 17 31 3 6 6 2 2 2 2 11 2 5").iterator(),
        singletonList("0").iterator());
    System.out.println(casaModel);
  }


  @Test
  public void testBanking2() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "prefix",
        -1,
        asList(
            "2",
            "15",
            "4 2 2 2 2 2 2 2 2 2 2 2 2 2 2").iterator(),
        asList(
            "3",
            "2",
            "- 2 - 20",
            "2",
            "- 20 - 1",
            "2",
            "- 20 - 3").iterator());
    System.out.println(casaModel);
    assertEquals(
        "t=2;factors:[prefix-0:[0, 1, 2, 3], prefix-1:[0, 1], prefix-2:[0, 1], prefix-3:[0, 1], prefix-4:[0, 1], prefix-5:[0, 1], prefix-6:[0, 1], prefix-7:[0, 1], prefix-8:[0, 1], prefix-9:[0, 1], prefix-10:[0, 1], prefix-11:[0, 1], prefix-12:[0, 1], prefix-13:[0, 1], prefix-14:[0, 1]],constraints:[prefix-0 != 2 || prefix-9 != 0, prefix-9 != 0 || prefix-0 != 1, prefix-9 != 0 || prefix-0 != 3]",
        casaModel.toString()
    );
  }


  @Test
  public void readBanking2FromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        -1
    );
    System.out.println(casaModel);
    assertEquals(
        "t=2;factors:[prefix-0:[0, 1, 2, 3], prefix-1:[0, 1], prefix-2:[0, 1], prefix-3:[0, 1], prefix-4:[0, 1], prefix-5:[0, 1], prefix-6:[0, 1], prefix-7:[0, 1], prefix-8:[0, 1], prefix-9:[0, 1], prefix-10:[0, 1], prefix-11:[0, 1], prefix-12:[0, 1], prefix-13:[0, 1], prefix-14:[0, 1]],constraints:[prefix-0 != 2 || prefix-9 != 0, prefix-9 != 0 || prefix-0 != 1, prefix-9 != 0 || prefix-0 != 3]",
        casaModel.toString()
    );
  }

  @Test
  public void generateCoveringArrayForBanking2FromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        strength()
    );
    List<Tuple> result = generateWithActs(
        new File("target/acts/cassa"),
        casaModel.factorSpace,
        casaModel.strength,
        algorithm(),
        constraintHandlingMethod());
    assertThat(
        result,
        allOf(
            asInteger("size").eq(11).$(),
            asInteger(call("get", 0).andThen("size").$()).eq(15).$()));
  }

  @Test
  public void generateCoveringArrayForInsuranceFromFile() {
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Insurance",
        "prefix",
        strength()
    );
    List<Tuple> result = generateWithActs(
        new File("target/acts/cassa"),
        casaModel.factorSpace,
        casaModel.strength,
        algorithm(),
        constraintHandlingMethod());
    assertThat(
        result,
        allOf(
            asInteger("size").eq(527).$(),
            asInteger(call("get", 0).andThen("size").$()).eq(14).$()));
  }

  @Override
  protected ConstraintHandlingMethod constraintHandlingMethod() {
    return FORBIDDEN_TUPLES;
  }

  @Override
  protected Algorithm algorithm() {
    return IPOG;
  }

  @Override
  protected int strength() {
    return 2;
  }

}

package com.github.dakusui.peerj.ut;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.testbases.PeerJExperimentBase;
import com.github.dakusui.peerj.utils.CasaDataSet;
import com.github.dakusui.peerj.utils.CasaUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class CasaUtilsTest extends PeerJExperimentBase {
  @Test
  public void testInsurance() {
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
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
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
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
        "t=2;factors:[prefix-0:[0, 1, 2, 3], prefix-1:[0, 1], prefix-2:[0, 1], prefix-3:[0, 1], prefix-4:[0, 1], prefix-5:[0, 1], prefix-6:[0, 1], prefix-7:[0, 1], prefix-8:[0, 1], prefix-9:[0, 1], prefix-10:[0, 1], prefix-11:[0, 1], prefix-12:[0, 1], prefix-13:[0, 1], prefix-14:[0, 1]],constraints:[prefix-0 != 2 or prefix-9 != 0, prefix-9 != 0 or prefix-0 != 1, prefix-9 != 0 or prefix-0 != 3]",
        casaModel.toString()
    );
  }


  @Test
  public void readBanking2FromFile() {
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        -1
    );
    System.out.println(casaModel);
    assertEquals(
        "t=2;factors:[prefix-0:[0, 1, 2, 3], prefix-1:[0, 1], prefix-2:[0, 1], prefix-3:[0, 1], prefix-4:[0, 1], prefix-5:[0, 1], prefix-6:[0, 1], prefix-7:[0, 1], prefix-8:[0, 1], prefix-9:[0, 1], prefix-10:[0, 1], prefix-11:[0, 1], prefix-12:[0, 1], prefix-13:[0, 1], prefix-14:[0, 1]],constraints:[prefix-0 != 2 or prefix-9 != 0, prefix-9 != 0 or prefix-0 != 1, prefix-9 != 0 or prefix-0 != 3]",
        casaModel.toString()
    );
  }

  @Test
  public void generateCoveringArrayForBanking2FromFileWithActs() {
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        strength()
    );
    List<Tuple> result = generateWithActs(
        new File("target/acts/casa"),
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
  public void generateCoveringArrayForBanking2FromFileWithPict() {
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Banking2",
        "prefix",
        strength()
    );
    List<Tuple> result = generateWithPict(
        new File("target/pict/casa"),
        casaModel.factorSpace,
        casaModel.strength
    );
    assertThat(
        result,
        allOf(
            asInteger("size").eq(13).$(),
            asInteger(call("get", 0).andThen("size").$()).eq(15).$()));
  }

  @Test
  public void generateCoveringArrayForInsuranceFromFile() {
    CasaDataSet.CasaModel casaModel = CasaUtils.readCasaModel(
        "IBM",
        "Insurance",
        "prefix",
        strength()
    );
    List<Tuple> result = generateWithActs(
        new File("target/acts/casa"),
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

  public static class Spec {
    final CasaDataSet              def;
    final int                      strength;
    final Algorithm                algorithm;
    final ConstraintHandlingMethod constraintHandlingMethod;

    public Spec(CasaDataSet def, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
      this.def = def;
      this.strength = strength;
      this.algorithm = algorithm;
      this.constraintHandlingMethod = constraintHandlingMethod;
    }

    @Override
    public String toString() {
      return format("%s:t=%s:algorithm=%s:constraintHandling=%s", def, strength, algorithm, constraintHandlingMethod);
    }

    static Spec create(CasaDataSet def) {
      return new Builder()
          .def(def)
          .strength(2)
          .algorithm(Algorithm.IPOG)
          .constraintHandlingMethod(ConstraintHandlingMethod.FORBIDDEN_TUPLES)
          .build();
    }

    public static class Builder {
      CasaDataSet              def;
      int                      strength;
      Algorithm                algorithm;
      ConstraintHandlingMethod constraintHandlingMethod;

      public Builder def(CasaDataSet def) {
        this.def = def;
        return this;
      }

      public Builder strength(int strength) {
        this.strength = strength;
        return this;
      }

      public Builder algorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        return this;
      }

      public Builder constraintHandlingMethod(ConstraintHandlingMethod constraintHandlingMethod) {
        this.constraintHandlingMethod = constraintHandlingMethod;
        return this;
      }

      public Spec build() {
        return new Spec(def, strength, algorithm, constraintHandlingMethod);
      }
    }
  }
}

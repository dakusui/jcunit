package com.github.dakusui.peerj;

import com.github.dakusui.peerj.utils.CasaUtils;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class CasaUtilsTest {
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
  }
}

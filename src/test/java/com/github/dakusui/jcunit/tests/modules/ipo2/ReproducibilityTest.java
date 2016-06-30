package com.github.dakusui.jcunit.tests.modules.ipo2;

import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.IPO2Optimizer;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReproducibilityTest extends IPO2Test {
  List<Tuple> generateTestCases() {
    Factors factors = buildFactors(
        factorsDef(2, 1),
        factorsDef(5, 2),
        factorsDef(4, 2),
        factorsDef(2, 2),
        factorsDef(2, 1),
        factorsDef(5, 2));
    ConstraintChecker constraintChecker = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors, 2, constraintChecker, optimizer);
    return Utils.transform(
        ipo.getResult(),
        new Utils.Form<Tuple, Tuple>() {
          @Override
          public Tuple apply(Tuple in) {
            return new Tuple.Builder().putAll(in).dictionaryOrder(true).build();
          }
        }
    );
  }

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void makeSureRandomReturnsSameValues() {
    Random random = new Random(4649);
    assertEquals(207680777, random.nextInt());
    assertEquals(-1501974936, random.nextInt());
    assertEquals(-1955325053, random.nextInt());
    assertEquals(1409796054, random.nextInt());
    assertEquals(316337786, random.nextInt());
    assertEquals(-458482530, random.nextInt());
    assertEquals(-290938810, random.nextInt());
    assertEquals(1309062832, random.nextInt());
    assertEquals(-252084440, random.nextInt());
    assertEquals(-1682670251, random.nextInt());
  }

  @Test
  public void makeSureIPO2CreatesSameMatrixOnSameMachine() {
    int n = 10;
    List<List<Tuple>> resultsList = new ArrayList<List<Tuple>>(10);
    for (int i = 0; i < n; i++) {
      List<Tuple> testCases = generateTestCases();
      resultsList.add(testCases);
    }
    boolean allPassed = true;
    for (int i = 0; i < n; i++) {
      UTUtils.stdout().print(String.format("%4d ", resultsList.get(i).size()));
      for (int j = 0; j < i; j++) {
        if (!resultsList.get(i).equals(resultsList.get(0))) {
          UTUtils.stdout().print("NG ");
          allPassed = false;
        } else {
          UTUtils.stdout().print("OK ");
        }
      }
      UTUtils.stdout().println();
    }
    assertTrue(allPassed);
    UTUtils.stdout().println(">>" + resultsList.get(0) + "<<");
  }

  @Test
  public void makeSureIPO2CreatesSameMatrixOnDifferentMachines() {
    String expected = "[{A=A0, B=B0, C=C3, D=D0, E=E0, F=F1, G=G0, H=H1, I=I2, J=J2}, {A=A0, B=B1, C=C2, D=D1, E=E2, F=F0, G=G1, H=H0, I=I1, J=J1}, {A=A0, B=B2, C=C4, D=D3, E=E1, F=F1, G=G1, H=H0, I=I3, J=J4}, {A=A0, B=B3, C=C0, D=D2, E=E3, F=F0, G=G0, H=H0, I=I0, J=J0}, {A=A0, B=B4, C=C1, D=D1, E=E1, F=F0, G=G0, H=H1, I=I4, J=J3}, {A=A1, B=B0, C=C4, D=D1, E=E3, F=F1, G=G1, H=H1, I=I0, J=J3}, {A=A1, B=B1, C=C0, D=D0, E=E1, F=F1, G=G1, H=H1, I=I1, J=J0}, {A=A1, B=B2, C=C3, D=D2, E=E2, F=F0, G=G0, H=H1, I=I3, J=J1}, {A=A1, B=B3, C=C2, D=D3, E=E0, F=F0, G=G0, H=H1, I=I4, J=J4}, {A=A1, B=B4, C=C2, D=D2, E=E0, F=F1, G=G1, H=H0, I=I2, J=J2}, {A=A1, B=B2, C=C1, D=D0, E=E3, F=F1, G=G1, H=H0, I=I4, J=J1}, {A=A0, B=B0, C=C0, D=D3, E=E2, F=F1, G=G0, H=H0, I=I4, J=J3}, {A=A0, B=B0, C=C1, D=D2, E=E1, F=F0, G=G1, H=H1, I=I2, J=J4}, {A=A1, B=B0, C=C2, D=D0, E=E2, F=F0, G=G0, H=H1, I=I3, J=J0}, {A=A1, B=B1, C=C1, D=D3, E=E0, F=F0, G=G0, H=H0, I=I0, J=J2}, {A=A1, B=B1, C=C3, D=D3, E=E3, F=F1, G=G1, H=H0, I=I1, J=J4}, {A=A1, B=B1, C=C4, D=D2, E=E0, F=F0, G=G0, H=H0, I=I1, J=J3}, {A=A1, B=B2, C=C0, D=D1, E=E0, F=F1, G=G0, H=H1, I=I3, J=J2}, {A=A1, B=B2, C=C1, D=D3, E=E2, F=F1, G=G1, H=H0, I=I2, J=J0}, {A=A1, B=B2, C=C2, D=D2, E=E3, F=F1, G=G0, H=H0, I=I0, J=J3}, {A=A1, B=B3, C=C1, D=D1, E=E2, F=F1, G=G1, H=H0, I=I3, J=J3}, {A=A1, B=B3, C=C3, D=D0, E=E1, F=F1, G=G1, H=H0, I=I0, J=J1}, {A=A0, B=B3, C=C4, D=D0, E=E2, F=F0, G=G1, H=H1, I=I2, J=J2}, {A=A1, B=B4, C=C0, D=D3, E=E2, F=F1, G=G1, H=H1, I=I0, J=J4}, {A=A0, B=B4, C=C3, D=D0, E=E3, F=F1, G=G1, H=H0, I=I3, J=J3}, {A=A1, B=B4, C=C4, D=D1, E=E3, F=F0, G=G1, H=H0, I=I2, J=J0}, {A=A0, B=B4, C=C3, D=D1, E=E0, F=F0, G=G1, H=H0, I=I1, J=J0}, {A=A0, B=B4, C=C2, D=D0, E=E1, F=F1, G=G1, H=H1, I=I2, J=J1}, {A=A0, B=B0, C=C0, D=D2, E=E0, F=F1, G=G0, H=H1, I=I1, J=J1}, {A=A0, B=B1, C=C4, D=D1, E=E1, F=F1, G=G0, H=H1, I=I2, J=J2}, {A=A0, B=B1, C=C1, D=D1, E=E1, F=F0, G=G1, H=H0, I=I3, J=J4}, {A=A1, B=B1, C=C4, D=D3, E=E1, F=F0, G=G1, H=H0, I=I4, J=J1}, {A=A0, B=B2, C=C2, D=D2, E=E2, F=F0, G=G1, H=H1, I=I1, J=J2}, {A=A1, B=B3, C=C2, D=D1, E=E3, F=F1, G=G0, H=H1, I=I1, J=J2}, {A=A0, B=B2, C=C0, D=D2, E=E3, F=F0, G=G0, H=H1, I=I2, J=J3}, {A=A1, B=B0, C=C1, D=D1, E=E0, F=F1, G=G1, H=H0, I=I1, J=J2}, {A=A1, B=B0, C=C3, D=D2, E=E0, F=F0, G=G0, H=H0, I=I4, J=J0}, {A=A1, B=B4, C=C4, D=D1, E=E3, F=F0, G=G1, H=H1, I=I4, J=J2}, {A=A1, B=B4, C=C3, D=D2, E=E1, F=F1, G=G0, H=H1, I=I4, J=J1}, {A=A1, B=B2, C=C1, D=D0, E=E0, F=F0, G=G1, H=H1, I=I2, J=J4}]";
    List<Tuple> testCases = generateTestCases();
    UTUtils.stdout().println(testCases.toString());
    assertEquals(expected, testCases.toString());
  }
}

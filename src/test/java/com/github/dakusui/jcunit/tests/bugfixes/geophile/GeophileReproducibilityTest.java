package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.standardrunner.JCUnit;
import com.github.dakusui.jcunit.standardrunner.JCUnitDesc;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JCUnit.class)
//@JCUnit.Execute(include = {0, 3, 7, 10, 11, 20, 25, 29, 32, 35, 36})
public class GeophileReproducibilityTest extends GeophileTestBase {
  @Rule
  public JCUnitDesc desc = new JCUnitDesc();

  /**
   * A map that holds test ids and corresponding expectations.
   */
  static         SortedMap<String, String> result = new TreeMap<String, String>();
  private static int                       num    = 0;

  @Before
  public void before() {
    addResult(result,
        "0(test[0]):((1.000000,1.000000),(0,1),(1.000000,21.000000),INCLUDED,false,true,1000)");
    addResult(result,
        "3(test[3]):((1.000000,64.000000),(30,20),(0.000000,1.000000),EXCLUDED,false,false,1000)");
    addResult(result,
        "7(test[7]):((64.000000,1.000000),(30,27),(21.000000,0.000000),INCLUDED,false,true,1000)");
    addResult(result,
        "10(test[10]):((1024.000000,1.000000),(20,20),(0.100000,21.000000),EXCLUDED,true,false,10000)");
    addResult(result,
        "11(test[11]):((1024.000000,64.000000),(30,1),(0.100000,2.000000),EXCLUDED,true,true,10000)");
    addResult(result,
        "20(test[20]):((1.000000,1.000000),(20,20),(2.000000,0.100000),EXCLUDED,true,false,10000)");
    addResult(result,
        "25(test[25]):((1024.000000,1024.000000),(0,0),(21.000000,0.000000),INCLUDED,true,true,10000)");
    addResult(result,
        "29(test[29]):((1000000.000000,1.000000),(20,0),(0.000000,1.000000),EXCLUDED,true,true,1000)");
    addResult(result,
        "32(test[32]):((1024.000000,1024.000000),(20,27),(0.000000,2.000000),EXCLUDED,true,false,10000)");
    addResult(result,
        "35(test[35]):((1.000000,64.000000),(20,20),(21.000000,21.000000),EXCLUDED,true,true,10000)");
  }

  private void addResult(SortedMap<String, String> result, String s) {
    String k = s.substring(0, s.indexOf(':'));
    String v = s.substring(s.indexOf(':') + 1);
    result.put(k, v);
  }

  private String createKey() {
    return String.format("%d(%s)", this.desc.getId(),
        this.desc.getTestName());
  }

  @Test
  public void test() {
    String v =
        String.format(
            "((%f,%f),(%d,%d),(%f,%f),%s,%s,%s,%d)",
            X,
            Y,
            X_BITS,
            Y_BITS,
            boxWidth,
            boxHeight,
            duplicates,
            indexForLeft,
            indexForRight,
            numBoxes
        );
    String expected = result.get(createKey());
    if (expected != null) {
      assertEquals(expected, v);
      num++;
    } else {
      assertNotNull(v);
    }
  }

  @AfterClass
  public static void afterClass() {
    assertEquals(result.size(), num);
  }
}

package com.github.dakusui.jcunit.tests.bugfixes.reproducibilitywithconstraints;

import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateWith;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
@GenerateWith(
    checker = @Checker(ReproducibilityWithComplicatedConstraintCheckerTest.CM.class)
)
public class ReproducibilityWithComplicatedConstraintCheckerTest {
  public static class CM extends ConstraintChecker.Base {
    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      if (!checkLOandHIareInOrder(tuple))
        return false;
      if (!checkLeftIndexIsSane(tuple))
        return false;
      //noinspection RedundantIfStatement
      if (!checkRightIndexIsSane(tuple))
        return false;
      return true;
    }

    private boolean checkLOandHIareInOrder(Tuple tuple) throws UndefinedSymbol {
      Checks.checksymbols(tuple, "LO", "HI");
      double[] LO = toDoubleArray((String) tuple.get("LO"));
      double[] HI = toDoubleArray((String) tuple.get("HI"));
      return (LO[0] < HI[0] &&
          LO[1] < HI[1]);
    }

    private boolean checkRightIndexIsSane(Tuple tuple) throws UndefinedSymbol {
      Checks.checksymbols(tuple, "useSharedIndexForRight", "useSharedSerializerForRight", "rightIndexType");
      boolean useSharedIndexForRight = (Boolean) tuple.get("useSharedIndexForRight");
      boolean useSharedSerializerForRight = (Boolean) tuple.get("useSharedSerializerForRight");
      String rightIndexType = (String) tuple.get("rightIndexType");
      ////
      // Consider &&! represents '->'
      if (useSharedIndexForRight && !useSharedSerializerForRight)
        return false;
      // noinspection
      //noinspection RedundantIfStatement
      if (!useSharedIndexForRight && rightIndexType.equals("INVALID")) {
        return false;
      }
      return true;
    }

    private boolean checkLeftIndexIsSane(Tuple tuple) throws UndefinedSymbol {
      Checks.checksymbols(tuple, "leftIndexType");
      String leftIndexType = (String) tuple.get("leftIndexType");
      return !leftIndexType.equals("INVALID");
    }


    double[] toDoubleArray(String s) {
      String[] work = s.split(",");
      double[] ret = new double[work.length];
      int i = 0;
      for (String w : work) {
        ret[i] = Double.parseDouble(w);
        i++;
      }
      return ret;
    }
  }

  @Rule
  public TestDescription desc = new TestDescription();

  @FactorField(stringLevels = { "INCLUDE", "EXCLUDE" })
  public String duplicates;

  @FactorField(stringLevels = { "-9999999,-9999999", "-1.0,-1.0", "0.0,0.0", "1.0,-1.0", "-1.0,1.0", "1.0,1.0" })
  public String LO;

  @FactorField(stringLevels = { "0.0,0.0", "1.0,1.0", "2560,768", "1000000,1000000", "9999999,9999999" })
  public String HI;

  @FactorField(intLevels = { 0, 1, 20, 27, 30 })
  public int X_BITS;

  @FactorField(intLevels = { 0, 1, 20, 27, 30 })
  public int Y_BITS;

  @FactorField(stringLevels = { "TreeIndex", "TreeIndexWithSerialization", "INVALID" })
  public String leftIndexType;

  @FactorField
  public boolean useSharedIndexForRight;

  @FactorField
  public boolean useSharedSerializerForRight;

  @FactorField(stringLevels = { "TreeIndex", "TreeIndexWithSerialization", "INVALID" })
  public String rightIndexType;

  @FactorField(stringLevels = { "BothEmpty", "LeftEmpty", "RightEmpty", "CompletelySame" })
  public String Boxes;

  static Map<Integer, String> expectations = new HashMap<Integer, String>();

  @BeforeClass
  public static void beforeClass() {
    expectations.put(0, "0;{\"Boxes\":\"BothEmpty\",\"HI\":\"0.0,0.0\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":0,\"Y_BITS\":30,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"INVALID\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(1, "1;{\"Boxes\":\"BothEmpty\",\"HI\":\"1.0,1.0\",\"LO\":\"0.0,0.0\",\"X_BITS\":20,\"Y_BITS\":20,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(2, "2;{\"Boxes\":\"BothEmpty\",\"HI\":\"2560,768\",\"LO\":\"1.0,-1.0\",\"X_BITS\":1,\"Y_BITS\":0,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(3, "3;{\"Boxes\":\"BothEmpty\",\"HI\":\"1000000,1000000\",\"LO\":\"-1.0,1.0\",\"X_BITS\":30,\"Y_BITS\":27,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(4, "4;{\"Boxes\":\"BothEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"1.0,1.0\",\"X_BITS\":27,\"Y_BITS\":1,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(5, "5;{\"Boxes\":\"LeftEmpty\",\"HI\":\"1.0,1.0\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":27,\"Y_BITS\":0,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(6, "6;{\"Boxes\":\"LeftEmpty\",\"HI\":\"2560,768\",\"LO\":\"-1.0,1.0\",\"X_BITS\":20,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(7, "7;{\"Boxes\":\"LeftEmpty\",\"HI\":\"1000000,1000000\",\"LO\":\"1.0,-1.0\",\"X_BITS\":0,\"Y_BITS\":20,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"INVALID\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(8, "8;{\"Boxes\":\"LeftEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"0.0,0.0\",\"X_BITS\":30,\"Y_BITS\":1,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"INVALID\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(9, "9;{\"Boxes\":\"RightEmpty\",\"HI\":\"0.0,0.0\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":20,\"Y_BITS\":1,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(10, "10;{\"Boxes\":\"RightEmpty\",\"HI\":\"1.0,1.0\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":30,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(11, "11;{\"Boxes\":\"RightEmpty\",\"HI\":\"2560,768\",\"LO\":\"0.0,0.0\",\"X_BITS\":0,\"Y_BITS\":27,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(12, "12;{\"Boxes\":\"RightEmpty\",\"HI\":\"1000000,1000000\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":1,\"Y_BITS\":20,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(13, "13;{\"Boxes\":\"CompletelySame\",\"HI\":\"0.0,0.0\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":30,\"Y_BITS\":20,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(14, "14;{\"Boxes\":\"CompletelySame\",\"HI\":\"2560,768\",\"LO\":\"1.0,1.0\",\"X_BITS\":27,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(15, "15;{\"Boxes\":\"CompletelySame\",\"HI\":\"1000000,1000000\",\"LO\":\"0.0,0.0\",\"X_BITS\":1,\"Y_BITS\":1,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(16, "16;{\"Boxes\":\"BothEmpty\",\"HI\":\"1.0,1.0\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":27,\"Y_BITS\":27,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(17, "17;{\"Boxes\":\"LeftEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"1.0,1.0\",\"X_BITS\":1,\"Y_BITS\":0,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(18, "18;{\"Boxes\":\"RightEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"1.0,-1.0\",\"X_BITS\":0,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(19, "19;{\"Boxes\":\"CompletelySame\",\"HI\":\"9999999,9999999\",\"LO\":\"-1.0,1.0\",\"X_BITS\":0,\"Y_BITS\":1,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(20, "20;{\"Boxes\":\"RightEmpty\",\"HI\":\"2560,768\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":30,\"Y_BITS\":20,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(21, "21;{\"Boxes\":\"LeftEmpty\",\"HI\":\"1000000,1000000\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":27,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(22, "22;{\"Boxes\":\"BothEmpty\",\"HI\":\"1000000,1000000\",\"LO\":\"1.0,1.0\",\"X_BITS\":0,\"Y_BITS\":20,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(23, "23;{\"Boxes\":\"BothEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":20,\"Y_BITS\":27,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(24, "24;{\"Boxes\":\"RightEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"-1.0,-1.0\",\"X_BITS\":27,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(25, "25;{\"Boxes\":\"RightEmpty\",\"HI\":\"1.0,1.0\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":1,\"Y_BITS\":1,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(26, "26;{\"Boxes\":\"CompletelySame\",\"HI\":\"2560,768\",\"LO\":\"1.0,-1.0\",\"X_BITS\":30,\"Y_BITS\":1,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(27, "27;{\"Boxes\":\"BothEmpty\",\"HI\":\"2560,768\",\"LO\":\"1.0,1.0\",\"X_BITS\":30,\"Y_BITS\":27,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(28, "28;{\"Boxes\":\"CompletelySame\",\"HI\":\"2560,768\",\"LO\":\"0.0,0.0\",\"X_BITS\":20,\"Y_BITS\":0,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndexWithSerialization\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(29, "29;{\"Boxes\":\"LeftEmpty\",\"HI\":\"2560,768\",\"LO\":\"0.0,0.0\",\"X_BITS\":30,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(30, "30;{\"Boxes\":\"RightEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"-1.0,1.0\",\"X_BITS\":30,\"Y_BITS\":0,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(31, "31;{\"Boxes\":\"RightEmpty\",\"HI\":\"2560,768\",\"LO\":\"1.0,-1.0\",\"X_BITS\":1,\"Y_BITS\":30,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
    expectations.put(32, "32;{\"Boxes\":\"CompletelySame\",\"HI\":\"9999999,9999999\",\"LO\":\"1.0,-1.0\",\"X_BITS\":30,\"Y_BITS\":0,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"INVALID\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(33, "33;{\"Boxes\":\"CompletelySame\",\"HI\":\"1000000,1000000\",\"LO\":\"0.0,0.0\",\"X_BITS\":27,\"Y_BITS\":30,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(34, "34;{\"Boxes\":\"LeftEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"1.0,-1.0\",\"X_BITS\":27,\"Y_BITS\":0,\"duplicates\":\"INCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(35, "35;{\"Boxes\":\"RightEmpty\",\"HI\":\"9999999,9999999\",\"LO\":\"-1.0,1.0\",\"X_BITS\":1,\"Y_BITS\":20,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndexWithSerialization\",\"useSharedIndexForRight\":\"true\",\"useSharedSerializerForRight\":\"true\"}");
    expectations.put(36, "36;{\"Boxes\":\"CompletelySame\",\"HI\":\"0.0,0.0\",\"LO\":\"-9999999,-9999999\",\"X_BITS\":0,\"Y_BITS\":0,\"duplicates\":\"EXCLUDE\",\"leftIndexType\":\"TreeIndex\",\"rightIndexType\":\"TreeIndex\",\"useSharedIndexForRight\":\"false\",\"useSharedSerializerForRight\":\"false\"}");
  }

  @Test
  public void test() {
    String s = this.desc.getTestCase().getId() + ";" + TupleUtils.toString(this.desc.getTestCase().getTuple());
    assertEquals(expectations.get(this.desc.getTestCase().getId()), s);
    expectations.remove(this.desc.getTestCase().getId());
  }

  @AfterClass
  public static void afterClass() {
    assertTrue(expectations.isEmpty());
  }
}

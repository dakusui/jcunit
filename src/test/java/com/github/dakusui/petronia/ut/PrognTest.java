package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.LisjUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrognTest extends JCUnitBase {
  @Test
  public void progn_01() throws Exception {
    Object value = Basic.eval(this,
        progn(assign($("a"), 100), assign($("b"), 200), assign($("c"), 300)));

    assertEquals(LisjUtils.bigDecimal(100), Basic.eval(this, $("a")));
    assertEquals(LisjUtils.bigDecimal(200), Basic.eval(this, $("b")));
    assertEquals(LisjUtils.bigDecimal(300), Basic.eval(this, $("c")));
    assertEquals(LisjUtils.bigDecimal(300), value);
  }

  @Test
  public void progn_2() throws Exception {
    Object value = Basic.eval(this, progn(assign($("a"), 100)));

    assertEquals(LisjUtils.bigDecimal(100), Basic.eval(this, $("a")));
    assertEquals(LisjUtils.bigDecimal(100), value);
  }

  @Test
  public void progn_3() throws Exception {
    Object value = Basic.eval(this, progn());

    assertEquals(null, value);
  }

  @Test
  public void test() throws Exception {
    Basic.eval(
        this,
        progn(
            assign($("a"), 0),
            loop(lt($("a"), 10), print("hello\n"),
                assign($("a"), add($("a"), 1)))
        )
    );
  }
}

package com.github.dakusui.jcunit.tests.plugins;

import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.PluginUtils;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ValueResolverTest {
  public static class TestPlugin implements Plugin {
    public enum TestEnum {
      WORLD
    }

    public final short    x;
    public final int      y;
    public final long     z;
    public final float    w;
    public final double   v;
    public final boolean  p;
    public final byte     q;
    public final char     r;
    public final String   s;
    public final TestEnum t;

    public TestPlugin(
        @Param short x,
        @Param int y,
        @Param long z,
        @Param float w,
        @Param double v,
        @Param boolean p,
        @Param byte q,
        @Param char r,
        @Param String s,
        @Param TestEnum t
    ) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
      this.v = v;
      this.p = p;
      this.q = q;
      this.r = r;
      this.s = s;
      this.t = t;
    }
  }


  @Test
  public void testArgResolver$normal() {
    Plugin.Factory<TestPlugin, Value> factory = new Plugin.Factory<TestPlugin, Value>(
        TestPlugin.class,
        new Value.Resolver(),
        new RunnerContext.Dummy()
    );
    TestPlugin testPlugin = factory.create(Arrays.asList(
        new Value.ArrayBuilder()
            .add("123") //x
            .add("456")  //y
            .add("789")  //z
            .add("1.23") //w
            .add("4.56") //v
            .add("true") //p
            .add("1") //q
            .add("R") //r
            .add("Hello") //s
            .add("WORLD") //t
        .build()
    ));
    assertEquals(123, testPlugin.x);
    assertEquals(456, testPlugin.y);
    assertEquals(789L, testPlugin.z);
    assertEquals(1.23f, testPlugin.w, 0);
    assertEquals(4.56d, testPlugin.v, 0);
    assertEquals(true, testPlugin.p);
    assertEquals(1, testPlugin.q);
    assertEquals('R', testPlugin.r);
    assertEquals("Hello", testPlugin.s);
    assertEquals(TestPlugin.TestEnum.WORLD, testPlugin.t);
  }

  @Test
  public void testStringResolver$normal() {
    Plugin.Factory<TestPlugin, String> factory = new Plugin.Factory<TestPlugin, String>(
        TestPlugin.class,
        PluginUtils.StringResolver.INSTANCE,
        new RunnerContext.Dummy()
    );
    TestPlugin testPlugin = factory.create(Arrays.asList(
        "123", //x
        "456",  //y
        "789",  //z
        "1.23", //w
        "4.56", //v
        "true", //p
        "1", //q
        "R", //r
        "Hello", //s,
        "WORLD" //t
    ));
    assertEquals(123, testPlugin.x);
    assertEquals(456, testPlugin.y);
    assertEquals(789L, testPlugin.z);
    assertEquals(1.23f, testPlugin.w, 0);
    assertEquals(4.56d, testPlugin.v, 0);
    assertEquals(true, testPlugin.p);
    assertEquals(1, testPlugin.q);
    assertEquals('R', testPlugin.r);
    assertEquals("Hello", testPlugin.s);
    assertEquals(TestPlugin.TestEnum.WORLD, testPlugin.t);
  }
}
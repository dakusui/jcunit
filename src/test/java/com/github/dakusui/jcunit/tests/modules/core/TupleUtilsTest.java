package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.SavedObjectBrokenException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TupleUtilsTest {
  @Test
  public void subtuples_01() {
    Tuple t = new Tuple.Builder().put("A", "a1").build();
    Iterator<Tuple> i = TupleUtils.subtuplesOf(t).iterator();
    assertEquals("{A=a1}", i.next().toString());
    assertEquals("{}", i.next().toString());
    assertEquals(false, i.hasNext());
  }

  @Test
  public void subtuples_02() {
    Tuple t = new Tuple.Builder().put("A", "a1").put("B", "b1").build();
    Iterator<Tuple> i = TupleUtils.subtuplesOf(t).iterator();
    assertEquals("{A=a1, B=b1}", i.next().toString());
    assertEquals("{A=a1}", i.next().toString());
    assertEquals("{B=b1}", i.next().toString());
    assertEquals("{}", i.next().toString());
    assertEquals(false, i.hasNext());
  }

  @Test
  public void toStringTest1() {
    Tuple t = new Tuple.Builder().put("A", "A").put("B", "\\").put("C", "\"").put("D", "\\\"").put("E", "\"\\").build();
    assertEquals(
        "{\"A\":\"A\",\"B\":\"\\\\\",\"C\":\"\\\"\",\"D\":\"\\\\\\\"\",\"E\":\"\\\"\\\\\"}",
        TupleUtils.toString(t)
    );
  }

  @Test
  public void toStringTest2() {
    Tuple t = new Tuple.Builder()
        .put("A\\", new Object[]{1, 2, 3, new int[]{1, 2, 3}})
        .put("B\"", new Tuple.Builder().put("A", "abc").build()).build();
    assertEquals(
        "{\"A\\\\\":[1,2,3,[1,2,3]],\"B\\\"\":{\"A\":\"abc\"}}",
        TupleUtils.toString(t)
    );
  }

  @Test
  public void saveAndLoadTest() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Tuple tuple1 = new Tuple.Builder().put("Hello", "World").build();
    TupleUtils.save(tuple1, baos);
    Tuple tuple2 = TupleUtils.load(new ByteArrayInputStream(baos.toByteArray()));
    assertEquals(tuple1, tuple2);
  }


  @Test(expected = SavedObjectBrokenException.class)
  public void loadBrokenTupleTest() {
    TupleUtils.load(new ByteArrayInputStream(new byte[]{}));
  }

  @Test(expected = SavedObjectBrokenException.class)
  public void loadInvalieTupleTest() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.save("Hello, world", baos);
    TupleUtils.load(new ByteArrayInputStream(baos.toByteArray()));
  }

  @Test
  public void toStringTest() {
    Tuple tuple1 = new Tuple.Builder().put("Hello1", "World1").build();
    Tuple tuple2 = new Tuple.Builder().put("Hello2", "World2").build();
    List<Tuple> tuples = new LinkedList<Tuple>();
    tuples.add(tuple1);
    tuples.add(tuple2);

    assertEquals("[{\"Hello1\":\"World1\"},{\"Hello2\":\"World2\"}]", TupleUtils.toString(tuples));
  }
}

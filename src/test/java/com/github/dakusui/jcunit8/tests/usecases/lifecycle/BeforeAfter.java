package com.github.dakusui.jcunit8.tests.usecases.lifecycle;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class BeforeAfter {
  static final List<String> log = Collections.synchronizedList(new LinkedList<>());

  @ParameterSource
  public Parameter.Simple.Factory<Integer> a() {
    return Parameter.Simple.Factory.of(asList(1, 2, 3));
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> b() {
    return Parameter.Simple.Factory.of(asList(1, 2, 3));
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> c() {
    return Parameter.Simple.Factory.of(asList(1, 2, 3));
  }


  @BeforeClass
  public static void beforeClass() {
    log.add("B");
  }

  @Before
  public void before() {
    log.add("b");
  }

  @Test
  public void test(@From("a") int a, @From("b") int b, @From("c") int c) {
    System.out.printf("test=(%s,%s,%s)%n", a, b, c);
    log.add("t");
  }

  @After
  public void after() {
    log.add("a");
  }

  @AfterClass
  public static void afterClass() {
    log.add("A");
  }
}

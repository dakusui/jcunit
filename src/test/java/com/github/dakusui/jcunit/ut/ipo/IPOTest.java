package com.github.dakusui.jcunit.ut.ipo;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class IPOTest {
  public static class Task {
    int numLevels;
    int numFactors;

    public Task(int numLevels, int numFactors) {
      this.numLevels = numLevels;
      this.numFactors = numFactors;
    }

    public int numLevels() {
      return this.numLevels;
    }

    public int numFactors() {
      return this.numFactors;
    }

  }

  private static Task task(int numLevels, int numFactors) {
    return new Task(numLevels, numFactors);
  }

  private static TestSpace createTestSpace(Task... tasks) {
    List<Object[]> work = new LinkedList<Object[]>();
    for (Task task : tasks) {
      for (int i = 0; i < task.numFactors; i++) {
        Object[] tmp = new Object[task.numLevels];
        for (int j = 0; j < tmp.length; j++) {
          tmp[j] = new Character((char) ('A' + i)).toString() + j;
        }
        work.add(tmp);
      }
    }
    return new TestSpace(work.toArray(new Object[][] {}));
  }

  @Test
  public void test3_3() {
    assertEquals(9, new IPO(createTestSpace(task(3, 3))).ipo().size());
  }

  @Test
  public void test3_13() {
    assertEquals(27, new IPO(createTestSpace(task(3, 13))).ipo().size());
  }

  @Test
  public void test4_15$3_17$2_20() {
    assertEquals(73,
        new IPO(createTestSpace(task(4, 15), task(3, 17), task(2, 20))).ipo()
            .size());
  }

  @Test
  public void test4_1$3_20$2_35() {
    assertEquals(73,
        new IPO(createTestSpace(task(4, 15), task(3, 17), task(2, 20))).ipo()
            .size());
  }

  @Test
  public void test2_100() {
    assertEquals(22, new IPO(createTestSpace(task(2, 100))).ipo().size());
  }

  @Test
  public void test10_20() {
    assertEquals(286, new IPO(createTestSpace(task(10, 20))).ipo().size());
  }

}

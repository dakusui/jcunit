package com.github.dakusui.jcunit8.experiments;

import org.junit.Test;

import java.util.*;

public class Find {
  String[] data = {
      "F-000",
      "F-001",
      "F-002",
      "F-003",
      "F-004",
      "F-005",
      "F-006",
      "F-007",
      "F-008",
      "F-009",
      /*
      "F-010",
      "F-011",
      "F-012",
      "F-013",
      "F-014",
      "F-015",
      "F-016",
      "F-017",
      "F-018",
      "F-019",
      */
  };

  @Test
  public void arrayIndex() {
    String[] array = data;
    for (int i = 0; i < 100; i++)
      doIndex(array);
    long before = System.currentTimeMillis();
    for (int i = 0; i < 1_000_000; i++)
      doIndex(array);
    System.out.printf("arrayIndex:%d", System.currentTimeMillis() - before);

  }

  @Test
  public void hashMap() {
    Map<String, String> map = new HashMap<String, String>() {{
      Arrays.stream(data).forEach(s -> put(s, s));
    }};
    for (int i = 0; i < 100; i++)
      doMapFind(map);
    long before = System.currentTimeMillis();
    for (int i = 0; i < 1_000_000; i++)
      doMapFind(map);
    System.out.printf("hashMap:%d", System.currentTimeMillis() - before);
  }

  @Test
  public void arrayList() {
    List<String> list = new ArrayList<String>() {{
      this.addAll(Arrays.asList(data));
    }};
    for (int i = 0; i < 100; i++)
      doListFind(list);
    long before = System.currentTimeMillis();
    for (int i = 0; i < 1_000_000; i++)
      doListFind(list);
    System.out.printf("arrayList:%d", System.currentTimeMillis() - before);
  }

  @Test
  public void array() {
    String[] array = data;
    for (int i = 0; i < 100; i++)
      doArrayFind(array);
    long before = System.currentTimeMillis();
    for (int i = 0; i < 1_000_000; i++)
      doArrayFind(array);
    System.out.printf("array:%d", System.currentTimeMillis() - before);
  }

  private void doMapFind(Map<String, String> map) {
    for (int i = 0; i < data.length; i++) {
      String s = map.get(data[i]);
    }
  }

  private void doListFind(List<String> list) {
    for (int i = 0; i < data.length; i++) {
      int s = list.indexOf(data[i]);
    }
  }

  private void doArrayFind(String[] array) {
    for (int i = 0; i < data.length; i++) {
      int s = Arrays.binarySearch(array, data[i]);
    }
  }

  private void doIndex(String[] array) {
    for (int i = 0; i < data.length; i++) {
      String s = array[i];
    }
  }
}

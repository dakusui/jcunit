package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

public abstract class TestArrayGeneratorTest {
  @Test
  public void makeSureAllThePossibleValuesOfEachFieldAreCovered() {
    System.out
        .println("*** Making sure all the possible values of each field are coverd ***");
    TestArrayGenerator<String, String> generator = createTestArrayGenerator();

    LinkedHashMap<String, String[]> d = new LinkedHashMap<String, String[]>();
    d.put("a", new String[] { "A1", "A2", "A3" });
    d.put("b", new String[] { "B1", "B2", "B3", "B4" });
    d.put("c", new String[] { "C1", "C2", "C3", "C4", "C5" });

    generator.init(new Value[0], d);

    Set<String> results = new HashSet<String>();
    // //
    // Make sure the same pattern doesn't appear more than once in the
    // generated test cases.
    while (generator.hasNext()) {
      Map<String, String> arr = generator.next();
      System.out.println("*** " + arr);
      if (results.contains(arr.toString()))
        fail(String.format(
            "The combination '%s' appeared more than once in the test array.",
            arr.toString()));
      results.add(arr.toString());
    }

    // //
    // Make sure all the possible values of each field appear in the
    // test patterns at least once.
    ArrayList<String> arr = new ArrayList<String>();
    arr.addAll(Arrays.asList(d.get("a")));
    arr.addAll(Arrays.asList(d.get("b")));
    arr.addAll(Arrays.asList(d.get("c")));
    String s = results.toString();

    for (String cur : arr.toArray(new String[0])) {
      if (!s.contains(cur)) {
        fail(String.format("Value '%s' isn't covered.", cur));
      }
    }
    System.out
        .println("********************************************************************");
  }

  protected abstract TestArrayGenerator<String, String> createTestArrayGenerator();

  @Test(expected = UnsupportedOperationException.class)
  public void makeSureRemoveThrowsIntendedException() {
    SimpleTestArrayGenerator<String, String> generator = new SimpleTestArrayGenerator<String, String>();
    generator.remove();
  }

  @Test
  public void makeSureGetterReturnsTheSameObjectSetByTheInit() {
    LinkedHashMap<String, String[]> domains = new LinkedHashMap<String, String[]>();
    SimpleTestArrayGenerator<String, String> generator = new SimpleTestArrayGenerator<String, String>();
    generator.init(new Value[0], domains);
    for (String key : domains.keySet()) {
      assertTrue(Arrays.equals(domains.get(key), generator.getDomain(key)));
    }
    assertEquals(domains.keySet().size(), generator.getKeys().size());
  }

}

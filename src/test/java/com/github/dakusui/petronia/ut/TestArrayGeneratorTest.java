package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public abstract class TestArrayGeneratorTest {
  @Test
  public void makeSureAllThePossibleValuesOfEachFieldAreCovered() {
    System.out
        .println(
            "*** Making sure all the possible values of each field are coverd ***");
    TestArrayGenerator<String> generator = createTestArrayGenerator();

    LinkedHashMap<String, Object[]> d = new LinkedHashMap<String, Object[]>();
    d.put("a", new Object[] { "A1", "A2", "A3" });
    d.put("b", new Object[] { "B1", "B2", "B3", "B4" });
    d.put("c", new Object[] { "C1", "C2", "C3", "C4", "C5" });

    generator.init(new Value[0], d);

    Set<String> results = new HashSet<String>();
    // //
    // Make sure the same pattern doesn't appear more than once in the
    // generated test cases.
    while (generator.hasNext()) {
      Map<String, Object> arr = generator.next();
      System.out.println("*** " + arr);
      if (results.contains(arr.toString())) {
        fail(String.format(
            "The combination '%s' appeared more than once in the test array.",
            arr.toString()));
      }
      results.add(arr.toString());
    }

    // //
    // Make sure all the possible values of each field appear in the
    // test patterns at least once.
    ArrayList<Object> arr = new ArrayList<Object>();
    arr.addAll(Arrays.asList(d.get("a")));
    arr.addAll(Arrays.asList(d.get("b")));
    arr.addAll(Arrays.asList(d.get("c")));
    String s = results.toString();

    for (Object cur : arr.toArray(new Object[arr.size()])) {
      if (!s.contains(
          (String) cur)) { // We know that this object is actually a string.
        fail(String.format("Value '%s' isn't covered.", cur));
      }
    }
    System.out
        .println(
            "********************************************************************");
  }

  protected abstract TestArrayGenerator<String> createTestArrayGenerator();

  @Test(
      expected = UnsupportedOperationException.class)
  public void makeSureRemoveThrowsIntendedException() {
    SimpleTestArrayGenerator<String> generator = new SimpleTestArrayGenerator<String>();
    generator.remove();
  }

  @Test
  public void makeSureGetterReturnsTheSameObjectSetByTheInit() {
    LinkedHashMap<String, Object[]> domains = new LinkedHashMap<String, Object[]>();
    SimpleTestArrayGenerator<String> generator = new SimpleTestArrayGenerator<String>();
    generator.init(new Value[0], domains);
    for (String key : domains.keySet()) {
      assertTrue(Arrays.equals(domains.get(key), generator.getDomain(key)));
    }
    assertEquals(domains.keySet().size(), generator.getKeys().size());
  }

}

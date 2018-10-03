package com.github.dakusui.jcunit8.tests.usecases.lifecycle;

import com.github.dakusui.crest.utils.printable.Printable;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;

public class BeforeAfterTest {
  @SuppressWarnings("unchecked")
  @Test
  public void testBeforeAfter() {
    synchronized (BeforeAfter.log) {
      BeforeAfter.log.clear();
      assertThat(
          JUnitCore.runClasses(BeforeAfter.class).wasSuccessful(),
          asBoolean("wasSuccessful").matcher()
      );
      assertThat(
          BeforeAfter.log,
          asString(
              Printable.function("join", list -> String.join("", (List<String>) list))
          ).matchesRegex(
              "B(bta)+A"
          ).matcher(
          )
      );
    }
  }
}

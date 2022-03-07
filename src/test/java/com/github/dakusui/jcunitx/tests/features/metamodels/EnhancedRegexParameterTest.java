package com.github.dakusui.jcunitx.tests.features.metamodels;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter;
import com.github.dakusui.jcunitx.utils.AssertionUtils;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter.*;
import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Predicates.allOf;
import static com.github.dakusui.pcond.functions.Predicates.isNotNull;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;

public class EnhancedRegexParameterTest {

  private static void printFactorSpace(FactorSpace factorSpace) {
    Stream.of(
            Stream.of("== Factors"),
            factorSpace.getFactors().stream(),
            Stream.of("== Constraints"),
            factorSpace.getConstraints().stream())
        .flatMap(identity())
        .forEach(System.out::println);
  }

  private static Constraint useIdenticalValuesFor(final String k1, final String k2) {
    return new Constraint() {
      final String key1 = k1, key2 = k2;

      @Override
      public String getName() {
        return String.format("useIdenticalValuesFor(%s,%s)", key1, key2);
      }

      @Override
      public boolean test(AArray in) {
        assert that(in,
            allOf(
                isNotNull(),
                AssertionUtils.containsKey(key1),
                AssertionUtils.containsKey(key2)));
        return in.get(key1).equals(in.get(key2));
      }

      @Override
      public List<String> involvedKeys() {
        return asList(key1, key2);
      }

      @Override
      public String toString() {
        return String.format("constraint(%s)", getName());
      }
    };
  }

  @Test
  public void test() {
    EnhancedRegexParameter regex = EnhancedRegexParameter.Descriptor.of("(openForWrite write{0,2} close){1,2} openForRead readLine{0,2} close")
        .call("openForWrite",
            parameter("filename", immediateValue("data.txt")),
            parameter("mode", immediateValuesFromEnum(FileHandle.Mode.class)))
        .call("write",
            parameter("fileHandle", valueFrom("openForWrite")),
            parameter("data", immediateValues("hello", "こんにちは")))
        .call("openForRead",
            parameter("filename", immediateValue("data.txt")),
            parameter("mode", immediateValuesFromEnum(FileHandle.Mode.class)))
        .call("readLine",
            parameter("fileHandle", valueFrom("openForRead")))
        .constraints(useIdenticalValuesFor("open[0].filename", "open[1].filename"))
        .create("regexExample");
    printFactorSpace(regex.toFactorSpace());
  }

  interface FileHandle {
    enum Mode {
      FOR_READ,
      FOR_WRITE,
      FOR_WRITE_CREATE_WHEN_ABSENT,
      FOR_APPEND
    }
  }

  public interface FileIo {
    FileHandle open(String filename, FileHandle.Mode mode);

    String readLine(FileHandle fileHandle);

    void write(FileHandle fileHandle, String data);

    void close(FileHandle fileHandle);
  }
}

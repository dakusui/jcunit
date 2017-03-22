package com.github.dakusui.jcunit8.pipeline.stage;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 */
public interface Generator {
  List<Tuple> generate();

  interface Factory {
    Generator create(FactorSpace factorSpace, Requirement requirement);
  }

  class SandBox {
    final Optional<String> getNameOfJack() {
      return Optional.of("Jack");
    }

    final Optional<String> getNameOfTyler() {
      return Optional.empty();
    }

    @Test
    public void test() {
      System.out.println(getNameOfJack().orElseThrow(RuntimeException::new));
      System.out.println(getNameOfTyler().orElseThrow(RuntimeException::new));
      getNameOfJack().flatMap(new Function<String, Optional<String>>() {
        @Override
        public Optional<String> apply(String s) {
          return null;
        }
      }).get();
      getNameOfJack().ifPresent(System.out::println);
      getNameOfTyler().ifPresent(System.out::println);
      System.out.println(getNameOfTyler().orElse("RuleNumberOne"));
      System.out.println("Bye");
    }
  }

}

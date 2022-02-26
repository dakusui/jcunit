package com.github.dakusui.jcunitx.factorspace;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.jcunitx.runners.core.TestInputPredicate;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.jcunitx.utils.Utils.project;
import static java.util.Arrays.asList;

public interface Constraint extends TestInputPredicate {
  static Constraint create(String name, Predicate<AArray> predicate, List<String> args) {
    return new Constraint() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(AArray row) {
        return predicate.test(project(args, row));
      }

      @Override
      public List<String> involvedKeys() {
        return args;
      }

      @Override
      public String toString() {
        return String.format("%s:%s", Utils.className(predicate.getClass()), args);
      }
    };
  }

  static Constraint create(String name, Predicate<AArray> predicate, String... args) {
    return create(name, predicate, asList(args));
  }
}

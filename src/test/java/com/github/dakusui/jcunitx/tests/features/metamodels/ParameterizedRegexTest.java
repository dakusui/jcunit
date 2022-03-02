package com.github.dakusui.jcunitx.tests.features.metamodels;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.metamodel.parameters.ParameterizedRegex;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.Test;

import static com.github.dakusui.jcunitx.runners.helpers.ParameterUtils.simple;

public class ParameterizedRegexTest {
  @Test
  public void test() {
    ParameterizedRegex regex = ParameterizedRegex.Factory.of("open write{0,2} read close")
        .parameters("open", param("filename", "output.txt"))
        .create("regexExample");
    regex.toFactorSpace().getFactors()
        .stream()
        .forEach(System.out::println);
  }

  private static <T> Parameter<T> param(String parameterName, T... args) {
    return simple(args).create(parameterName);
  }


  public interface Driver {
    void open(String filename);

    int read();

    void close();
  }

  public interface BankAccountExample {
    interface BankAccount {
    }

    BankAccount open();

    void deposit(@From("open") BankAccount account, @From("depositAmount") int amount);

    void withdraw(@From("open") BankAccount account, @From("withdrawAmount") int amount);

    void transfer(@From("open") BankAccount from, @From("openAnother") BankAccount to, @From("transferAmount") int amount);

    void close(@From("open") BankAccount bankAccount);
  }
}

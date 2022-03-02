package com.github.dakusui.jcunitx.tests.features.metamodels;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.metamodel.parameters.CallSequenceRegexParameter;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.Test;

import static com.github.dakusui.jcunitx.runners.helpers.ParameterUtils.simple;

public class ParameterizedRegexTest {
  private static <T> Parameter<T> param(String parameterName, T... args) {
    return simple(args).create(parameterName);
  }

  @Test
  public void test() {
    CallSequenceRegexParameter regex = CallSequenceRegexParameter.Descriptor.of("open write{0,2} read close")
        .parameters("open", param("filename", "output.txt"))
        .create("regexExample");
    regex.toFactorSpace().getFactors()
        .stream()
        .forEach(System.out::println);
  }


  public interface Driver {
    void open(String filename);

    int read();

    void close();
  }

  public interface BankAccountExample {
    BankAccount open();

    void deposit(@From("open") BankAccount account, @From("depositAmount") int amount);

    void withdraw(@From("open") BankAccount account, @From("withdrawAmount") int amount);

    void transfer(@From("open") BankAccount from, @From("openAnother") BankAccount to, @From("transferAmount") int amount);

    void close(@From("open") BankAccount bankAccount);

    interface BankAccount {
    }
  }
}

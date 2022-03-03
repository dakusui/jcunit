package com.github.dakusui.jcunitx.tests.features.metamodels;

import com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.Test;

import static com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter.immediateValue;
import static com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter.parameter;
import static com.github.dakusui.jcunitx.tests.features.metamodels.EnhancedRegexTest.FileHandle.Mode.FOR_APPEND;

public class EnhancedRegexTest {

  @Test
  public void test() {
    EnhancedRegexParameter regex = EnhancedRegexParameter.Descriptor.of("(open write{0,2} close){1,2} open readLine")
        .call("open",
            parameter("filename", immediateValue("output.txt")),
            parameter("mode", immediateValue(FOR_APPEND)))
        .call("write", parameter("data", immediateValue("hello"), immediateValue("こんにちは")))
        .create("regexExample");
    System.out.println("== Factors");
    regex.toFactorSpace().getFactors()
        .forEach(System.out::println);
    System.out.println("== Constraints");
    regex.toFactorSpace().getConstraints()
        .forEach(System.out::println);
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

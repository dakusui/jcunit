package com.github.dakusui.jcunit8.examples.bankaccount;

import com.github.jcunit.annotations.*;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample.JournalEntry.Type.*;
import static com.github.jcunit.annotations.From.ALL;
import static com.github.jcunit.annotations.JCUnitParameter.Type.REGEX;
import static java.util.Arrays.asList;

@SuppressWarnings("NewClassNamingConvention")
@ExtendWith(JCUnitTestEngine.class)
@ConfigureWith(parameterSpace = BankAccountExample.ParameterSpace.class)
public class BankAccountExample {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter(type = REGEX, args = {"open", " ", "deposit", "(deposit|withdraw|transfer){0,2}", "getBalance"})
    public static List<ValueResolver<Action>> scenario() {
      return asList(
          ValueResolver.<Action>fromClassMethodNamed("open", ParameterSpace.class).name("open"),
          ValueResolver.<Action>fromClassMethodNamed("deposit", ParameterSpace.class).name("deposit"),
          ValueResolver.<Action>fromClassMethodNamed("withdraw", ParameterSpace.class).name("withdraw"),
          ValueResolver.<Action>fromClassMethodNamed("transfer", ParameterSpace.class).name("transfer"),
          ValueResolver.<Action>fromClassMethodNamed("getBalance", ParameterSpace.class).name("getBalance")
      );
    }

    @Named
    @JCUnitParameterValue
    public static Action open() {
      return new Action("open") {
        @Override
        public void accept(Context context) {
          BankAccount bankAccount = null;

          try {
            bankAccount = BankAccount.open();

          } finally {
            context.assignTo("account", bankAccount);
          }
        }
      };
    }

    @Named
    @JCUnitParameterValue
    public static Action deposit(@From("depositAmount") int depositAmount) {
      return new Action("deposit[" + depositAmount + "]") {
        @Override
        public void accept(Context context) {
          BankAccount account = context.valueFor("account");
          boolean succeeded = false;

          try {
            account.deposit(depositAmount);

            succeeded = true;
          } finally {
            context.journal()
                   .add(new JournalEntry(JournalEntry.Type.DEPOSIT, depositAmount, succeeded));
          }
        }
      };
    }

    @Named
    @JCUnitParameterValue
    public static Action withdraw(@From("withdrawAmount") int withdrawAmount) {
      return new Action("withdraw[" + withdrawAmount + "]") {
        @Override
        public void accept(Context context) {
          BankAccount account = context.valueFor("account");
          boolean succeeded = false;

          try {
            account.withdraw(withdrawAmount);

            succeeded = true;
          } finally {
            context.journal()
                   .add(new JournalEntry(WITHDRAW, withdrawAmount, succeeded));
          }
        }
      };
    }

    @Named
    @JCUnitParameterValue
    public static Action transfer(@From("transferAmount") int transferAmount) {
      return new Action("transfer[" + transferAmount + "]") {
        @Override
        public void accept(Context context) {
          BankAccount destination = BankAccount.open();
          BankAccount account = context.valueFor("account");
          boolean succeeded = false;

          try {
            account.transferTo(destination, transferAmount);

            succeeded = true;
          } finally {
            context.journal()
                   .add(new JournalEntry(TRANSFER, transferAmount, succeeded));
          }
        }
      };
    }

    @Named
    @JCUnitParameterValue
    public static Action getBalance() {
      return new Action("getBalance") {
        @Override
        public void accept(Context context) {
          BankAccount account = context.valueFor("account");
          boolean succeeded = false;
          int balance = 0;
          try {
            balance = account.getBalance();
            succeeded = true;
          } finally {
            context.journal()
                   .add(new JournalEntry(GET_BALANCE, balance, succeeded));
          }
        }
      };
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<Integer>> depositAmount() {
      return asList(ValueResolver.of(1),
                    ValueResolver.of(1_000),
                    ValueResolver.of(2_000),
                    ValueResolver.of(1_000_000));
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<Integer>> withdrawAmount() {
      return asList(ValueResolver.of(1),
                    ValueResolver.of(1_000),
                    ValueResolver.of(2_000),
                    ValueResolver.of(1_000_000));
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<Integer>> transferAmount() {
      return asList(ValueResolver.of(1),
                    ValueResolver.of(500),
                    ValueResolver.of(1000),
                    ValueResolver.of(1_000_000));
    }
  }

  public static abstract class Action implements Consumer<Context> {
    private final String name;

    Action(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  static class Context {
    final Map<String, Object> data = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T valueFor(String key) {
      return (T) data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T valueFor(String key, Function<String, T> function) {
      return (T) data.computeIfAbsent(key, function);
    }

    public <T> void assignTo(String key, T value) {
      this.data.put(key, value);
    }

    public List<JournalEntry> journal() {
      return this.<List<JournalEntry>>valueFor("journal", k -> new ArrayList<>());
    }
  }

  static class JournalEntry {
    enum Type {
      WITHDRAW,
      DEPOSIT,
      TRANSFER,
      GET_BALANCE
    }

    final Type type;
    final int amount;
    final boolean succeeded;

    JournalEntry(Type type, int amount, boolean succeeded) {
      this.type = type;
      this.amount = amount;
      this.succeeded = succeeded;
    }

    @Override
    public String toString() {
      return this.type + ":" + this.amount;
    }
  }

  @JCUnitTest
  public void examineJournalAndBalance(@From(value = "scenario", index = ALL) List<Action> scenario) {
    System.out.println("scenario:" + scenario);
    Context context = new Context();
    for (Action action : scenario) {
      action.accept(context);
    }
    System.out.println("journal:" + context.valueFor("journal"));
  }
}

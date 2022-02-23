package com.github.dakusui.jcunit8.examples.bankaccount2;

import com.github.dakusui.jcunit8.metamodel.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;

public interface BankAccountDriver extends RegexScenario {
  @ParameterSource
  Parameter.Factory<String> accountName();

  @ParameterSource
  Parameter.Factory<String> recipientAccountName();

  @ParameterSource
  Parameter.Factory<Integer> amount();

  @Handle
  BankAccount openAccount(@From("accountName") String name);

  @Handle
  BankAccount openRecipientAccount(@From("recipientAccountName") String name);

  @Handle
  void deposit(@From("openAccount") BankAccount bankAccount, @From("amount") int amount);

  @Handle
  void withdraw(@From("openAccount") BankAccount bankAccount, @From("amount") int amount);

  @Handle
  void transfer(@From("openAccount") BankAccount from, @From("openRecipientAccount") BankAccount to, int amount);

  @Handle
  void closeAccount(@From("openAccount") BankAccount bankAccount);
}

package com.github.dakusui.jcunitx.examples.bankaccount2;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;

public interface BankAccountDriver extends RegexScenario {
  @ParameterSource
  Parameter.Descriptor<String> accountName();

  @ParameterSource
  Parameter.Descriptor<String> recipientAccountName();

  @ParameterSource
  Parameter.Descriptor<Integer> amount();

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

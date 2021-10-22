package com.github.dakusui.jcunit8.examples.bankaccount;

public class BankAccount {
  public static BankAccount open() {
    return new BankAccount();
  }

  private int balance;

  public void deposit(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    balance += amount;
  }

  public void withdraw(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    if (amount > balance)
      throw new InsufficientBalance();
    balance -= amount;
  }

  public void transferTo(BankAccount another, int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    withdraw(amount);
    another.deposit(amount);
  }

  public int getBalance() {
    return this.balance;
  }

  static class InsufficientBalance extends RuntimeException {
  }
}

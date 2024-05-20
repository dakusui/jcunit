package com.github.dakusui.jcunit8.examples.bankaccount;

class BankAccount {
  static BankAccount open() {
    return new BankAccount();
  }

  private int balance;

  void deposit(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    balance += amount;
  }

  void withdraw(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    if (amount > balance)
      throw new InsufficientBalance();
    balance -= amount;
  }

  void transferTo(BankAccount another, int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException();
    }
    withdraw(amount);
    another.deposit(amount);
  }

  int getBalance() {
    return this.balance;
  }

  static class InsufficientBalance extends RuntimeException {
  }
}

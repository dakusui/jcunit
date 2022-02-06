package com.github.dakusui.jcunit8.examples.bankaccount2;

class BankAccount2 {
  static BankAccount2 open() {
    return new BankAccount2();
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

  void transferTo(BankAccount2 another, int amount) {
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

package com.github.dakusui.jcunit8.sandbox.core;

import java.util.Optional;

public interface Factor<T> {
  int numLevels();

  Optional<T> level(int i);

  interface EnumBased<T> extends Factor<T> {
    T value();
    default boolean isVoid() {
      return false;
    }

    @Override
    default int numLevels() {
      return ExampleStringFactor.class.getEnumConstants().length;
    }

    @Override
    default Optional<T> level(int i) {
      @SuppressWarnings("unchecked") EnumBased<T> enumConstant = this.getClass().getEnumConstants()[i];
      return !enumConstant.isVoid() ? Optional.of(enumConstant.value()) : Optional.empty();
    }

    interface Simple<T extends Enum<T> & Simple<T>> extends EnumBased<T> {
      @SuppressWarnings("unchecked")
      default T value() {
        return (T) this;
      }
    }
  }

  enum ExampleStringFactor implements EnumBased<String> {
    LEVEL_1 {
      @Override
      public String value() {
        return "Hello";
      }
    },
    LEVEL_2 {
      @Override
      public String value() {
        return "World";
      }
    };
  }
  enum ExampleSimpleFactor implements EnumBased.Simple<ExampleSimpleFactor> {
    LEVEL_1,
    LEVEL_2,

  }
}

package com.github.jcunit.core.model;

import java.util.Optional;

public interface Factor<T> {
  static <T> Factor<T> create(Object[] array) {
    return new Factor.Impl<>(array);
  }
  
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
  
  class Impl<T> implements Factor<T> {
    public Impl(Object[] array) {
    }
    
    @Override
    public int numLevels() {
      return 0;
    }
    
    @Override
    public Optional<T> level(int i) {
      return Optional.empty();
    }
  }
}

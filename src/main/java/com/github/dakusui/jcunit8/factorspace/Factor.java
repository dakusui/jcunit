package com.github.dakusui.jcunit8.factorspace;

import java.util.List;

public interface Factor<T> {
  String getName();

  List<T> getLevels();
  interface Internal extends Factor<Object> {
  }
}

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface Parameters extends Iterable<Factor> {
  Object     VOID  = new Object();
  Parameters EMPTY = new Parameters() {
    @Override
    public Object[][] values() {
      return new Object[0][];
    }

    @Override
    public int size() {
      return values().length;
    }

    @Override
    public Factor get(int i) {
      throw new NoSuchElementException();
    }

    @Override
    public Iterator<Factor> iterator() {
      return Collections.<Factor>emptyList().iterator();
    }
  };

  Object[][] values();

  int size();

  Factor get(int i);
}

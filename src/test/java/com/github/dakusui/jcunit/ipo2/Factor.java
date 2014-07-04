package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.enumerator.tuple.AttrValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Created by hiroshi on 7/3/14.
*/
public class Factor {
  public final String       name;
  public final List<Object> levels;

  public Factor(String name, List<Object> levels) {
    this.name = name;
    this.levels = Collections.unmodifiableList(levels);
  }

  public List<AttrValue<String, Object>> asAttrValues() {
    List<AttrValue<String, Object>> ret = new ArrayList<AttrValue<String, Object>>(
        levels.size());
    for (Object l : this.levels) {
      ret.add(new AttrValue<String, Object>(name, l));
    }
    return ret;
  }
}

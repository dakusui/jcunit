package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FSMContext {
  private final Map<String, Story> stories;
  private final Map<String, Boolean> performed = new HashMap<String, Boolean>();

  private FSMContext(Map<String, Story> stories) {
    ////
    // To guarantee the order returned by this.stories.keySet() consistent, wrap
    // original stories with LinkedHashMap.
    Map<String, Story> tmpStories = new LinkedHashMap<String, Story>();
    tmpStories.putAll(stories);
    this.stories = Collections.unmodifiableMap(tmpStories);
    for (String each : this.storyNames()) {
      this.performed.put(each, false);
    }
  }

  public String[] storyNames() {
    return stories.keySet().toArray(new String[this.stories.size()]);
  }

  public Story<Object> lookupStory(String name) {
    Checks.checknotnull(name);
    return this.stories.get(name);
  }

  public boolean hasStory(String name) {
    Checks.checknotnull(name);
    return this.stories.containsKey(name);
  }

  public boolean isAlreadyPerformed(String name) {
    Checks.checknotnull(name);
    Checks.checkcond(this.stories.containsKey(name));
    return this.performed.containsKey(name);
  }

  public void perform(String name, Object sut, Story.Observer observer) throws Throwable {
    Checks.checknotnull(name);
    Checks.checkcond(this.stories.containsKey(name));
    Checks.checkcond(this.performed.containsKey(name));
    Checks.checkcond(this.performed.get(name));
    try {
      this.lookupStory(name).perform(sut, observer);
    } finally {
      this.performed.put(name, true);
    }
  }

  public static class Builder {
    private Map<String, Story> stories = new HashMap<String, Story>();

    public Builder add(String name, Story story) {
      Checks.checknotnull(name);
      Checks.checknotnull(story);
      this.stories.put(name, story);
      return this;
    }

    public FSMContext build() {
      return new FSMContext(stories);
    }
  }
}

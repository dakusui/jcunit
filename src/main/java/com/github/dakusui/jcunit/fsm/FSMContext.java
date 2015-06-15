package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FSMContext {
  private final Map<String, Story> stories;

  public FSMContext(Map<String, Story> stories) {
    ////
    // To guarantee the order returned by this.stories.keySet() consistent, wrap
    // original stories with LinkedHashMap.
    Map<String, Story> tmpStories = new LinkedHashMap<String, Story>();
    tmpStories.putAll(stories);
    this.stories = Collections.unmodifiableMap(tmpStories);
  }

  public String[] storyNames() {
    return stories.keySet().toArray(new String[this.stories.size()]);
  }

  public void performStory(String storyName) {
    Checks.checknotnull(storyName);
    Checks.checkcond(this.stories.containsKey(storyName));
    Story story = this.stories.get(storyName);
    story.perform(this);
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

package com.github.dakusui.jcunitx.engine.junit5.compat;

import java.lang.annotation.*;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ArgumentsSources.class)
public @interface ArgumentsSource {
  Class<? extends ArgumentsProvider> value();
}

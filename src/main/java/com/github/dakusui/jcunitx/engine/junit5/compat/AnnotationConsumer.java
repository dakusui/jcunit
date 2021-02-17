package com.github.dakusui.jcunitx.engine.junit5.compat;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

public interface AnnotationConsumer<A extends Annotation> extends Consumer<A> {
}

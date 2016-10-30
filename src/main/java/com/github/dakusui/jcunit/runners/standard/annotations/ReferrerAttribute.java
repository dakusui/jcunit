package com.github.dakusui.jcunit.runners.standard.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation of JCUnit.
 * If this annotation is attached, an attribute specified by the {@code value} of
 * this annotation will be picked up and parsed by JCUnit.
 * <p/>
 * JCUnit assumes the attribute is an array of {@code String}, each of whose element
 * looks like following
 * <pre>
 *   - "methodName"
 *   - "!methodName"
 *   - "methodName1&&methodName2"
 *   - "methodName1&&methodName2"
 *   - "methodName1&&methodName2&&...methodName3"
 * </pre>
 * <p/>
 * Since JCUnit uses this attribute to build a {@code CompositeFrameworkMethod}, which
 * is used for determining if a test case should be executed or not, the methods
 * referenced by the attribute must satisfy following properties.
 * <p/>
 * <ol>
 * <li>must be public</li>
 * <li>must NOT be static</li>
 * <li>must return a boolean.</li>
 * <li>must NOT take any parameters.</li>
 * <li>annotated with @Condition annotation.</li>
 * </ol>
 *
 * @see Condition
 * @see Precondition
 * @see Given
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ReferrerAttribute {
  String value();
}

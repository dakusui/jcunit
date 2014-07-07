package com.github.dakusui.jcunit.core.encoders;

/**
 * Factory methods for <code>ObjectEncoder</code> instances.
 * 
 * @author hiroshi
 * 
 */
public class ObjectEncoders {
  private ObjectEncoders() {
  }

  /**
   * Creates an <code>ObjectEncoder</code> instance that uses Java's
   * serialization mechanism.
   * 
   * @return <code>ObjectEncoder</code> that uses Java's serialization.
   */
  public static ObjectEncoder createSerializingEncoder() {
    return new SerializingEncoder();
  }

  /**
   * Creates an <code>ObjectEncoder</code> instance that encodes an object in
   * JSON format using Gson library.
   * 
   * @param clazz
   *          A class to which a decoded object belongs.
   * @return <code>ObjectEncoder</code> based on Gson library.
   */
  public static ObjectEncoder createGsonEncoder(Class<?> clazz) {
    return new GsonEncoder(clazz);
  }

  /**
   * Creates an <code>ObjectEncoder</code> instance that encodes an object in
   * XML format using XStream library.
   * 
   * @return <code>ObjectEncoder</code> based on XStream library.
   */
  public static ObjectEncoder createXStreamEncoder() {
    return new XStreamEncoder();
  }
}

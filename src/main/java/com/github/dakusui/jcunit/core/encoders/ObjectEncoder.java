package com.github.dakusui.jcunit.core.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface that defines methods to read an object from
 * <code>InputStream</code> and to write it to <code>OutputStream</code>
 *
 * @author hiroshi
 */
public interface ObjectEncoder {
  /**
   * Writes an object <code>obj</code> to the given <code>OutputStream</code>.
   *
   * @param os  An <code>OutputStream</code> to which <code>obj</code> will be
   *            written.
   * @param obj An object to be written.
   * @throws IOException Failed to write the object to the output stream.
   */
  public void encodeObject(OutputStream os, Object obj) throws IOException;

  /**
   * Reads an object from <code>InputStream</code>
   *
   * @param is An input stream from which an object will be returned.
   * @return An object read from the input stream.
   * @throws IOException Failed to load an object from the input stream.
   */
  public Object decodeObject(InputStream is) throws IOException;
}

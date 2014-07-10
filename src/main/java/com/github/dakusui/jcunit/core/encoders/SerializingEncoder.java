package com.github.dakusui.jcunit.core.encoders;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.*;

class SerializingEncoder extends BaseObjectEncoder {

  @Override
  public void encodeObject(OutputStream os, Object obj) throws IOException {
    ObjectOutputStream oos = new ObjectOutputStream(os);
    try {
      oos.writeObject(obj);
    } finally {
      oos.close();
    }
  }

  @Override
  public Object decodeObject(InputStream is) throws IOException {
    Object ret;
    ObjectInputStream ois = new ObjectInputStream(is);
    try {
      ret = ois.readObject();
    } catch (ClassNotFoundException e) {
      String msg = createMessage_FailedToDecodeObject(e);
      throw new JCUnitException(msg, e);
    } finally {
      ois.close();
    }
    return ret;
  }
}

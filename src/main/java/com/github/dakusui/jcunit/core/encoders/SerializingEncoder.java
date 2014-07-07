package com.github.dakusui.jcunit.core.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

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
      throw new JCUnitRuntimeException(msg, e);
    } finally {
      ois.close();
    }
    return ret;
  }
}

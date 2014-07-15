package com.github.dakusui.jcunit.compat.core.encoders;

import com.google.gson.Gson;

import java.io.*;

/*
 * This encoders is still buggy.
 */
class GsonEncoder extends BaseObjectEncoder {

  private Class<?> clazz;
  private Gson     gson;

  GsonEncoder(Class<?> clazz) {
    this.clazz = clazz;
    this.gson = new Gson();
  }

  @Override
  public void encodeObject(OutputStream os, Object obj) throws IOException {
    OutputStreamWriter writer = new OutputStreamWriter(os);
    try {
      writer.write(gson.toJson(obj));
    } finally {
      writer.close();
    }
  }

  @Override
  public Object decodeObject(InputStream is) throws IOException {
    Reader reader = new InputStreamReader(is);
    Object ret;
    ret = this.gson.fromJson(reader, this.clazz);
    return ret;
  }
}

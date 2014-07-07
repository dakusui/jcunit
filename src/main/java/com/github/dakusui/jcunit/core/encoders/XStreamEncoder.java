package com.github.dakusui.jcunit.core.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;

class XStreamEncoder extends BaseObjectEncoder {

  private XStream xstream;

  XStreamEncoder() {
    this.xstream = new XStream();
  }

  @Override
  public void encodeObject(OutputStream os, Object obj) throws IOException {
    xstream.toXML(obj, os);
  }

  @Override
  public Object decodeObject(InputStream is) throws IOException {
    return xstream.fromXML(is);
  }

}

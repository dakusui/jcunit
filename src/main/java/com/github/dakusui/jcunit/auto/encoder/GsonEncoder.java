package com.github.dakusui.jcunit.auto.encoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import com.google.gson.Gson;

/*
 * This encoder is still buggy.
 */
class GsonEncoder extends BaseObjectEncoder {
	
	private Class<?> clazz;
	private Gson gson;

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

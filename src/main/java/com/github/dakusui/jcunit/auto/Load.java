package com.github.dakusui.jcunit.auto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

public class Load extends AutoBase {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -6545578051675203857L;

	@Override
	protected Object autoBaseExec(String testName, Object obj, String fieldName)
			throws JCUnitException {
		return load(obj.getClass(), fieldName, testName);
	}
	
	private Object load(Class<?> clazz, String fieldName, String testName) throws JCUnitException {
		return readObjectFromFile(fileForField(baseDir(), testName, field(clazz, fieldName)));
	}
	
	private Object readObjectFromFile(File f) {
		Object ret;
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			try {
				ret = ois.readObject();
			} finally {
				ois.close();
			}
		} catch (ClassNotFoundException e) {
			String msg = String.format("Failed to deserialize an object in a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		} catch (FileNotFoundException e) {
			String msg = String.format("Failed to find a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		} catch (IOException e) {
			String msg = String.format("Failed to read object from a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		}
		return ret;
	}

}

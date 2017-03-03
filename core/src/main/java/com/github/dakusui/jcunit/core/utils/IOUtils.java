package com.github.dakusui.jcunit.core.utils;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.*;

public enum IOUtils {
  ;

  /**
   * Creates a file using {@code java.io.File#createNewFile()} method.
   *
   * @param file A file to be created.
   * @return true - created / false - not created.
   * @see java.io.File
   */
  public static boolean createFile(File file) {
    Checks.checknotnull(file);
    try {
      return file.createNewFile();
    } catch (IOException e) {
      throw Checks.wrap(e);
    }
  }

  public static BufferedOutputStream openForWrite(File f) {
    BufferedOutputStream ret;
    try {
      ret = new BufferedOutputStream(new FileOutputStream(f));
    } catch (FileNotFoundException e) {
      throw Checks.wrap(e);
    }
    return ret;
  }

  public static BufferedInputStream openForRead(File f) {
    Checks.checknotnull(f);
    BufferedInputStream ret;
    try {
      ret = new BufferedInputStream(new FileInputStream(f));
    } catch (FileNotFoundException e) {
      throw Checks.wrap(e, "File not found: '%s'", f.getAbsolutePath());
    }
    return ret;
  }

  public static void close(Closeable stream) {
    try {
      stream.close();
    } catch (IOException e) {
      throw Checks.wrap(e);
    }
  }

  /**
   * Saves a given object to a file.
   *
   * @param obj An object to be saved.
   * @param to  A file to which {@code obj} is saved.
   */
  public static void save(Object obj, File to) {
    BufferedOutputStream bos;
    bos = openForWrite(to);
    try {
      save(obj, bos);
    } finally {
      close(bos);
    }
  }

  public static void save(Object obj, OutputStream os) {
    Checks.checknotnull(obj);
    Checks.checknotnull(os);

    try {
      ObjectOutputStream oos = new ObjectOutputStream(os);
      try {
        oos.writeObject(obj);
      } finally {
        oos.close();
      }
    } catch (IOException e) {
      throw Checks.wrap(e);
    }
  }

  public static Object load(File f) {
    Checks.checknotnull(f);
    BufferedInputStream bis;
    bis = openForRead(f);
    try {
      return load(bis);
    } finally {
      close(bis);
    }
  }

  public static <T> T load(Class<T> clazz, File f) {
    Object ret = load(f);
    if (ret == null) {
      return null;
    }
    Checks.checkcond (
        Checks.checknotnull(clazz).isAssignableFrom(ret.getClass()),
        "The specified file '%s' is not compatible with '%s'",
        f.getAbsolutePath(),
        clazz.getCanonicalName());
    return (T)ret;
  }

  public static Object load(InputStream is) {
    Checks.checknotnull(is);
    Object ret = null;
    try {
      ObjectInputStream ois = new ObjectInputStream(is);
      try {
        ret = ois.readObject();
      } catch (ClassNotFoundException e) {
        throw Checks.wrap(e);
      } finally {
        ois.close();
      }
    } catch (IOException e) {
      throw Checks.wrap(e);
    }
    return ret;
  }

  /**
   * By default File#delete fails for non-empty directories, it works like "rm".
   * We need something a little more brutal - this does the equivalent of "rm -r"
   * <p/>
   * This method is cited from the url indicated in the link.
   *
   * @param path Root File Path
   * @return true iff the file and all sub files/directories have been removed
   * @link "http://stackoverflow.com/questions/779519/delete-files-recursively-in-java"
   */
  public static boolean deleteRecursive(File path) {
    Checks.checknotnull(path);
    if (!path.exists()) {
      throw new JCUnitException(
          String.format("Path '%s' was not found.", path.getAbsolutePath()), null);
    }
    boolean ret = true;
    if (path.isDirectory()) {
      //noinspection ConstantConditions
      for (File f : path.listFiles()) {
        ret = ret && deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }

  public static void mkdirs(File dir) {
    Checks.checknotnull(dir).mkdirs();
    Checks.checkcond(dir.exists() && dir.isDirectory(), "Failed to create a directory '%s'.", dir);
  }

  public static File determineTestSuiteFile(Class<?> testClass) {
    String fqcn = Checks.checknotnull(testClass).getCanonicalName();
    String filename = String.format("%s/testsuites/%s/testsuite.dat", SystemProperties.jcunitBaseDir(), fqcn);
    return new File(filename);
  }
}

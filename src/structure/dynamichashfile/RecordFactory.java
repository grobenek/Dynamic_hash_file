package structure.dynamichashfile;

import java.lang.reflect.InvocationTargetException;

public class RecordFactory {

  private RecordFactory() {}

  public static <T extends Record> T createDummyInstance(Class<? extends Record> recordClass) {
    if (recordClass == null) {
      throw new IllegalArgumentException("Cannot create dummy instance of null class!");
    }

    try {
      return (T) recordClass.getMethod("getDummyInstance").invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}

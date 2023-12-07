package structure.dynamichashfile.constant;

public class ElementByteSize {
  private static final int INT_BYTE_SIZE = 4;
  private static final int LONG_BYTE_SIZE = 8;

  public static int intByteSize() {
    return INT_BYTE_SIZE;
  }

  public static int longByteSize() {
    return LONG_BYTE_SIZE;
  }
}

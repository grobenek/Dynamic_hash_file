package structure.dynamichashfile;

import java.util.BitSet;

public abstract class Record implements IConvertableToBytes {
  protected static int BYTE_SIZE;
  protected static Record DUMMY_INSTANCE;

  public static int getByteSize() {
    return BYTE_SIZE;
  }

  public static Record getDummyInstance() {
    return DUMMY_INSTANCE;
  }

  public abstract BitSet hash();

  public abstract Record createDummyRecord();

  @Override
  public abstract byte[] toByteArray();

  @Override
  public abstract void fromByteArray(byte[] byteArray);
}

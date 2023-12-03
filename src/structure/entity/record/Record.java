package structure.entity.record;

import structure.entity.IConvertableToBytes;

import java.util.BitSet;

/**
 * Abstract class requiered for DynamicHashFile.
 *
 * <p>All classes extending it NEEDS TO IMPLEMENT static getDummyInstance method!
 */
public abstract class Record implements IConvertableToBytes {
  public abstract int getByteSize();

  public abstract BitSet hash();

  @Override
  public abstract byte[] toByteArray();

  @Override
  public abstract void fromByteArray(byte[] byteArray);
}

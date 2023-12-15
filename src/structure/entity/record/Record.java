package structure.entity.record;

import java.util.BitSet;
import structure.entity.IConvertableToBytes;

/**
 * Abstract class requiered for DynamicHashFile.
 *
 * <p>All classes extending it NEED TO IMPLEMENT static getDummyInstance method!
 */
public abstract class Record implements IConvertableToBytes {
  public abstract int getByteSize();

  public abstract BitSet hash();

  public abstract int getMaxHashSize();

  @Override
  public abstract byte[] toByteArray();

  @Override
  public abstract void fromByteArray(byte[] byteArray);
}

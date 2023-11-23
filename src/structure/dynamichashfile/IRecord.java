package structure.dynamichashfile;

import java.util.BitSet;

public interface IRecord extends IConvertableToBytes {
  BitSet hash();
  @Override
  byte[] toByteArray();

  @Override
  void fromByteArray(byte[] byteArray);
}

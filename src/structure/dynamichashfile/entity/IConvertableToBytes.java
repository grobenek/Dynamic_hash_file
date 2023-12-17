package structure.dynamichashfile.entity;

public interface IConvertableToBytes {
  byte[] toByteArray();

  void fromByteArray(byte[] byteArray);
}
